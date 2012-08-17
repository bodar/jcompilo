package com.googlecode.compilo;

import com.googlecode.totallylazy.Files;

import java.io.File;
import java.io.PrintStream;
import java.util.Properties;

public interface Environment {
    File workingDirectory();
    Properties properties();
    PrintStream out();

    class constructors {
        public static Environment environment() {
            return environment(Files.workingDirectory());
        }

        public static Environment environment(File workingDirectory) {
            return environment(workingDirectory, System.getProperties());
        }

        public static Environment environment(File workingDirectory, Properties properties) {
            return environment(workingDirectory, properties, System.out);
        }

        public static Environment environment(final File workingDirectory, final Properties properties, final PrintStream out) {
            return new Environment() {
                @Override
                public File workingDirectory() {
                    return workingDirectory;
                }

                @Override
                public Properties properties() {
                    return properties;
                }

                @Override
                public PrintStream out() {
                    return out;
                }
            };
        }
    }

}
