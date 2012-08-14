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

import static com.googlecode.compilo.CompileOption.Debug;
import static com.googlecode.totallylazy.Files.isFile;
import static com.googlecode.totallylazy.Files.recursiveFiles;
import static com.googlecode.totallylazy.Predicates.not;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Strings.endsWith;
import static com.googlecode.totallylazy.collections.ImmutableList.constructors;
import static javax.tools.ToolProvider.getSystemJavaCompiler;

public class Compiler {
    public static final Charset UTF8 = Charset.forName("UTF-8");
    private final ImmutableList<Processor> processors;

    private Compiler(ImmutableList<Processor> processors) {
        this.processors = processors;
    }

    public static Compiler compiler(Iterable<File> dependancies) throws IOException {
        return compiler(dependancies, sequence(Debug));
    }

    public static Compiler compiler(Iterable<File> dependancies, Sequence<?> compileOptions) throws IOException {
        return compiler(dependancies, compileOptions, getSystemJavaCompiler());
    }

    public static Compiler compiler(Iterable<File> dependancies, Sequence<?> compileOptions, JavaCompiler javaCompiler) throws IOException {
        return compiler(constructors.<Processor>empty()).
                add(CompileProcessor.compile(compileOptions, javaCompiler, dependancies)).
                add(copy(not(endsWith(".java"))));
    }

    private static Processor copy(final Predicate<? super String> predicate) {
        return new Processor() {
            @Override
            public Integer call(Source source, Destination destination) throws Exception {
                return Source.methods.copy(source, destination);
            }

            @Override
            public boolean matches(String other) {
                return predicate.matches(other);
            }

            @Override
            public String name() {
                return "Copied";
            }
        };
    }

    public static Compiler compiler(ImmutableList<Processor> processors) {
        return new Compiler(processors);
    }

    public Compiler add(Processor processor) {
        return compiler(processors.add(processor));
    }

    public Map<Processor, List<Pair<String, InputStream>>> compile(final File sourceDirectory, File destinationJar) throws Exception {
        Source source = source(sourceDirectory);
        Destination destination = ZipDestination.zipDestination(new FileOutputStream(destinationJar));

        final Map<Processor, List<Pair<String, InputStream>>> matchedSources = partition(source);

        for (final Processor processor : processors) {
            Integer number = processor.call(iterableSource(matchedSources.get(processor)), destination);
            System.out.printf("%s %d inputs%n", processor.name(), number);
        }

        source.close();
        destination.close();
        return matchedSources;
    }

    private Map<Processor, List<Pair<String, InputStream>>> partition(Source source) {
        final Map<Processor, List<Pair<String, InputStream>>> matchedSources = Maps.map();

        for (Pair<String, InputStream> pair : source.sources()) {
            for (Processor processor : processors) {
                if (processor.matches(pair.first())) {
                    if (!matchedSources.containsKey(processor))
                        matchedSources.put(processor, new ArrayList<Pair<String, InputStream>>());
                    matchedSources.get(processor).add(pair);
                }
            }
        }
        return matchedSources;
    }

    private MemoryStore source(File sourceDirectory) {
        return MemoryStore.copy(FileSource.fileSource(sourceDirectory, recursiveFiles(sourceDirectory).filter(isFile())));
    }

    private Source iterableSource(final Iterable<Pair<String, InputStream>> sequence) {
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