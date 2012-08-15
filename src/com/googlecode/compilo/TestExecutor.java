package com.googlecode.compilo;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestExecutor {
    public static boolean execute(final List<String> testNames, final int numberOfThreads) throws Exception {
        PrintStream original = System.out;
        System.setOut(nullPrintStream());
        JUnitCore junit = new JUnitCore();
        Result result = new Result();
        junit.addListener(result.createListener());

        execute(asClasses(testNames), numberOfThreads, junit);
        System.setOut(original);

        boolean success = result.wasSuccessful();
        if (!success) {
            System.out.printf("%d%n Tests FAILED", result.getFailureCount());
            for (Failure failure : result.getFailures()) {
                System.out.println(failure.getTestHeader());
                System.out.println(failure.getTrace());
            }
        }

        return success;
    }

    private static void execute(Class<?>[] classes, int numberOfThreads, JUnitCore junit) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

        List<Callable<Result>> tests = new ArrayList<Callable<Result>>();
        for (Class<?> testClass : classes) {
            tests.add(test(junit, testClass));
        }

        executorService.invokeAll(tests);
        executorService.shutdown();
    }

    private static PrintStream nullPrintStream() {
        return new NullPrintStream();
    }

    private static Callable<Result> test(final JUnitCore junit, final Class<?> testClass) {
        return new ResultCallable(junit, testClass);
    }

    public static Class<?>[] asClasses(List<String> fileNames) throws ClassNotFoundException {
        Class<?>[] result = new Class<?>[fileNames.size()];
        for (int i = 0; i < fileNames.size(); i++) {
            result[i] = Class.forName(className(fileNames.get(i)));
        }
        return result;
    }

    private static String className(String filename) {
        return filename.replace('/', '.').replace(".java", "");
    }


    static class ResultCallable implements Callable<Result> {
        private final JUnitCore junit;
        private final Class<?> testClass;

        public ResultCallable(JUnitCore junit, Class<?> testClass) {
            this.junit = junit;
            this.testClass = testClass;
        }

        @Override
        public Result call() {
            return junit.run(testClass);
        }
    }

    static class NullPrintStream extends PrintStream {
        public NullPrintStream() {
            super(new NullOutputStream());
        }
    }

    static class NullOutputStream extends OutputStream {
        @Override
        public void write(int b) throws IOException {
        }
    }
}
