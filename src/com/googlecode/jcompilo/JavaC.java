package com.googlecode.jcompilo;

import com.googlecode.totallylazy.io.*;
import com.googlecode.totallylazy.Files;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Sequence;

import java.io.File;
import java.util.concurrent.Callable;

import static com.googlecode.jcompilo.Compiler.JAVA_FILES;
import static com.googlecode.totallylazy.io.FileSource.fileSource;
import static com.googlecode.totallylazy.Files.asFile;
import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.predicates.Predicates.where;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Strings.split;
import static java.lang.System.getProperty;

public class JavaC implements Callable<Boolean> {
    private final Environment environment;
    private Sequence<String> arguments;

    private JavaC(Sequence<String> arguments, Environment environment) {
        this.arguments = arguments;
        this.environment = environment;
    }

    public static void main(String[] args) throws Exception {
        javaC(sequence(args), Environment.constructors.environment()).call();
    }

    public static JavaC javaC(Sequence<String> arguments, Environment environment) {
        return new JavaC(arguments, environment);
    }

    public static JavaC javaC(Sequence<String> arguments) {
        return new JavaC(arguments, Environment.constructors.environment());
    }

    @Override
    public Boolean call() throws Exception {
        Sequence<File> classPath = argument("-cp").toSequence().
                flatMap(split(getProperty("path.separator"))).
                map(asFile());

        Sources sourceFiles = FilterSource.filterSource(where(Source::name, JAVA_FILES), fileSource(directory("-sourcepath")));
        Destination destination = FileDestination.fileDestination(directory("-d"));
        return CompileProcessor.compile(environment, classPath, sourceFiles, destination);
    }

    private File directory(String name) {
        return argument(name).
                map(asFile()).getOrElse(Files.workingDirectory());
    }

    private Option<String> argument(String name) {
        int index = arguments.indexOf(name);
        if(index == -1) return none();
        return arguments.drop(index + 1).headOption();
    }
}
