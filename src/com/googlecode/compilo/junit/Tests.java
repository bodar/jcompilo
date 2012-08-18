package com.googlecode.compilo.junit;

import com.googlecode.compilo.FileUrls;
import com.googlecode.compilo.Processor;
import com.googlecode.totallylazy.Destination;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Methods;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Source;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
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

    private Tests(Predicate<? super String> predicate, Sequence<File> dependencies, int threads) {
        this.predicate = predicate;
        this.dependencies = dependencies;
        this.numberOfThreads = threads;
    }

    public static Tests tests(final Sequence<File> dependencies) {
        return tests(dependencies, DEFAULT_THREADS);
    }

    public static Tests tests(final Sequence<File> dependencies, final int threads) {
        return tests(dependencies, threads, endsWith("Test.java"));
    }

    public static Tests tests(final Sequence<File> dependencies, final int threads, Predicate<? super String> predicate) {
        return new Tests(predicate, dependencies, threads);
    }

    @Override
    public String call(Source source, Destination destination) throws Exception {
        source.sources().size();
        return "";
    }

    @Override
    public boolean matches(String other) {
        boolean matched = predicate.matches(other);
        if (matched) tests.add(other);
        return matched;
    }

    public void execute(File testJar, PrintStream out) throws FileNotFoundException, ClassNotFoundException {
        final URLClassLoader classLoader = new URLClassLoader(FileUrls.asUrls(dependencies.cons(testJar).cons(testExecutor())), null);

        Class<?> executor = classLoader.loadClass(TestExecutor.class.getName());
        Method execute = Methods.method(executor, "execute", List.class, int.class, PrintStream.class).get();
        out.printf("    [junit] Running %s tests on %s threads%n", tests.size(), numberOfThreads);
        Boolean success = Methods.<TestExecutor, Boolean>invoke(execute, null, tests, numberOfThreads, out);
        if(!success) throw new IllegalStateException("BUILD FAILED");
    }

    private File testExecutor() throws FileNotFoundException {
        File testExecutor = file(temporaryDirectory(), "compilo.test.hook.jar");
        Destination destination = zipDestination(new FileOutputStream(testExecutor));

        Source source = classesSource(TestExecutor.class, TestExecutor.ResultCallable.class, TestExecutor.NullOutputStream.class, TestExecutor.NullPrintStream.class);
        Source.methods.copyAndClose(source, destination);
        return testExecutor;
    }

}