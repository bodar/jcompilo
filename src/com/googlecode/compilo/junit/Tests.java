package com.googlecode.compilo.junit;

import com.googlecode.compilo.Environment;
import com.googlecode.compilo.Inputs;
import com.googlecode.compilo.Outputs;
import com.googlecode.compilo.Processor;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Streams;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.googlecode.compilo.BootStrap.jarFile;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Strings.endsWith;
import static java.io.File.pathSeparator;

public class Tests implements Processor {
    public static final int DEFAULT_THREADS = Runtime.getRuntime().availableProcessors();
    private final List<String> tests = new ArrayList<String>();
    private final Predicate<? super String> predicate;
    private final Sequence<File> dependencies;
    private final int numberOfThreads;
    private final Environment environment;

    private Tests(Environment environment, Sequence<File> dependencies, int threads, Predicate<? super String> predicate) {
        this.environment = environment;
        this.dependencies = dependencies;
        this.numberOfThreads = threads;
        this.predicate = predicate;
    }

    public static Tests tests(Environment env, final Sequence<File> dependencies) {
        return tests(env, dependencies, DEFAULT_THREADS);
    }

    public static Tests tests(Environment env, final Sequence<File> dependencies, final int threads) {
        return tests(env, dependencies, threads, endsWith("Test.java"));
    }

    public static Tests tests(Environment env, final Sequence<File> dependencies, final int threads, Predicate<? super String> predicate) {
        return new Tests(env, dependencies, threads, predicate);
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
            List<String> arguments = sequence("java", "-cp", dependencies.cons(testJar).cons(jarFile(getClass())).toString(pathSeparator),
                    "com.googlecode.compilo.junit.TestExecutor", String.valueOf(numberOfThreads)).toList();
            arguments.addAll(sequence(tests).toList());
            ProcessBuilder builder = new ProcessBuilder(arguments);
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
}