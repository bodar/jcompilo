package com.googlecode.compilo;

import com.googlecode.totallylazy.Callables;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Source;
import com.googlecode.totallylazy.callables.TimeCallable;
import com.googlecode.yadic.SimpleContainer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Properties;

import static com.googlecode.compilo.CompileProcessor.compile;
import static com.googlecode.compilo.Compiler.iterableSource;
import static com.googlecode.totallylazy.Files.files;
import static com.googlecode.totallylazy.Files.name;
import static com.googlecode.totallylazy.Files.relativePath;
import static com.googlecode.totallylazy.Files.workingDirectory;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Sequences.empty;
import static com.googlecode.totallylazy.Sequences.one;
import static com.googlecode.totallylazy.Strings.endsWith;
import static java.lang.System.nanoTime;

public class Main {
    private final File root;
    private final Properties properties;
    private final PrintStream out;

    public Main(File root, Properties properties, final PrintStream out) {
        this.root = root;
        this.properties = properties;
        this.out = out;
    }

    public static void main(String[] args) throws Exception {
        new Main(workingDirectory(), System.getProperties(), System.out).build();
    }

    public void build() throws Exception {
        long start = nanoTime();
        File buildFile = buildFile();
        out.println("using: " + buildFile.getAbsolutePath());

        Class<?> aClass = buildClass(buildFile);
        Build build = newBuildClass(aClass);
        build.build();

        out.println("BUILD SUCCESSFUL");
        out.printf("Total time: %s milliseconds%n", TimeCallable.calculateMilliseconds(start, nanoTime()));
    }

    private Build newBuildClass(Class<?> aClass) throws Exception {
        return (Build) new SimpleContainer().
                addInstance(File.class, root).
                addInstance(Properties.class, properties).
                addInstance(PrintStream.class, out).
                create(aClass);
    }

    public Class<?> buildClass(File buildFile) throws Exception {
        String name = relativePath(root, buildFile);

        final MemoryStore compiledBuild = MemoryStore.memoryStore();
        compile(empty(File.class),
                fileSource(buildFile, name),
                compiledBuild);

        ClassLoader loader = new ByteClassLoader(compiledBuild.data());
        return loader.loadClass(className(name));
    }

    public File buildFile() {
        return files(root).
                find(where(name(), endsWith("uild.java"))).
                getOrElse(Callables.<File>callThrows(new IllegalStateException("Can not find build file")));
    }

    private static Source fileSource(File buildFile, String name) throws FileNotFoundException {
        return iterableSource(one(Pair.<String, InputStream>pair(name, new FileInputStream(buildFile))));
    }

    private static String className(String build) {
        return build.replace(".java", "").replace('.', '/');
    }
}