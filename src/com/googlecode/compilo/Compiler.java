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
    private final ImmutableList<Pair<Predicate<String>, Function2<Source, Destination, Integer>>> processors;

    private Compiler(ImmutableList<Pair<Predicate<String>, Function2<Source, Destination, Integer>>> processors) {
        this.processors = processors;
    }

    public static Compiler compiler(Iterable<File> dependancies) throws IOException {
        return compiler(dependancies, sequence(Debug));
    }

    public static Compiler compiler(Iterable<File> dependancies, Sequence<?> compileOptions) throws IOException {
        return compiler(dependancies, compileOptions, getSystemJavaCompiler());
    }

    public static Compiler compiler(Iterable<File> dependancies, Sequence<?> compileOptions, JavaCompiler javaCompiler) throws IOException {
        return compiler(constructors.<Pair<Predicate<String>, Function2<Source, Destination, Integer>>>empty()).
                add(endsWith(".java"), CompileProcessor.compile(compileOptions, javaCompiler, dependancies)).
                add(not(endsWith(".java")), copy());
    }

    private static Function2<Source, Destination, Integer> copy() {
        return new Function2<Source, Destination, Integer>() {
            @Override
            public Integer call(Source source, Destination destination) throws Exception {
                return Source.methods.copy(source, destination);
            }
        };
    }

    public static Compiler compiler(ImmutableList<Pair<Predicate<String>, Function2<Source, Destination, Integer>>> processors) {
        return new Compiler(processors);
    }

    public Compiler add(Predicate<? super String> predicate, Callable2<? super Source, ? super Destination, ? extends Integer> processor) {
        return compiler(processors.add(Pair.pair(Unchecked.<Predicate<String>>cast(predicate), Function2.<Source, Destination, Integer>function(processor))));
    }

    public Boolean compile(final File sourceDirectory, File destinationJar) throws IOException {
        Source source = source(sourceDirectory);
        Destination destination = ZipDestination.zipDestination(new FileOutputStream(destinationJar));

        final Map<Pair<Predicate<String>, Function2<Source, Destination, Integer>>, List<Pair<String, InputStream>>> matchedSources = partition(source);

        for (final Pair<Predicate<String>, Function2<Source, Destination, Integer>> processor : processors) {
            processor.second().apply(iterableSource(matchedSources.get(processor)), destination);
        }

        source.close();
        destination.close();
        return true;
    }

    private Map<Pair<Predicate<String>, Function2<Source, Destination, Integer>>, List<Pair<String, InputStream>>> partition(Source source) {
        final Map<Pair<Predicate<String>, Function2<Source, Destination, Integer>>, List<Pair<String, InputStream>>> matchedSources = Maps.map();

        for (Pair<String, InputStream> pair : source.sources()) {
            for (Pair<Predicate<String>, Function2<Source, Destination, Integer>> processor : processors) {
                if (processor.first().matches(pair.first())) {
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