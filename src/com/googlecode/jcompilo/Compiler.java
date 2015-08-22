package com.googlecode.jcompilo;

import com.googlecode.jcompilo.asm.AsmMethodHandler;
import com.googlecode.jcompilo.tool.JCompiler;
import com.googlecode.totallylazy.*;
import com.googlecode.totallylazy.collections.PersistentList;
import com.googlecode.totallylazy.functions.Curried2;
import com.googlecode.totallylazy.functions.Function1;
import com.googlecode.totallylazy.io.Destination;
import com.googlecode.totallylazy.io.FileDestination;
import com.googlecode.totallylazy.io.Sources;
import com.googlecode.totallylazy.predicates.Predicate;
import jdk.internal.org.objectweb.asm.Type;

import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipOutputStream;

import static com.googlecode.jcompilo.BackgroundDestination.backgroundDestination;
import static com.googlecode.jcompilo.BackgroundOutputs.backgroundOutputs;
import static com.googlecode.jcompilo.MemoryStore.memoryStore;
import static com.googlecode.jcompilo.ModifiedPredicate.modifiedMatches;
import static com.googlecode.jcompilo.Outputs.constructors.output;
import static com.googlecode.jcompilo.ResourceHandler.methods.decorate;
import static com.googlecode.jcompilo.asm.AsmResourceHandler.asmResourceHandler;
import static com.googlecode.jcompilo.tco.TailRecHandler.tailRecHandler;
import static com.googlecode.totallylazy.Closeables.using;
import static com.googlecode.totallylazy.functions.Functions.andPair;
import static com.googlecode.totallylazy.io.FileSource.fileSource;
import static com.googlecode.totallylazy.Files.isFile;
import static com.googlecode.totallylazy.Files.recursiveFiles;
import static com.googlecode.totallylazy.functions.Functions.and;
import static com.googlecode.totallylazy.predicates.Predicates.not;
import static com.googlecode.totallylazy.predicates.Predicates.or;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Strings.endsWith;
import static com.googlecode.totallylazy.Strings.startsWith;
import static com.googlecode.totallylazy.io.ZipDestination.zipDestination;
import static com.googlecode.totallylazy.collections.PersistentList.constructors;
import static java.lang.Boolean.parseBoolean;
import static java.lang.String.format;

public class Compiler {
    public static final int CPUS = Math.max(1, Runtime.getRuntime().availableProcessors() / 2);
    public static final Predicate<String> JAVA_FILES = endsWith(".java");
    private final Environment env;
    private final PersistentList<Processor> processors;
    private final PersistentList<ResourceHandler> resourceHandlers;

    private Compiler(Environment env, PersistentList<Processor> processors, PersistentList<ResourceHandler> resourceHandlers) {
        this.env = env;
        this.processors = processors;
        this.resourceHandlers = resourceHandlers;
    }

    public static Compiler compiler(Environment env, PersistentList<Processor> processors, final PersistentList<ResourceHandler> resourceHandlers) {
        return new Compiler(env, processors, resourceHandlers);
    }

    public static Compiler compiler(Environment env) {
        return new Compiler(env, constructors.<Processor>empty(), constructors.<ResourceHandler>empty());
    }

    public static Compiler compiler(Environment env, Iterable<File> dependancies) {
        return compiler(env, dependancies, CompileProcessor.DEFAULT_OPTIONS);
    }

    public static Compiler compiler(Environment env, Iterable<File> dependancies, Iterable<CompileOption> compileOptions) {
        return compiler(env, dependancies, compileOptions, JCompiler.DEFAULT_COMPILER, Option.<DiagnosticListener<JavaFileObject>>none());
    }

    public static Compiler compiler(Environment env, Iterable<File> dependancies, Iterable<CompileOption> compileOptions, DiagnosticListener<JavaFileObject> diagnosticListener) {
        return compiler(env, dependancies, compileOptions, JCompiler.DEFAULT_COMPILER, Option.<DiagnosticListener<JavaFileObject>>some(diagnosticListener));
    }

    public static Compiler compiler(Environment env, Iterable<File> dependancies, Iterable<CompileOption> compileOptions, JavaCompiler javaCompiler, Option<DiagnosticListener<JavaFileObject>> diagnosticListener) {
        CompilerResources resources = new CompilerResources(javaCompiler, dependancies);
        return compiler(env).
                add(CompileProcessor.compile(env, compileOptions, javaCompiler, resources, diagnosticListener)).
                add(CopyProcessor.copy(env, not(or(startsWith("."), JAVA_FILES)))).
                add(asmResourceHandler(asmProcessors(env)));
    }

    private static Sequence<Pair<Type, AsmMethodHandler>> asmProcessors(Environment env) {
        return postProcess(env) ?
                sequence(Pair.<Type, AsmMethodHandler>pair(tailRecAnnotation(env), tailRecHandler())) :
                Sequences.<Pair<Type, AsmMethodHandler>>empty();
    }

    private static Type tailRecAnnotation(Environment env) {
        String property = env.properties().getProperty("jcompilo.tailrec");
        return typeFor(property);
    }

    public static Type typeFor(String className) {
        return Type.getType(format("L%s;", className.replace('.', '/')));
    }

    private static boolean postProcess(Environment env) {
        return parseBoolean(env.properties().getProperty("jcompilo.post.process", "true"));
    }

    public Compiler add(Processor processor) {
        return compiler(env, processors.cons(processor), resourceHandlers);
    }

    public Compiler add(ResourceHandler resourceHandler) {
        return compiler(env, processors, resourceHandlers.cons(resourceHandler));
    }

    public boolean compile(final File sourceDirectory, final File destination) throws Exception {
        Sources source = fileSource(sourceDirectory, recursiveFiles(sourceDirectory).filter(not(isFile().and(modifiedMatches(sourceDirectory, destination)))).realise());
        return source.sources().isEmpty() || using(source, backgroundDestination(destination(destination)), new Curried2<Sources, Destination, Boolean>() {
            public Boolean call(final Sources source, Destination destination) throws Exception {
                return compile(memoryStore(source), output(destination));
            }
        });
    }

    private Destination destination(File destination) throws IOException {
        if (destination.getPath().endsWith(".jar") || destination.getPath().endsWith(".zip")) {
            env.out().prefix("      [zip] ").printf("Creating: %s%n", destination.getAbsoluteFile());
            return zipDestination(outputStream(destination));
        }
        return FileDestination.fileDestination(destination);
    }

    private OutputStream outputStream(File destination) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(destination);
        return destination.getPath().endsWith(".jar") ? new JarOutputStream(fileOutputStream) : new ZipOutputStream(fileOutputStream);
    }

    public boolean compile(final Inputs inputs, final Outputs raw) throws Exception {
        return using(backgroundOutputs(env, decorate(resourceHandlers, raw)), new Function1<Outputs, Boolean>() {
            @Override
            public Boolean call(final Outputs outputs) throws Exception {
                final Map<Processor, MemoryStore> partitions = partition(inputs);

                return sequence(processors).mapConcurrently(processor -> {
                    Inputs matched = partitions.get(processor);
                    return matched.isEmpty() || processor.process(matched, outputs);
                }).reduceRight(andPair());
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

    public static Sources iterableSource(final Iterable<Sources.Source> sequence) {
        return new Sources() {
            @Override
            public Sequence<Source> sources() {
                return sequence(sequence);
            }

            @Override
            public void close() throws IOException {
            }
        };
    }
}