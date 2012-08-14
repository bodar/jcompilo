package com.googlecode.compilo;

import com.googlecode.totallylazy.Destination;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Methods;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Source;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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

public class TestProcessor implements Processor {
    public static final int DEFAULT_THREADS = Runtime.getRuntime().availableProcessors();
    private final List<String> tests = new ArrayList<String>();
    private final Predicate<? super String> predicate;

    private TestProcessor(Predicate<? super String> predicate) {
        this.predicate = predicate;
    }

    public static TestProcessor testProcessor() {
        return testProcessor(endsWith("Test.java"));
    }

    public static TestProcessor testProcessor(Predicate<? super String> predicate) {
        return new TestProcessor(predicate);
    }

    @Override
    public String name() {
        return "Testing";
    }

    @Override
    public Integer call(Source source, Destination destination) throws Exception {
        return source.sources().size();
    }

    @Override
    public boolean matches(String other) {
        boolean matched = predicate.matches(other);
        if (matched) tests.add(other);
        return matched;
    }

    public Sequence<String> tests() {
        return sequence(tests);
    }

    public boolean execute(Sequence<File> dependencies) throws Exception {
        return execute(dependencies, DEFAULT_THREADS);
    }

    public boolean execute(Sequence<File> dependencies, int numberOfThreads) throws MalformedURLException, FileNotFoundException, ClassNotFoundException {
        final URLClassLoader classLoader = new URLClassLoader(asUrls(dependencies.cons(testExecutor())), null);

        Class<?> executor = classLoader.loadClass(TestExecutor.class.getName());
        Method execute = Methods.method(executor, "execute", List.class, int.class).get();
        return Methods.<TestExecutor, Boolean>invoke(execute, null, tests().toList(), numberOfThreads);
    }

    private File testExecutor() throws FileNotFoundException {
        File testExecutor = file(temporaryDirectory(), "compilo.test.hook.jar");
        Destination destination = zipDestination(new FileOutputStream(testExecutor));

        Source source = classesSource(TestExecutor.class, TestExecutor.ResultCallable.class, TestExecutor.NullOutputStream.class, TestExecutor.NullPrintStream.class);
        Source.methods.copyAndClose(source, destination);
        return testExecutor;
    }

    private static URL[] asUrls(Sequence<File> jars) throws MalformedURLException {
        return jars.map(new Function1<File, URL>() {
            @Override
            public URL call(File file) throws Exception {
                return file.toURI().toURL();
            }
        }).toArray(URL.class);
    }

}
