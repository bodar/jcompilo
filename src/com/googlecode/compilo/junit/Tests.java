package com.googlecode.compilo.junit;

import com.googlecode.compilo.Environment;
import com.googlecode.compilo.FileUrls;
import com.googlecode.compilo.Processor;
import com.googlecode.totallylazy.Destination;
import com.googlecode.totallylazy.Methods;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Source;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import static com.googlecode.compilo.ClassesSource.classesSource;
import static com.googlecode.totallylazy.Files.file;
import static com.googlecode.totallylazy.Files.temporaryDirectory;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Strings.endsWith;
import static com.googlecode.totallylazy.ZipDestination.zipDestination;
import static java.lang.String.format;

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
    public Boolean call(Source source, Destination destination) throws Exception {
        source.sources().size();
        return true;
    }

    @Override
    public boolean matches(String other) {
        boolean matched = predicate.matches(other);
        if (matched) tests.add(other);
        return matched;
    }

    public void execute(File testJar) throws FileNotFoundException, ClassNotFoundException {
        environment.out().prefix("    [junit] ");
        final URLClassLoader classLoader = new URLClassLoader(FileUrls.asUrls(dependencies.cons(testJar).cons(testExecutor())), null);

        Class<?> executor = classLoader.loadClass(TestExecutor.class.getName());
        Method execute = Methods.method(executor, "execute", List.class, int.class, PrintStream.class).get();
        environment.out().printf("Running %s tests classes on %s threads%n", tests.size(), numberOfThreads);
        Boolean success = Methods.<TestExecutor, Boolean>invoke(execute, null, tests, numberOfThreads, environment.out());
        environment.out().clearPrefix();
        if(!success) throw new IllegalStateException("Tests failed");
    }

    private File testExecutor() throws FileNotFoundException {
        File testExecutor = file(temporaryDirectory(), "compilo.test.hook.jar");
        Destination destination = zipDestination(new FileOutputStream(testExecutor));

        Source source = classesSource(TestExecutor.class, TestExecutor.ResultCallable.class, TestExecutor.NullOutputStream.class, TestExecutor.NullPrintStream.class);
        Source.methods.copyAndClose(source, destination);
        return testExecutor;
    }

}