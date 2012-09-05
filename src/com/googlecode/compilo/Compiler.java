package com.googlecode.compilo;

import com.googlecode.totallylazy.Destination;
import com.googlecode.totallylazy.Function2;
import com.googlecode.totallylazy.Maps;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Source;
import com.googlecode.totallylazy.collections.ImmutableList;

import javax.tools.JavaCompiler;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;

import static com.googlecode.compilo.BackgroundDestination.backgroundDestination;
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
        return using(source, backgroundDestination(zipDestination(new FileOutputStream(destinationJar))), new Function2<Source, Destination, Void>() {
            public Void call(Source source, Destination destination) throws Exception {
                return compile(memoryStore(source), Outputs.constructors.output(destination));
            }
        });
    }

    public Void compile(final Inputs inputs, final Outputs outputs) throws Exception {
        final Map<Processor, MemoryStore> partitions = partition(inputs);

        for (final Processor processor : processors) {
            Inputs matched = partitions.get(processor);
            if (matched.isEmpty()) continue;
            Boolean result = processor.process(matched, outputs);
        }
        return VOID;
    }

    private Map<Processor, MemoryStore> partition(Inputs inputs) {
        final Map<Processor, MemoryStore> partitions = Maps.map();

        for (Processor processor : processors) {
            partitions.put(processor, memoryStore());
        }

        for (Resource resource : inputs) {
            for (Processor processor : processors) {
                if (processor.matches(resource.name())) {
                    partitions.get(processor).put(resource);
                }
            }
        }
        return partitions;
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