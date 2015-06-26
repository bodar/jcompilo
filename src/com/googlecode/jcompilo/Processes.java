package com.googlecode.jcompilo;

import com.googlecode.totallylazy.Strings;
import com.googlecode.totallylazy.regex.Regex;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static com.googlecode.totallylazy.Files.workingDirectory;
import static com.googlecode.totallylazy.Lists.list;

public class Processes {
    private static final Regex splitter = Regex.regex("([^\"]\\S*|\".+?\")\\s*");

    public static String executeReturnString(String command) throws IOException {
        return executeReturnString(command, workingDirectory());
    }

    public static String executeReturnString(String command, File workingDirectory) throws IOException {
        return Strings.string(inputStream(command, workingDirectory));
    }

    public static InputStream inputStream(String command) throws IOException {
        return inputStream(command, workingDirectory());
    }

    public static InputStream inputStream(String command, File workingDirectory) throws IOException {
        return execute(command, workingDirectory).getInputStream();
    }

    public static Process execute(String command, File workingDirectory) throws IOException {
        List<String> arguments = splitter.findMatches(command).map(matchResult ->
                matchResult.group(1).replaceAll("\"", "")).toList();
        return execute(arguments, workingDirectory);
    }

    public static Process execute(List<String> arguments, File workingDirectory) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(arguments);
        builder.redirectErrorStream(true);
        builder.directory(workingDirectory);
        return builder.start();
    }
}
