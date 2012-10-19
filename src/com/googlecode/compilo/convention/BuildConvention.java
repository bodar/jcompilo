package com.googlecode.compilo.convention;

import com.googlecode.compilo.Build;
import com.googlecode.compilo.CompileOption;
import com.googlecode.compilo.Environment;
import com.googlecode.compilo.junit.Tests;
import com.googlecode.shavenmaven.PomGenerator;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Zip;

import java.io.File;
import java.io.IOException;

import static com.googlecode.compilo.Compiler.compiler;
import static com.googlecode.compilo.junit.Tests.tests;
import static com.googlecode.totallylazy.Files.delete;
import static com.googlecode.totallylazy.Files.files;
import static com.googlecode.totallylazy.Files.hasSuffix;
import static com.googlecode.totallylazy.Files.name;
import static com.googlecode.totallylazy.Files.recursiveFiles;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Sequences.cons;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.predicates.WherePredicate.where;

public abstract class BuildConvention extends LocationsConvention implements Build {
    protected BuildConvention() {
        this(Environment.constructors.environment());
    }

    protected BuildConvention(Environment environment) {
        super(environment);
    }

    @Override
    public Build build() throws Exception {
        return clean().compile().test().Package();
    }

    @Override
    public Build clean() throws Exception {
        stage("clean");
        env.out().printf("   [delete] Deleting directory: %s%n", artifactsDir());
        delete(artifactsDir());
        return this;
    }

    @Override
    public Build compile() throws Exception {
        stage("compile");
        compiler(env, dependencies(), compileOptions()).compile(srcDir(), mainJar());
        return this;
    }

    @Override
    public Build test() throws Exception {
        stage("test");
        Sequence<File> productionJars = cons(mainJar(), dependencies());
        Tests tests = tests(env, productionJars, testThreads());
        compiler(env, productionJars, compileOptions()).
                add(tests).compile(testDir(), testJar());
        tests.execute(testJar());
        return this;
    }

    @Override
    public int testThreads() {
        return Integer.valueOf(env.properties().getProperty("compilo.test.threads",String.valueOf(Tests.DEFAULT_THREADS)));
    }

    @Override
    public Build Package() throws IOException {
        stage("package");
        Option<File> dependencies = files(buildDir()).find(where(name(), is("runtime.dependencies")));
        if(!dependencies.isEmpty()) {
            env.out().printf("      [pom] Generating pom from: %s%n", dependencies.get());
            PomGenerator.generate(artifactUri(), dependencies, artifactsDir());
        }
        return this;
    }

    private void zip(File source, File destination) throws IOException {
        Number size = Zip.zip(source, destination);
        env.out().printf("      [zip] Zipped %s files: %s%n", size, destination.getAbsoluteFile());
    }

    @Override
    public Iterable<CompileOption> compileOptions() { return sequence(CompileOption.Debug); }

    @Override
    public Iterable<File> dependencies() { return recursiveFiles(libDir()).filter(hasSuffix("jar")).realise(); }

    public Build stage(String name) {
        env.out().println();
        env.out().println(name + ":");
        return this;
    }
}
