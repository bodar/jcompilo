package com.googlecode.compilo;

import com.googlecode.totallylazy.*;
import com.googlecode.totallylazy.collections.ImmutableList;

import javax.tools.JavaCompiler;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.googlecode.totallylazy.Files.isFile;
import static com.googlecode.totallylazy.Files.recursiveFiles;
import static com.googlecode.totallylazy.Predicates.not;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Strings.endsWith;
import static com.googlecode.totallylazy.ZipDestination.zipDestination;
import static com.googlecode.totallylazy.collections.ImmutableList.constructors;
import static javax.tools.ToolProvider.getSystemJavaCompiler;

public class Compiler {
    public static final Charset UTF8 = Charset.forName("UTF-8");
    private final ImmutableList<Processor> processors;

    private Compiler(ImmutableList<Processor> processors) {
        this.processors = processors;
    }

    public static Compiler compiler(Iterable<File> dependancies) {
        return compiler(dependancies, sequence(CompileOption.Debug));
    }

    public static Compiler compiler(Iterable<File> dependancies, Sequence<CompileOption> compileOptions)  {
        return compiler(dependancies, compileOptions, getSystemJavaCompiler());
    }

    public static Compiler compiler(Iterable<File> dependancies, Sequence<CompileOption> compileOptions, JavaCompiler javaCompiler) {
        return compiler(constructors.<Processor>empty()).
                add(CompileProcessor.compile(compileOptions, javaCompiler, dependancies)).
                add(CopyProcessor.copy(not(endsWith(".java"))));
    }

    public static Compiler compiler(ImmutableList<Processor> processors) {
        return new Compiler(processors);
    }

    public Compiler add(Processor processor) {
        return compiler(processors.add(processor));
    }

    public Map<Processor, List<Pair<String, InputStream>>> compile(final File sourceDirectory, File destinationJar) throws Exception {
        Source source = source(sourceDirectory);
        Destination destination = zipDestination(new FileOutputStream(destinationJar));
        try {
            return compile(source, destination);
        } finally {
            source.close();
            destination.close();
            System.out.printf("Created '%s'%n", destinationJar);
        }
    }

    public Map<Processor, List<Pair<String, InputStream>>> compile(Source source, Destination destination) throws Exception {
        final Map<Processor, List<Pair<String, InputStream>>> matchedSources = partition(source);

        for (final Processor processor : processors) {
            List<Pair<String, InputStream>> matched = matchedSources.get(processor);
            if(matched.isEmpty()) continue;
            Integer number = processor.call(iterableSource(matched), destination);
            System.out.printf("%s %d; ", processor.name(), number);
        }

        return matchedSources;
    }

    private Map<Processor, List<Pair<String, InputStream>>> partition(Source source) {
        final Map<Processor, List<Pair<String, InputStream>>> matchedSources = Maps.map();

        for (Processor processor : processors) {
            matchedSources.put(processor, new ArrayList<Pair<String, InputStream>>());
        }

        for (Pair<String, InputStream> pair : source.sources()) {
            for (Processor processor : processors) {
                if (processor.matches(pair.first())) {
                    matchedSources.get(processor).add(pair);
                }
            }
        }
        return matchedSources;
    }

    private MemoryStore source(File sourceDirectory) {
        return MemoryStore.copy(FileSource.fileSource(sourceDirectory, recursiveFiles(sourceDirectory).filter(isFile())));
    }

    public static Source iterableSource(final Iterable<Pair<String, InputStream>> sequence) {
        return new Source() {
            @Override
            public Sequence<Pair<String, InputStream>> sources() {
                return sequence(sequence);
            }

            @Override
            public void close() throws IOException {
            }
        };
    }

}