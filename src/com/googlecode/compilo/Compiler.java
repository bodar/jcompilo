package com.googlecode.compilo;

import com.googlecode.totallylazy.Files;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.zip.ZipOutputStream;

import static com.googlecode.compilo.CompileOption.Debug;
import static com.googlecode.totallylazy.Callables.toString;
import static com.googlecode.totallylazy.Closeables.using;
import static com.googlecode.totallylazy.FileSource.fileSource;
import static com.googlecode.totallylazy.Files.hasSuffix;
import static com.googlecode.totallylazy.Files.isFile;
import static com.googlecode.totallylazy.Files.recursiveFiles;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Source.methods.copy;
import static com.googlecode.totallylazy.ZipDestination.zipDestination;
import static javax.tools.StandardLocation.CLASS_PATH;
import static javax.tools.ToolProvider.getSystemJavaCompiler;

public class Compiler {
    public static final Charset UTF8 = Charset.forName("UTF-8");
    private final JavaCompiler compiler;
    private final StandardJavaFileManager standardFileManager;
    private final Sequence<?> options;

    private Compiler(Sequence<?> options, final JavaCompiler javaCompiler) {
        compiler = javaCompiler;
        this.options = options;
        standardFileManager = compiler.getStandardFileManager(null, null, UTF8);
    }

    public static Compiler compiler() {
        return compiler(sequence(Debug));
    }

    public static Compiler compiler(Sequence<?> compileOptions) {
        return compiler(compileOptions, getSystemJavaCompiler());
    }

    public static Compiler compiler(Sequence<?> compileOptions, JavaCompiler javaCompiler) {
        return new Compiler(compileOptions, javaCompiler);
    }

    public Boolean compile(final File source, File destination, final Iterable<File> dependancies) throws IOException {
        setDependencies(dependancies);
        final Pair<Sequence<File>, Sequence<File>> partition = recursiveFiles(source).filter(isFile()).partition(hasSuffix("java"));
        final Sequence<File> javaFiles = partition.first();
        final Sequence<File> nonJava = partition.second();
        return using(new ZipOutputStream(new FileOutputStream(destination)), new Function1<ZipOutputStream, Boolean>() {
            @Override
            public Boolean call(ZipOutputStream output) throws Exception {
                copy(fileSource(source, nonJava), zipDestination(output));
                return compiler.getTask(null, manager(output), null, options.map(toString), null, javaFileObjects(javaFiles)).call();
            }
        });
    }

    private JavaFileManager manager(final ZipOutputStream zipOutputStream) throws FileNotFoundException {
        return new ZipFileManager(standardFileManager, zipOutputStream);
    }

    private Sequence<JavaFileObject> javaFileObjects(Sequence<File> javaFiles) {
        return sequence(standardFileManager.getJavaFileObjectsFromFiles(javaFiles));
    }

    private void setDependencies(Iterable<File> dependancies) throws IOException {
        standardFileManager.setLocation(CLASS_PATH, dependancies);
    }
}