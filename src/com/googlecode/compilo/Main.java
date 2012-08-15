package com.googlecode.compilo;

import com.googlecode.totallylazy.Callables;
import com.googlecode.totallylazy.Pair;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;

import static com.googlecode.compilo.CompileProcessor.compile;
import static com.googlecode.compilo.Compiler.iterableSource;
import static com.googlecode.totallylazy.Files.files;
import static com.googlecode.totallylazy.Files.name;
import static com.googlecode.totallylazy.Files.relativePath;
import static com.googlecode.totallylazy.Files.temporaryFile;
import static com.googlecode.totallylazy.Files.workingDirectory;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Sequences.empty;
import static com.googlecode.totallylazy.Sequences.one;
import static com.googlecode.totallylazy.Strings.endsWith;
import static com.googlecode.totallylazy.ZipDestination.zipDestination;

public class Main {
    public static void main(String[] args) throws Exception {
        File buildFile = files(workingDirectory()).
                find(where(name(), endsWith("Build.java"))).
                getOrElse(Callables.<File>callThrows(new IllegalStateException("Can not find *Build.java")));

        String name = relativePath(workingDirectory(), buildFile);
        File buildJar = temporaryFile();

        compile(empty(File.class),
                iterableSource(one(Pair.<String, InputStream>pair(name, new FileInputStream(buildFile)))),
                zipDestination(new FileOutputStream(buildJar)));

        URLClassLoader classLoader = new URLClassLoader(new URL[]{buildJar.toURI().toURL()});
        Class<?> aClass = classLoader.loadClass(className(name));
        Build build = (Build) aClass.newInstance();
        build.build();
    }

    private static String className(String build) {
        return build.replace(".java", "").replace('.', '/');
    }
}