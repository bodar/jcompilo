package com.googlecode.compilo.junit;

import com.googlecode.compilo.*;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Streams;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.googlecode.compilo.BootStrap.jarFile;
import static com.googlecode.compilo.Compiler.CPUS;
import static com.googlecode.totallylazy.Sequences.cons;
import static com.googlecode.totallylazy.Sequences.empty;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Strings.endsWith;
import static java.io.File.pathSeparator;

public class Tests implements Processor {
    public static final Sequence<String> debugJvm = sequence("-Xdebug", "-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005");
    private final List<String> tests = new ArrayList<String>();
    private final Predicate<? super String> predicate;
    private final Sequence<File> dependencies;
    private final Environment environment;
    private final int numberOfThreads;
    private final boolean debug;
    private final File reportsDirectory;

    private Tests(Environment environment, Sequence<File> dependencies, int threads, File reportsDirectory, Predicate<? super String> predicate, boolean debug) {
        this.environment = environment;
        this.dependencies = dependencies;
        this.numberOfThreads = threads;
        this.predicate = predicate;
        this.debug = debug;
        this.reportsDirectory = reportsDirectory;
    }

    public static Tests tests(Environment env, final Sequence<File> dependencies, final File reportsDirectory) {
        return tests(env, dependencies, CPUS, reportsDirectory, false);
    }

    public static Tests tests(Environment env, final Sequence<File> dependencies, final int threads, final File reportsDirectory, final boolean debug) {
        return tests(env, dependencies, threads, reportsDirectory, endsWith("Test.java"), debug);
    }

    public static Tests tests(Environment env, final Sequence<File> dependencies, final int threads, final File reportsDirectory, Predicate<? super String> predicate, final boolean debug) {
        return new Tests(env, dependencies, threads, reportsDirectory, predicate, debug);
    }

    @Override
    public boolean process(Inputs inputs, Outputs outputs) throws Exception {
        return true;
    }

    @Override
    public boolean matches(String other) {
        boolean matched = predicate.matches(other);
        if (matched) tests.add(other);
        return matched;
    }

    public void execute(File testJar) throws Exception {
        try {
            environment.out().prefix("    [junit] ");
            environment.out().printf("Running %s tests classes on %s threads%n", tests.size(), numberOfThreads);
            List<String> arguments = cons(javaProcess(), debug().join(sequence("-cp", dependencies.cons(testJar).cons(jarFile(getClass())).toString(pathSeparator),
                    "com.googlecode.compilo.junit.TestExecutor", String.valueOf(numberOfThreads), reportsDirectory.toString()))).toList();
            arguments.addAll(sequence(tests).toList());
            ProcessBuilder builder = new ProcessBuilder(arguments);
            builder.redirectErrorStream(true);
            builder.directory(environment.workingDirectory());
            Process process = builder.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                Streams.copy(process.getInputStream(), environment.out());
                throw new IllegalStateException("Tests failed");
            }
        } finally {
            environment.out().clearPrefix();

        }
    }

    private String javaProcess() {
        return environment.properties().getProperty("java.home") + "/bin/java";
    }

    private Sequence<String> debug() {
        if (debug) {
            environment.out().println("Debugging tests running with " + debugJvm.toString(" "));
            return debugJvm;
        }
        else return empty(String.class);
    }
}