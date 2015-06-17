package com.googlecode.jcompilo.convention;

import com.googlecode.jcompilo.Build;
import com.googlecode.jcompilo.CompileOption;
import com.googlecode.jcompilo.Environment;
import com.googlecode.jcompilo.tests.Tests;
import com.googlecode.shavenmaven.PomGenerator;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Zip;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;

import static com.googlecode.jcompilo.Compiler.CPUS;
import static com.googlecode.jcompilo.Compiler.compiler;
import static com.googlecode.jcompilo.MoveToTL.write;
import static com.googlecode.jcompilo.convention.ReleaseFile.constructors.releaseFile;
import static com.googlecode.jcompilo.convention.ReleaseFile.functions.file;
import static com.googlecode.jcompilo.tests.Tests.tests;
import static com.googlecode.totallylazy.Callers.callConcurrently;
import static com.googlecode.totallylazy.Closeables.using;
import static com.googlecode.totallylazy.Files.*;
import static com.googlecode.totallylazy.Functions.and;
import static com.googlecode.totallylazy.Option.some;
import static com.googlecode.totallylazy.Sequences.cons;
import static com.googlecode.totallylazy.Sequences.sequence;
import static java.lang.Boolean.parseBoolean;
import static java.lang.String.format;

public abstract class BuildConvention extends LocationsConvention implements Build {
    protected BuildConvention() {
        this(Environment.constructors.environment());
    }

    protected BuildConvention(Environment environment) {
        super(environment);
    }

    @Override
    public boolean build() throws Exception {
        return sequence(
                clean(),
                compile(),
                parallel(test(this), Package(this))).
                reduce(and);
    }

    @Override
    public boolean clean() throws Exception {
        stage("clean");
        env.out().printf("   [delete] Deleting directory: %s%n", artifactsDir());
        return delete(artifactsDir());
    }

    @Override
    public boolean compile() throws Exception {
        stage("compile");
        return compiler(env, dependencies(), compileOptions()).
                compile(srcDir(), mainJar());
    }

    @Override
    public boolean test() throws Exception {
        stage("test");
        Sequence<File> productionJars = cons(mainJar(), dependencies());
        Tests tests = tests(env, productionJars, testThreads(), reportsDir(), debug());
        return compiler(env, productionJars, compileOptions()).
                add(tests).compile(testDir(), testJar()) &&
                tests.execute(testJar());
    }

    @Override
    public boolean Package() throws IOException {
        stage("package");
        zip(srcDir(), sourcesJar());
        zip(testDir(), testSourcesJar());
        generateReleaseProperties();
        generatePom();
        return true;
    }

    @Override
    public Iterable<CompileOption> compileOptions() {
        return sequence(CompileOption.Debug);
    }

    @Override
    public Iterable<File> dependencies() {
        return recursiveFiles(libDir()).filter(hasSuffix("jar")).realise();
    }

    protected boolean debug() {
        return parseBoolean(env.properties().getProperty("jcompilo.debug"));
    }

    protected int testThreads() {
        return Integer.valueOf(env.properties().getProperty("jcompilo.test.threads", String.valueOf(CPUS)));
    }

    protected void generateReleaseProperties() throws IOException {
        final Properties release = new Properties();
        release.setProperty("project.name", artifact());
        release.setProperty("release.version", version());
        release.setProperty("release.name", versionedArtifact());
        release.setProperty("release.path", releasePath());
        Properties commit = lastCommitData();
        Sequence<ReleaseFile> releaseFiles = sequence(releaseFiles(commit));
        release.setProperty("release.files", releaseFiles.map(file).map(name()).toString(","));
        for (ReleaseFile releaseFile : releaseFiles) {
            release.setProperty(format("%s.description", releaseFile.file().getName()), releaseFile.description());
            release.setProperty(format("%s.labels", releaseFile.file().getName()), sequence(releaseFile.labels()).toString(","));
        }

        for (Map.Entry<Object, Object> entry : commit.entrySet()) {
            release.put("commit." + entry.getKey(), entry.getValue());
        }

        using(new FileWriter(releaseProperties()), write(release));
    }

    protected void generatePom() {
        File dependencies = runtimeDependencies();
        if (dependencies.exists()) {
            env.out().printf("      [pom] Generating pom from: %s%n", dependencies);
            PomGenerator.generate(artifactUri(), some(dependencies), artifactsDir());
        }
    }

    protected File releaseProperties() {
        return new File(artifactsDir(), "release.properties");
    }

    protected File runtimeDependencies() {
        return new File(buildDir(), "runtime.dependencies");
    }

    protected File pomFile() {
        return new File(artifactsDir(), format("%s.pom", versionedArtifact()));
    }

    protected Iterable<ReleaseFile> releaseFiles(Properties commit) {
        return sequence(
                releaseFile(mainJar(), format("%s build:%s changeset:%s user:%s", commit.getProperty("summary"), version(), commit.getProperty("changeset"), commit.getProperty("user")), "Jar"),
                releaseFile(pomFile(), format("Maven POM file build:%s", version()), "POM"),
                releaseFile(sourcesJar(), format("Source file build:%s", version()), "Source"),
                releaseFile(testJar(), format("Test jar build:%s", version()), "Test", "Jar"),
                releaseFile(testSourcesJar(), format("Test source file build:%s", version()), "Test", "Source")
        );
    }

    protected Properties lastCommitData() throws IOException {
        return LastCommit.lastCommitData(rootDir());
    }

    protected void zip(File source, File destination) throws IOException {
        Number size = Zip.zip(source, destination);
        env.out().printf("      [zip] Zipped %s files: %s%n", size, destination.getAbsoluteFile());
    }

    public Build stage(String name) {
        env.out().println();
        env.out().println(name + ":");
        return this;
    }

    public final boolean parallel(Callable<Boolean> stage1, Callable<Boolean> stage2) {
        return parallel(sequence(stage1, stage2));
    }

    public final boolean parallel(Iterable<Callable<Boolean>> stages) {
        return callConcurrently(stages).reduce(and);
    }

    public static Callable<Boolean> compile(final Build build) {
        return () -> build.compile();
    }

    public static Callable<Boolean> test(final Build build) {
        return () -> build.test();
    }

    public static Callable<Boolean> Package(final Build build) {
        return () -> build.Package();
    }
}
