package com.googlecode.jcompilo.convention;

import com.googlecode.jcompilo.Build;
import com.googlecode.jcompilo.CompileOption;
import com.googlecode.jcompilo.Environment;
import com.googlecode.jcompilo.Processes;
import com.googlecode.jcompilo.junit.Tests;
import com.googlecode.shavenmaven.PomGenerator;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Zip;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.Callable;

import static com.googlecode.jcompilo.Compiler.CPUS;
import static com.googlecode.jcompilo.Compiler.compiler;
import static com.googlecode.jcompilo.MoveToTL.write;
import static com.googlecode.jcompilo.convention.ReleaseFile.constructors.releaseFile;
import static com.googlecode.jcompilo.convention.ReleaseFile.functions.file;
import static com.googlecode.jcompilo.junit.Tests.tests;
import static com.googlecode.totallylazy.Callers.callConcurrently;
import static com.googlecode.totallylazy.Closeables.using;
import static com.googlecode.totallylazy.Files.delete;
import static com.googlecode.totallylazy.Files.hasSuffix;
import static com.googlecode.totallylazy.Files.recursiveFiles;
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
    public Build build() throws Exception {
        clean().compile();
        return parallel(test(this), Package(this));
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
        compiler(env, dependencies(), compileOptions()).
                compile(srcDir(), mainJar());
        return this;
    }

    @Override
    public Build test() throws Exception {
        stage("test");
        Sequence<File> productionJars = cons(mainJar(), dependencies());
        Tests tests = tests(env, productionJars, testThreads(), reportsDir(), debug());
        compiler(env, productionJars, compileOptions()).
                add(tests).compile(testDir(), testJar());
        tests.execute(testJar());
        return this;
    }

    @Override
    public Build Package() throws IOException {
        stage("package");
        zip(srcDir(), sourcesJar());
        zip(testDir(), testSourcesJar());
        generateReleaseProperties();
        generatePom();
        return this;
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
        Sequence<ReleaseFile> releaseFiles = sequence(releaseFiles(lastCommitData()));
        release.setProperty("release.files", releaseFiles.map(file).toString(","));
        for (ReleaseFile releaseFile : releaseFiles) {
            release.setProperty(format("%s.description", releaseFile.file()), releaseFile.description());
            release.setProperty(format("%s.labels", releaseFile.file()), sequence(releaseFile.labels()).toString(","));
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
        return new File(artifactsDir(), format("%s.pom", artifact()));
    }

    protected Iterable<ReleaseFile> releaseFiles(Properties lastCommitData) {
        return sequence(
                releaseFile(mainJar(), format("%s build:%s", lastCommitData.getProperty("summary"), version()), "Jar"),
                releaseFile(pomFile(), format("Maven POM file build:%s", version()), "POM"),
                releaseFile(sourcesJar(), format("Source file build:%s", version()), "Source"),
                releaseFile(testJar(), format("Test jar build:%s", version()), "Test", "Jar"),
                releaseFile(testSourcesJar(), format("Test source file build:%s", version()), "Test", "Source")
        );
    }

    protected Properties lastCommitData() throws IOException {
        Properties properties = new Properties();
        if (new File(rootDir(), ".hg").exists()) {
            InputStream output = Processes.exec(rootDir(), "hg log -l 1");
            properties.load(output);
        }
        return properties;
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

    public Build parallel(Callable<? extends Build> stage1, Callable<? extends Build> stage2) {
        return parallel(sequence(stage1, stage2));
    }

    public Build parallel(Callable<? extends Build> stage1, Callable<? extends Build> stage2, Callable<? extends Build> stage3) {
        return parallel(sequence(stage1, stage2, stage3));
    }

    public Build parallel(Iterable<Callable<? extends Build>> stages) {
        callConcurrently(stages).realise();
        return this;
    }

    public static Callable<Build> compile(final Build build) {
        return new Callable<Build>() {
            @Override
            public Build call() throws Exception {
                return build.compile();
            }
        };
    }

    public static Callable<Build> test(final Build build) {
        return new Callable<Build>() {
            @Override
            public Build call() throws Exception {
                return build.test();
            }
        };
    }

    public static Callable<Build> Package(final Build build) {
        return new Callable<Build>() {
            @Override
            public Build call() throws Exception {
                return build.Package();
            }
        };
    }
}
