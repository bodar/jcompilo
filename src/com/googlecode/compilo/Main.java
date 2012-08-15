package com.googlecode.compilo;

import com.googlecode.totallylazy.Callables;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Source;
import com.googlecode.totallylazy.callables.TimeCallable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.googlecode.compilo.CompileProcessor.compile;
import static com.googlecode.compilo.Compiler.iterableSource;
import static com.googlecode.totallylazy.Files.files;
import static com.googlecode.totallylazy.Files.name;
import static com.googlecode.totallylazy.Files.relativePath;
import static com.googlecode.totallylazy.Files.workingDirectory;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Sequences.empty;
import static com.googlecode.totallylazy.Sequences.one;
import static com.googlecode.totallylazy.Source.methods.copyAndClose;
import static com.googlecode.totallylazy.Strings.endsWith;
import static java.lang.System.getProperty;
import static java.lang.System.nanoTime;

public class Main {
    public static void main(String[] args) throws Exception {
        long start = nanoTime();
        File buildFile = files(new File(getProperty("user.dir"))).
                find(where(name(), endsWith("uild.java"))).
                getOrElse(Callables.<File>callThrows(new IllegalStateException("Can not find build file")));

        System.out.println("using: " + buildFile.getAbsolutePath());

        String name = relativePath(workingDirectory(), buildFile);
        Source source = source(buildFile, name);

        final MemoryStore compiledBuild = MemoryStore.memoryStore();
        compile(empty(File.class),
                source,
                compiledBuild);

        ClassLoader loader = new ByteClassLoader(compiledBuild.data());
        Class<?> aClass = loader.loadClass(className(name));
        Build build = (Build) aClass.newInstance();
        build.build();

        System.out.println("BUILD SUCCESSFUL");
        System.out.printf("Total time: %s milliseconds%n", TimeCallable.calculateMilliseconds(start, nanoTime()));
    }

    private static Source source(File buildFile, String name) throws IOException {FileInputStream input = new FileInputStream(buildFile);
        Source source = iterableSource(one(Pair.<String, InputStream>pair(name, input)));
        MemoryStore copy = MemoryStore.memoryStore();
        copyAndClose(source, copy);
        input.close();
        return copy;
    }

    private static String className(String build) {
        return build.replace(".java", "").replace('.', '/');
    }
}