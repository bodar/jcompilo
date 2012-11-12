package com.googlecode.jcompilo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static com.googlecode.totallylazy.Lists.list;

public class Processes {
    public static InputStream exec(File workingDirectory, String command) throws IOException {
        try {
            Process process = processFor(workingDirectory, command);
            process.waitFor();
            return process.getInputStream();
        } catch (InterruptedException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    public static Process processFor(File workingDirectory, String command) throws IOException {
        return processFor(workingDirectory, list(command.split("\\s")));
    }

    public static Process processFor(File workingDirectory, List<String> arguments) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(arguments);
        builder.redirectErrorStream(true);
        builder.directory(workingDirectory);
        return builder.start();
    }
}
