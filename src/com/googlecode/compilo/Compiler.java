package com.googlecode.compilo;

import com.googlecode.totallylazy.Bytes;
import com.googlecode.totallylazy.Closeables;
import com.googlecode.totallylazy.Destination;
import com.googlecode.totallylazy.Function2;
import com.googlecode.totallylazy.Maps;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Source;
import com.googlecode.totallylazy.collections.ImmutableList;

import javax.tools.JavaCompiler;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.googlecode.compilo.MemoryStore.copy;
import static com.googlecode.compilo.MemoryStore.memoryStore;
import static com.googlecode.totallylazy.Closeables.using;
import static com.googlecode.totallylazy.FileSource.fileSource;
import static com.googlecode.totallylazy.Files.isFile;
import static com.googlecode.totallylazy.Files.recursiveFiles;
import static com.googlecode.totallylazy.Predicates.not;
import static com.googlecode.totallylazy.Runnables.VOID;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Strings.startsWith;
import static com.googlecode.totallylazy.ZipDestination.zipDestination;
import static com.googlecode.totallylazy.collections.ImmutableList.constructors;

public class Compiler {
    public static final Charset UTF8 = Charset.forName("UTF-8");
    private final Environment env;
    private final ImmutableList<Processor> processors;

    private Compiler(Environment env, ImmutableList<Processor> processors) {
        this.env = env;
        this.processors = processors;
    }

    public static Compiler compiler(Environment env, Iterable<File> dependancies) {
        return compiler(env, dependancies, CompileProcessor.DEFAULT_OPTIONS);
    }

    public static Compiler compiler(Environment env, Iterable<File> dependancies, Iterable<CompileOption> compileOptions) {
        return compiler(env, dependancies, compileOptions, CompileProcessor.DEFAULT_COMPILER);
    }

    public static Compiler compiler(Environment env, Iterable<File> dependancies, Iterable<CompileOption> compileOptions, JavaCompiler javaCompiler) {
        return compiler(env, constructors.<Processor>empty()).
                add(CopyProcessor.copy(env, not(startsWith(".")))).
                add(CompileProcessor.compile(env, compileOptions, javaCompiler, dependancies));
    }

    public static Compiler compiler(Environment env, ImmutableList<Processor> processors) {
        return new Compiler(env, processors);
    }

    public Compiler add(Processor processor) {
        return compiler(env, processors.add(processor));
    }

    public Void compile(final File sourceDirectory, final File destinationJar) throws Exception {
        Source source = fileSource(sourceDirectory, recursiveFiles(sourceDirectory).filter(isFile()).realise());
        if (source.sources().isEmpty()) return VOID;
        env.out().prefix("      [zip] ").printf("Creating: %s%n", destinationJar.getAbsoluteFile());
        return using(source, BackgroundZip.backgroundZip(destinationJar), new Function2<Source, Destination, Void>() {
            public Void call(Source source, Destination destination) throws Exception {
                return compile(source, destination);
            }
        });
    }

    public Void compile(Source source, Destination destination) throws Exception {
        final Map<Processor, Map<String, byte[]>> matchedSources = partition(copy(source).data());

        for (final Processor processor : processors) {
            Map<String, byte[]> matched = matchedSources.get(processor);
            if (matched.isEmpty()) continue;
            Boolean result = processor.call(memoryStore(matched), destination);
        }
        return VOID;
    }

    private Map<Processor, Map<String, byte[]>> partition(Map<String, byte[]> source) {
        final Map<Processor, Map<String, byte[]>> matchedSources = Maps.map();

        for (Processor processor : processors) {
            matchedSources.put(processor, new HashMap<String, byte[]>());
        }

        for (Map.Entry<String, byte[]> entry : source.entrySet()) {
            for (Processor processor : processors) {
                if (processor.matches(entry.getKey())) {
                    matchedSources.get(processor).put(entry.getKey(), entry.getValue());
                }
            }
        }
        return matchedSources;
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