package com.googlecode.compilo;

import com.googlecode.totallylazy.Streams;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import java.io.PrintStream;
import java.util.List;

public class TestExecutor {
    public static boolean execute(final List<String> tests) throws Exception {
        PrintStream original = System.out;
        System.setOut(Streams.nullPrintStream());
        final JUnitCore junit = new JUnitCore();
        Result result = new Result();
        junit.addListener(result.createListener());
        junit.run(asClasses(tests));

        System.setOut(original);

        boolean success = result.wasSuccessful();
        if (!success) {
            System.out.println("Tests failed: " +  result.getFailureCount());
            for (Failure failure : result.getFailures()) {
                System.out.println(failure.getTestHeader());
                System.out.println(failure.getTrace());
            }
        }

        return success;
    }

    private static Class<?>[] asClasses(List<String> tests) throws ClassNotFoundException {
        Class<?>[] result = new Class<?>[tests.size()];
        for (int i = 0; i < tests.size(); i++) {
            result[i] = Class.forName(className(tests.get(i)));
        }
        return result;
    }

    private static String className(String filename) {
        return filename.replace('/', '.').replace(".java", "");
    }


}
