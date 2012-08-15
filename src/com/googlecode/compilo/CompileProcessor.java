package com.googlecode.compilo;

import com.googlecode.totallylazy.Callables;
import com.googlecode.totallylazy.Destination;
import com.googlecode.totallylazy.Pair;
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
import java.io.InputStream;

import static com.googlecode.totallylazy.LazyException.lazyException;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Strings.endsWith;
import static java.lang.String.format;
import static javax.tools.StandardLocation.CLASS_PATH;
import static javax.tools.ToolProvider.getSystemJavaCompiler;

public class CompileProcessor implements Processor {
    public static final JavaCompiler DEFAULT_COMPILER = getSystemJavaCompiler();
    public static final Sequence<CompileOption> DEFAULT_OPTIONS = sequence(CompileOption.Debug);
    private static final Predicate<String> JAVA_FILES = endsWith(".java");
    private final JavaCompiler compiler;
    private final Sequence<CompileOption> options;
    private final StandardJavaFileManager standardFileManager;

    private CompileProcessor(JavaCompiler compiler, Iterable<CompileOption> options, Iterable<File> dependancies) {
        this.compiler = compiler;
        this.options = sequence(options);
        standardFileManager = compiler.getStandardFileManager(null, null, Compiler.UTF8);
        setDependencies(dependancies);
    }

    public static CompileProcessor compile(final Iterable<CompileOption> options, final JavaCompiler compiler, Iterable<File> dependancies) {
        return new CompileProcessor(compiler, options, dependancies);
    }

    public static String compile(Iterable<File> dependancies, Source source, Destination destination) throws Exception {
        try {
            return compile(DEFAULT_OPTIONS, DEFAULT_COMPILER, dependancies).call(source, destination);
        } finally {
            source.close();
            destination.close();
        }
    }

    @Override
    public String call(Source source, Destination destination) throws Exception {
        Sequence<Pair<String, InputStream>> sources = source.sources();
        Boolean success = compiler.getTask(null, manager(destination), null, options.flatMap(Callables.<Iterable<String>>value()), null, javaFileObjects(sources)).call();
        if (!success) throw new IllegalStateException("Compile failed");
        int size = sources.size();
        return format("    [javac] Compiling %s source files%n", size);
    }

    private JavaFileManager manager(final Destination destination) throws FileNotFoundException {
        return new ZipFileManager(standardFileManager, destination);
    }

    private Sequence<JavaFileObject> javaFileObjects(Sequence<Pair<String, InputStream>> javaFiles) {
        return javaFiles.map(SourceFileObject.sourceFileObject());
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
