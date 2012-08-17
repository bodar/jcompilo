package com.googlecode.compilo;

import com.googlecode.compilo.convention.AutoBuild;
import com.googlecode.shavenmaven.Dependencies;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
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
import static com.googlecode.totallylazy.Files.directory;
import static com.googlecode.totallylazy.Files.files;
import static com.googlecode.totallylazy.Files.hasSuffix;
import static com.googlecode.totallylazy.Files.name;
import static com.googlecode.totallylazy.Files.relativePath;
import static com.googlecode.totallylazy.Files.workingDirectory;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Sequences.empty;
import static com.googlecode.totallylazy.Sequences.one;
import static com.googlecode.totallylazy.Strings.endsWith;
import static java.lang.System.nanoTime;

public class BootStrap {
    private final File root;
    private final Properties properties;
    private final PrintStream out;

    public BootStrap(File root, Properties properties, final PrintStream out) {
        this.root = root;
        this.properties = properties;
        this.out = out;
    }

    public static void main(String[] args) throws Exception {
        new BootStrap(workingDirectory(), System.getProperties(), System.out).build();
    }

    public void build() throws Exception {
        long start = nanoTime();
        loadLibs(root);
        Class<?> buildClass = findBuildClass();
        out.printf("buildfile: %s%n", buildClass);
        Build build = createBuildClass(buildClass);
        build.build();

        out.println("BUILD SUCCESSFUL");
        out.printf("Total time: %s milliseconds%n", TimeCallable.calculateMilliseconds(start, nanoTime()));
    }

    private void loadLibs(File root) {
        Sequence<File> dependencies = files(directory(root, "build")).filter(hasSuffix("dependencies"));
        if(dependencies.isEmpty()) return;
        out.printf("update:%n");
        final File libDir = directory(root, "lib");
        dependencies.mapConcurrently(new Function1<File, Boolean>() {
            @Override
            public Boolean call(File file) throws Exception {
                return  Dependencies.load(file).update(directory(libDir, file.getName().replace(".dependencies", "")));
            }
        }).realise();
    }

    private Build createBuildClass(Class<?> aClass) throws Exception {
        return (Build) new SimpleContainer().
                addInstance(File.class, root).
                addInstance(Properties.class, properties).
                addInstance(PrintStream.class, out).
                create(aClass);
    }

    public Class<?> findBuildClass() throws Exception {
        return buildFile().map(new Function1<File, Class<?>>() {
            @Override
            public Class<?> call(File buildFile) throws Exception {
                String name = relativePath(root, buildFile);

                final MemoryStore compiledBuild = MemoryStore.memoryStore();
                compile(empty(File.class),
                        fileSource(buildFile, name),
                        compiledBuild);

                ClassLoader loader = new ByteClassLoader(compiledBuild.data());
                return loader.loadClass(className(name));

            }
        }).getOrElse(AutoBuild.class);

    }

    public Option<File> buildFile() {
        return files(root).
                find(where(name(), endsWith("uild.java")));
    }

    private static Source fileSource(File buildFile, String name) throws FileNotFoundException {
        return iterableSource(one(Pair.<String, InputStream>pair(name, new FileInputStream(buildFile))));
    }

    private static String className(String build) {
        return build.replace(".java", "").replace('.', '/');
    }
}