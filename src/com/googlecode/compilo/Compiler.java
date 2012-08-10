package com.googlecode.compilo;

import com.googlecode.totallylazy.Files;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Runnables;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Streams;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.googlecode.totallylazy.Closeables.using;
import static com.googlecode.totallylazy.Files.hasSuffix;
import static com.googlecode.totallylazy.Files.isFile;
import static com.googlecode.totallylazy.Files.recursiveFiles;
import static com.googlecode.totallylazy.Sequences.sequence;
import static javax.tools.StandardLocation.CLASS_PATH;
import static javax.tools.ToolProvider.getSystemJavaCompiler;

public class Compiler {
    public static final Charset UTF8 = Charset.forName("UTF-8");
    private final JavaCompiler compiler;
    private final StandardJavaFileManager standardFileManager;

    public Compiler(final JavaCompiler javaCompiler) {
        compiler = javaCompiler;
        standardFileManager = compiler.getStandardFileManager(null, null, UTF8);
    }

    public static Compiler compiler() {
        return new Compiler(getSystemJavaCompiler());
    }

    public Boolean compile(final File source, File destination, final Iterable<File> dependancies) throws IOException {
        setDependencies(dependancies);
        final Pair<Sequence<File>, Sequence<File>> partition = recursiveFiles(source).filter(isFile()).partition(hasSuffix("java"));
        final Sequence<File> javaFiles = partition.first();
        final Sequence<File> nonJava = partition.second();
        return using(new ZipOutputStream(new FileOutputStream(destination)), new Function1<ZipOutputStream, Boolean>() {
            @Override
            public Boolean call(ZipOutputStream output) throws Exception {
                copy(nonJava.map(relativeTo(source)), output);
                return compiler.getTask(null, manager(output), null, null, null, javaFileObjects(javaFiles)).call();
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

    public static Function1<File, Pair<String, File>> relativeTo(final File base) {
        return new Function1<File, Pair<String, File>>() {
            @Override
            public Pair<String, File> call(File file) throws Exception {
                return Pair.pair(Files.relativePath(base, file), file);
            }
        };
    }

    private void copy(Sequence<Pair<String, File>> files, ZipOutputStream output) throws IOException {
        for (Pair<String, File> pair : files) {
            String relativePath = pair.first();
            output.putNextEntry(new ZipEntry(relativePath));
            copyFile(pair.second(), output);
            output.closeEntry();
        }
    }

    private void copyFile(final File source, final OutputStream out) throws IOException {
        using(new FileInputStream(source), new Function1<FileInputStream, Object>() {
            @Override
            public Object call(final FileInputStream in) throws Exception {
                Streams.copy(in, out);
                return Runnables.VOID;
            }
        });
    }
}