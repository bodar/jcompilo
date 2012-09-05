package com.googlecode.compilo;

import com.googlecode.totallylazy.Callables;
import com.googlecode.totallylazy.Destination;
import com.googlecode.totallylazy.Function2;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Source;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;

import static com.googlecode.totallylazy.Closeables.using;
import static com.googlecode.totallylazy.LazyException.lazyException;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Strings.endsWith;
import static javax.tools.StandardLocation.CLASS_PATH;
import static javax.tools.ToolProvider.getSystemJavaCompiler;

public class CompileProcessor implements Processor {
    public static final JavaCompiler DEFAULT_COMPILER = getSystemJavaCompiler();
    public static final Sequence<CompileOption> DEFAULT_OPTIONS = sequence(CompileOption.Debug);
    private static final Predicate<String> JAVA_FILES = endsWith(".java");
    private final Environment env;
    private final JavaCompiler compiler;
    private final Sequence<CompileOption> options;
    private final StandardJavaFileManager standardFileManager;

    private CompileProcessor(Environment env, JavaCompiler compiler, Iterable<CompileOption> options, Iterable<File> dependancies) {
        this.env = env;
        this.compiler = compiler;
        this.options = sequence(options);
        standardFileManager = compiler.getStandardFileManager(null, null, Compiler.UTF8);
        setDependencies(dependancies);
    }

    public static CompileProcessor compile(Environment env, final Iterable<CompileOption> options, final JavaCompiler compiler, Iterable<File> dependancies) {
        return new CompileProcessor(env, compiler, options, dependancies);
    }

    public static boolean compile(final Environment env, final Iterable<File> dependancies, Source source, Destination destination) throws Exception {
        return using(source, destination, new Function2<Source, Destination, Boolean>() {
            @Override
            public Boolean call(Source source, Destination destination) throws Exception {
                return compile(env, DEFAULT_OPTIONS, DEFAULT_COMPILER, dependancies).call(Inputs.constructors.inputs(source), destination);
            }
        });
    }

    @Override
    public Boolean call(Inputs sources, Destination destination) throws Exception {
        env.out().prefix("    [javac] ");
        env.out().printf("Compiling %s source files%n", sources.size());
        Boolean success = compiler.getTask(new OutputStreamWriter(env.out()), manager(destination), null, options.flatMap(Callables.<Iterable<String>>value()), null, javaFileObjects(sources)).call();
        env.out().clearPrefix();
        if (!success) throw new IllegalStateException("Compile failed");
        return success;
    }

    private JavaFileManager manager(final Destination destination) throws FileNotFoundException {
        return new ZipFileManager(standardFileManager, destination);
    }

    private Sequence<JavaFileObject> javaFileObjects(Inputs javaFiles) {
        return sequence(javaFiles).map(SourceFileObject.sourceFileObject());
    }

    private void setDependencies(Iterable<File> dependancies) {
        try {
            if (sequence(dependancies).isEmpty()) return;
            standardFileManager.setLocation(CLASS_PATH, dependancies);
        } catch (IOException e) {
            throw lazyException(e);
        }
    }

    @Override
    public boolean matches(String other) {
        return JAVA_FILES.matches(other);
    }
}
