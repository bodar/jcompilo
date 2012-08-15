package com.googlecode.compilo;

import com.googlecode.totallylazy.Callables;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Source;

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

public class Main {
    public static void main(String[] args) throws Exception {
        File buildFile = files(workingDirectory()).
                find(where(name(), endsWith("Build.java"))).
                getOrElse(Callables.<File>callThrows(new IllegalStateException("Can not find *Build.java")));

        System.out.println("Buildfile:" + buildFile.getAbsolutePath());

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