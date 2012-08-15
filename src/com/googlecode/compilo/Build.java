package com.googlecode.compilo;

import com.googlecode.totallylazy.Sequence;

import java.io.File;
import java.util.Properties;

import static com.googlecode.totallylazy.Files.delete;
import static com.googlecode.totallylazy.Files.directory;
import static com.googlecode.totallylazy.Files.file;
import static com.googlecode.totallylazy.Files.hasSuffix;
import static com.googlecode.totallylazy.Files.recursiveFiles;
import static com.googlecode.totallylazy.Files.workingDirectory;
import static com.googlecode.totallylazy.Sequences.sequence;
import static java.lang.String.format;

public interface Build {
    Build build();
    Build clean();
    Build compile();
    Build test();
    Build Package();
    Locations locations();
    Identifiers identifiers();

    interface Locations {
        File build();
        File artifacts();
        File src();
        File test();
        File lib();
        File jar();
    }

    interface Identifiers {
        String group();
        String artifact();
        String version();
    }

    abstract class Convention implements Build {
        private final File root;
        private final Compiler compiler;

        public Convention() {
            this(workingDirectory());
        }

        public Convention(File root) {
            this.root = root;
            compiler = Compiler.compiler(dependencies(), compileOptions());
        }

        public Build build() { return clean().compile().test().Package(); }
        public Build clean() { delete(locations().artifacts()); return this; }
        public Build compile() {
//            compiler.compile(locations().src(), locations().jar())
            return this;
        }

        public Sequence<CompileOption> compileOptions() {
            return sequence(CompileOption.Debug);
        }

        public Sequence<File> dependencies() {
            return recursiveFiles(locations().lib()).filter(hasSuffix("jar")).realise();
        }

        @Override
        public Build test() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Build Package() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Locations locations() {
            return new Locations(root, identifiers());
        }

        public static class Locations implements Build.Locations{
            private final File root;
            private final Build.Identifiers identifiers;

            public Locations(File root, Build.Identifiers identifiers) {
                this.root = root;
                this.identifiers = identifiers;
            }

            public File build() { return directory(root, "build"); }
            public File artifacts() { return directory(build(), "artifacts"); }
            public File src() { return directory(root, "src"); }
            public File test() { return directory(root, "test"); }
            public File lib() { return directory(root, "lib"); }
            public File jar() { return file(artifacts(), format("%s-%s.jar", identifiers.artifact(), identifiers.version())); }
        }

        public static abstract class Identifiers implements Build.Identifiers{
            private final Properties properties;

            protected Identifiers(Properties properties) {
                this.properties = properties;
            }

            protected Identifiers() {
                this(System.getProperties());
            }

            @Override
            public String version() {
                return properties.getProperty("build.number", "dev.build");
            }
        }


    }
}
