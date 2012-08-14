package com.googlecode.compilo;

import com.googlecode.totallylazy.*;
import com.googlecode.totallylazy.predicates.LogicalPredicate;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static com.googlecode.totallylazy.Callables.toString;
import static com.googlecode.totallylazy.Strings.endsWith;
import static javax.tools.StandardLocation.CLASS_PATH;

public class CompileProcessor extends Function2<Source, Destination, Integer> implements Processor {
    private static final LogicalPredicate<String> JAVA_FILES = endsWith(".java");
    private final JavaCompiler compiler;
    private final Sequence<?> options;
    private final StandardJavaFileManager standardFileManager;

    private CompileProcessor(JavaCompiler compiler, Sequence<?> options, Iterable<File> dependancies) throws IOException {
        this.compiler = compiler;
        this.options = options;
        standardFileManager = compiler.getStandardFileManager(null, null, Compiler.UTF8);
        setDependencies(dependancies);
    }

    public static CompileProcessor compile(final Sequence<?> options, final JavaCompiler compiler, Iterable<File> dependancies) throws IOException {
        return new CompileProcessor(compiler, options, dependancies);
    }

    @Override
    public Integer call(Source source, Destination destination) throws Exception {
        Sequence<Pair<String, InputStream>> sources = source.sources();
        Boolean success = compiler.getTask(null, manager(destination), null, options.map(toString), null, javaFileObjects(sources)).call();
        if(!success) throw new IllegalStateException("Compile failed");
        return sources.size();
    }

    private JavaFileManager manager(final Destination destination) throws FileNotFoundException {
        return new ZipFileManager(standardFileManager, destination);
    }

    private Sequence<JavaFileObject> javaFileObjects(Sequence<Pair<String, InputStream>> javaFiles) {
        return javaFiles.map(new Function1<Pair<String, InputStream>, JavaFileObject>() {
            @Override
            public JavaFileObject call(final Pair<String, InputStream> pair) throws Exception {
                return new SourceFileObject(pair);
            }
        });
    }

    private void setDependencies(Iterable<File> dependancies) throws IOException {
        standardFileManager.setLocation(CLASS_PATH, dependancies);
    }

    @Override
    public boolean matches(String other) {
        return JAVA_FILES.matches(other);
    }

    @Override
    public String name() {
        return "Compiled";
    }
}
