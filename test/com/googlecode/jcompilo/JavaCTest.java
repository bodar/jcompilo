package com.googlecode.jcompilo;

import org.junit.Test;

import java.io.File;
import java.io.InputStream;

import static com.googlecode.totallylazy.Files.name;
import static com.googlecode.totallylazy.Files.recursiveFiles;
import static com.googlecode.totallylazy.predicates.Predicates.where;
import static com.googlecode.totallylazy.Streams.copy;
import static com.googlecode.totallylazy.Strings.endsWith;
import static com.googlecode.totallylazy.Strings.startsWith;

public class JavaCTest {
    @Test
    public void supportsBasicJavaCOptions() throws Exception {
        String command = "java -cp " + findJar("build/artifacts", "jcompilo") + " " +
                JavaC.class.getName() + " " +
                "-sourcepath example/src " +
                "-d example/build/artifacts/ " +
                "-cp " + findJar("example/lib/runtime", "totallylazy");
        InputStream result = Processes.inputStream(command);
        copy(result, System.out);
    }

    private File findJar(String directory, String name) {
        return recursiveFiles(new File(directory)).
                filter(where(name(), startsWith(name))).
                filter(where(name(), endsWith(".jar"))).
                filter(where(name(), endsWith("-sources.jar").not())).
                head();
    }
}
