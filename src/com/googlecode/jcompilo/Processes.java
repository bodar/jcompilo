package com.googlecode.jcompilo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static com.googlecode.totallylazy.Files.workingDirectory;
import static com.googlecode.totallylazy.Lists.list;

public class Processes {
    public static InputStream inputStream(String command) throws IOException {
        return inputStream(command, workingDirectory());
    }

    public static InputStream inputStream(String command, File workingDirectory) throws IOException {
        return execute(command, workingDirectory).getInputStream();
    }

    public static Process execute(String command, File workingDirectory) throws IOException {
        return execute(list(command.split("\\s")), workingDirectory);
    }

    public static Process execute(List<String> arguments, File workingDirectory) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(arguments);
        builder.redirectErrorStream(true);
        builder.directory(workingDirectory);
        return builder.start();
    }
}
