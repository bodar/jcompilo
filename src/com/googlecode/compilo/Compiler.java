package com.googlecode.compilo;

import com.googlecode.compilo.asm.AsmMethodHandler;
import com.googlecode.totallylazy.Destination;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Function2;
import com.googlecode.totallylazy.Maps;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.Source;
import com.googlecode.totallylazy.collections.ImmutableList;
import org.objectweb.asm.Type;

import javax.tools.JavaCompiler;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static com.googlecode.compilo.BackgroundDestination.backgroundDestination;
import static com.googlecode.compilo.BackgroundOutputs.backgroundOutputs;
import static com.googlecode.compilo.MemoryStore.memoryStore;
import static com.googlecode.compilo.Outputs.constructors.output;
import static com.googlecode.compilo.ResourceHandler.methods.decorate;
import static com.googlecode.compilo.asm.AsmResourceHandler.asmResourceHandler;
import static com.googlecode.compilo.tco.TailRecHandler.tailRecHandler;
import static com.googlecode.totallylazy.Closeables.using;
import static com.googlecode.totallylazy.FileSource.fileSource;
import static com.googlecode.totallylazy.Files.isFile;
import static com.googlecode.totallylazy.Files.recursiveFiles;
import static com.googlecode.totallylazy.Predicates.not;
import static com.googlecode.totallylazy.Predicates.or;
import static com.googlecode.totallylazy.Runnables.VOID;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Strings.endsWith;
import static com.googlecode.totallylazy.Strings.startsWith;
import static com.googlecode.totallylazy.ZipDestination.zipDestination;
import static com.googlecode.totallylazy.collections.ImmutableList.constructors;
import static java.lang.Boolean.parseBoolean;
import static java.lang.String.format;

public class Compiler {
    public static final int CPUS = Runtime.getRuntime().availableProcessors();
    public static final Predicate<String> JAVA_FILES = endsWith(".java");
    private final Environment env;
    private final ImmutableList<Processor> processors;
    private final ImmutableList<ResourceHandler> resourceHandlers;

    private Compiler(Environment env, ImmutableList<Processor> processors, ImmutableList<ResourceHandler> resourceHandlers) {
        this.env = env;
        this.processors = processors;
        this.resourceHandlers = resourceHandlers;
    }

    public static Compiler compiler(Environment env, ImmutableList<Processor> processors, final ImmutableList<ResourceHandler> resourceHandlers1) {
        return new Compiler(env, processors, resourceHandlers1);
    }

    public static Compiler compiler(Environment env) {
        return new Compiler(env, constructors.<Processor>empty(), constructors.<ResourceHandler>empty());
    }

    public static Compiler compiler(Environment env, Iterable<File> dependancies) {
        return compiler(env, dependancies, CompileProcessor.DEFAULT_OPTIONS);
    }

    public static Compiler compiler(Environment env, Iterable<File> dependancies, Iterable<CompileOption> compileOptions) {
        return compiler(env, dependancies, compileOptions, CompileProcessor.DEFAULT_COMPILER);
    }

    public static Compiler compiler(Environment env, Iterable<File> dependancies, Iterable<CompileOption> compileOptions, JavaCompiler javaCompiler) {
        return compiler(env).
                add(CompileProcessor.compile(env, compileOptions, javaCompiler, dependancies)).
                add(CopyProcessor.copy(env, not(or(startsWith("."), JAVA_FILES)))).
                add(asmResourceHandler(asmProcessors(env)));
    }

    private static Sequence<Pair<Type, AsmMethodHandler>> asmProcessors(Environment env) {
        return postProcess(env) ?
                sequence(Pair.<Type, AsmMethodHandler>pair(tailRecClass(env), tailRecHandler())) :
                Sequences.<Pair<Type, AsmMethodHandler>>empty();
    }

    private static Type tailRecClass(Environment env) {
        String property = env.properties().getProperty("compilo.tailrec");
        return Type.getType(format("L%s;", property.replace('.', '/')));
    }

    private static boolean postProcess(Environment env) {
        return parseBoolean(env.properties().getProperty("compilo.post.process", "true"));
    }

    public Compiler add(Processor processor) {
        return compiler(env, processors.cons(processor), resourceHandlers);
    }

    public Compiler add(ResourceHandler resourceHandler) {
        return compiler(env, processors, resourceHandlers.cons(resourceHandler));
    }

    public Void compile(final File sourceDirectory, final File destinationJar) throws Exception {
        Source source = fileSource(sourceDirectory, recursiveFiles(sourceDirectory).filter(isFile()).realise());
        if (source.sources().isEmpty()) return VOID;
        return using(source, destination(destinationJar), new Function2<Source, Destination, Void>() {
            public Void call(final Source source, Destination destination) throws Exception {
                return compile(memoryStore(source), output(destination));
            }
        });
    }

    private Destination destination(File destinationJar) throws FileNotFoundException {
        env.out().prefix("      [zip] ").printf("Creating: %s%n", destinationJar.getAbsoluteFile());
        return backgroundDestination(zipDestination(new FileOutputStream(destinationJar)));
    }

    public Void compile(final Inputs inputs, final Outputs raw) throws Exception {
        return using(backgroundOutputs(env, decorate(resourceHandlers, raw)), new Function1<Outputs, Void>() {
            @Override
            public Void call(final Outputs outputs) throws Exception {
                final Map<Processor, MemoryStore> partitions = partition(inputs);

                sequence(processors).mapConcurrently(new Function1<Processor, Boolean>() {
                    @Override
                    public Boolean call(Processor processor) throws Exception {
                        Inputs matched = partitions.get(processor);
                        return matched.isEmpty() || processor.process(matched, outputs);
                    }
                }).realise();
                return VOID;
            }
        });
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