package com.googlecode.compilo;

import com.googlecode.totallylazy.Files;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Runnables;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Streams;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.ForwardingJavaFileObject;
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

    public Boolean compile(File source, File destination, final Iterable<File> dependancies) throws IOException {
        Pair<Sequence<File>, Sequence<File>> partition = recursiveFiles(source).filter(isFile()).partition(hasSuffix("java"));
        Sequence<File> javaFiles = partition.first();
        Sequence<File> nonJava = partition.second();
        copy(nonJava.map(relativeTo(source)), destination);
        setDependencies(dependancies);
        return compiler.getTask(null, manager(destination), null, null, null, javaFileObjects(javaFiles)).call();
    }

    private JavaFileManager manager(File destination) throws FileNotFoundException {
        if(destination.isDirectory()) return standardFileManager;
        return new ZipFileManager(standardFileManager, destination);
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

    private void copy(Sequence<Pair<String, File>> files, File directory) throws IOException {
        for (Pair<String, File> pair : files) {
            String relativePath = pair.first();
            File source = pair.second();
            File destination = new File(directory, relativePath);
            destination.getParentFile().mkdirs();
            copyFile(source, destination);
        }
    }

    private void copyFile(final File source, final File destination) throws IOException {
        using(new FileInputStream(source), new Function1<FileInputStream, Object>() {
            @Override
            public Object call(final FileInputStream in) throws Exception {
                return using(new FileOutputStream(destination), new Function1<FileOutputStream, Object>() {
                    @Override
                    public Object call(FileOutputStream out) throws Exception {
                        Streams.copy(in, out);
                        return Runnables.VOID;
                    }
                });
            }
        });
    }

    private static class ZipFileManager extends ForwardingJavaFileManager<JavaFileManager> {
        private final ZipOutputStream outputStream;

        public ZipFileManager(final JavaFileManager fileManager, File destination) throws FileNotFoundException {
            super(fileManager);
            outputStream = new ZipOutputStream(new FileOutputStream(destination));
        }

        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
            return new ZipFileObject(super.getJavaFileForOutput(location, className, kind, sibling), outputStream);
        }

        @Override
        public void flush() throws IOException {
            outputStream.flush();
            outputStream.close();
        }
    }

    private static class ZipFileObject extends ForwardingJavaFileObject<JavaFileObject> {
        private final JavaFileObject output;
        private final ZipOutputStream outputStream;

        public ZipFileObject(JavaFileObject output, ZipOutputStream outputStream) {
            super(output);
            this.output = output;
            this.outputStream = outputStream;
        }

        @Override
        public OutputStream openOutputStream() throws IOException {
            outputStream.putNextEntry(new ZipEntry(output.getName()));
            return new OutputStream() {
                @Override
                public void write(int b) throws IOException {
                    outputStream.write(b);
                }

                @Override
                public void close() throws IOException {
                    outputStream.closeEntry();
                }
            };
        }


    }

}
