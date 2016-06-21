package com.googlecode.jcompilo.tests.junit;

import com.googlecode.totallylazy.Lists;
import com.googlecode.totallylazy.time.SystemClock;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.googlecode.jcompilo.tests.LocalPrintStream.localPrintStream;

public class TestExecutor {
    public static void main(String[] arguments) {
        try {
            List<String> values = Lists.list(arguments);
            boolean success = execute(values.subList(2, values.size()), Integer.valueOf(values.get(0)), System.out, new File(values.get(1)));
            System.exit(success ? 0 : -1);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }
    }

    public static boolean execute(final List<String> testNames, final int numberOfThreads, PrintStream out, File directory) throws Exception {
        System.setOut(localPrintStream);
        System.setErr(localPrintStream);

        Result result = execute(asClasses(testNames), numberOfThreads, directory);
        out.print(result);

        boolean success = result.wasSuccessful();
        if (!success) {
            out.printf("%s tests failed:%n%n", result.getFailureCount());
            for (Failure failure : result.getFailures()) {
                Description description = failure.getDescription();
                Throwable throwable = failure.getException();
                out.printf("%s.%s %s%n", description.getTestClass().getSimpleName(), description.getMethodName(), throwable.getClass().getName());
            }
        }

        return success;
    }

    public static Result execute(Class<?>[] classes, int numberOfThreads, File directory) throws InterruptedException, ClassNotFoundException {
        Result result = new Result();
        execute(numberOfThreads, tests(result, classes, directory));
        return result;
    }

    private static JUnitCore junit(Result result, File reportDirectory) {
        JUnitCore junit = new JUnitCore();
        junit.addListener(result.createListener());
        junit.addListener(new AntTestOutput(reportDirectory, System.getProperties(), new SystemClock()));
        return junit;
    }

    private static List<Callable<Result>> tests(Result result, final Class<?>[] classes, File directory) throws ClassNotFoundException {
        List<Callable<Result>> tests = new ArrayList<Callable<Result>>();
        for (Class<?> testClass : classes) {
            tests.add(new ResultCallable(result, testClass, directory));
        }
        return tests;
    }

    @SuppressWarnings("unchecked")
    private static void execute(int numberOfThreads, final List<? extends Callable<?>> tests) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        executorService.invokeAll((Collection<? extends Callable<Object>>) tests);
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.DAYS);
    }

    public static Class<?>[] asClasses(List<String> fileNames) throws ClassNotFoundException {
        Class<?>[] result = new Class<?>[fileNames.size()];
        for (int i = 0; i < fileNames.size(); i++) {
            result[i] = Class.forName(className(fileNames.get(i)));
        }
        return result;
    }

    public static String className(String filename) {
        return filename.replace('/', '.').replaceFirst("\\.java$", "");
    }

    static class ResultCallable implements Callable<Result> {
        private final Result result;
        private final Class<?> testClass;
        private final File directory;

        public ResultCallable(Result result, Class<?> testClass, File directory) {
            this.result = result;
            this.testClass = testClass;
            this.directory = directory;
        }

        @Override
        public Result call() {
            return junit(result, directory).run(testClass);
        }
    }
}
