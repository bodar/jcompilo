package com.googlecode.compilo.convention;

import com.googlecode.compilo.Build;
import com.googlecode.compilo.CompileOption;
import com.googlecode.compilo.Tests;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Zip;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Properties;

import static com.googlecode.compilo.Compiler.compiler;
import static com.googlecode.compilo.Tests.tests;
import static com.googlecode.totallylazy.Files.delete;
import static com.googlecode.totallylazy.Files.hasSuffix;
import static com.googlecode.totallylazy.Files.recursiveFiles;
import static com.googlecode.totallylazy.Files.workingDirectory;
import static com.googlecode.totallylazy.Sequences.cons;
import static com.googlecode.totallylazy.Sequences.sequence;

public abstract class BuildConvention extends LocationsConvention implements Build {
    protected final PrintStream out;

    protected BuildConvention() {
        this(workingDirectory(), System.getProperties(), System.out);
    }

    protected BuildConvention(File root, Properties properties, PrintStream out) {
        super(root, properties);
        this.out = out;
    }

    @Override
    public Build build() throws Exception {
        stage("build");
        return clean().compile().test().Package();
    }

    @Override
    public Build clean() throws Exception {
        stage("clean");
        delete(artifactsDir()); return this;
    }

    @Override
    public Build compile() throws Exception {
        stage("compile");
        compiler(dependencies(), compileOptions()).compile(srcDir(), mainJar());
        return this;
    }

    @Override
    public Build test() throws Exception {
        stage("test");
        Tests tests = tests();
        Sequence<File> productionJars = cons(mainJar(), dependencies());
        compiler(productionJars, compileOptions()).
                add(tests).compile(testDir(), testJar());
        tests.execute(cons(testJar(), productionJars), testThreads());
        return this;
    }

    @Override
    public int testThreads() {
        return Tests.DEFAULT_THREADS;
    }

    @Override
    public Build Package() throws IOException {
        stage("package");
        zip(srcDir(), sourcesJar());
//        zip(testDir(), testSourcesJar());
        return this;
    }

    private void zip(File source, File destination) throws IOException {
        out.printf("[zip] %s file(s): %s%n", Zip.zip(source, destination), destination.getAbsoluteFile());
    }

    @Override
    public Iterable<CompileOption> compileOptions() { return sequence(CompileOption.Debug); }

    @Override
    public Iterable<File> dependencies() { return recursiveFiles(libDir()).filter(hasSuffix("jar")).realise(); }

    public Build stage(String name) {out.println(name + ":"); return this; }
}
