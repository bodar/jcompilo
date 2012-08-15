package com.googlecode.compilo;

import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Zip;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import static com.googlecode.compilo.Compiler.compiler;
import static com.googlecode.totallylazy.Files.delete;
import static com.googlecode.totallylazy.Files.directory;
import static com.googlecode.totallylazy.Files.file;
import static com.googlecode.totallylazy.Files.hasSuffix;
import static com.googlecode.totallylazy.Files.recursiveFiles;
import static com.googlecode.totallylazy.Files.workingDirectory;
import static com.googlecode.totallylazy.Sequences.cons;
import static com.googlecode.totallylazy.Sequences.sequence;
import static java.lang.String.format;

public interface Build {
    Build build() throws Exception;
    Build clean() throws Exception;
    Build compile() throws Exception;
    Build test() throws Exception;
    Build Package() throws Exception;

    File root();
    Locations locations();
    Identifiers identifiers();
    Iterable<CompileOption> compileOptions();
    Iterable<File> dependencies();

    interface Locations {
        File build();
        File artifacts();
        File src();
        File test();
        File lib();
        File jar();
        File testJar();
    }

    interface Identifiers {
        String group();
        String artifact();
        String version();
        String versionedArtifact();
    }

    abstract class Convention implements Build {
        private final File root;

        public Convention() {
            this(workingDirectory());
        }

        public Convention(File root) {
            this.root = root;
        }

        public Build build() throws Exception {
            System.out.println("build:");
            return clean().compile().test().Package();
        }

        public Build clean() throws Exception {
            System.out.println("clean:");
            delete(locations().artifacts()); return this;
        }

        public Build compile() throws Exception {
            System.out.println("compile:");
            compiler(dependencies(), compileOptions()).compile(locations().src(), locations().jar());
            return this;
        }

        @Override
        public Build test() throws Exception {
            System.out.println("test:");
            Tests tests = Tests.tests();
            Sequence<File> productionJars = cons(locations().jar(), dependencies());
            compiler(productionJars, compileOptions()).
                    add(tests).compile(locations().test(), locations().testJar());
            tests.execute(cons(locations().testJar(), productionJars));
            return this;
        }

        @Override
        public Build Package() throws IOException {
            System.out.println("package:");
            Zip.zip(locations().src(), locations().sourceJar());
            return this;
        }

        @Override
        public Iterable<CompileOption> compileOptions() {
            return sequence(CompileOption.Debug);
        }

        @Override
        public Iterable<File> dependencies() {
            return recursiveFiles(locations().lib()).filter(hasSuffix("jar")).realise();
        }

        @Override
        public Locations locations() {
            return new Locations(root(), identifiers());
        }

        @Override
        public File root() {
            return root;
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
            public File jar() { return file(artifacts(), format("%s.jar", identifiers.versionedArtifact())); }
            public File sourceJar() { return file(artifacts(), format("%s-sources.jar", identifiers.versionedArtifact())); }
            public File testJar() { return file(artifacts(), format("%s-tests.jar", identifiers.versionedArtifact())); }
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

            @Override
            public String versionedArtifact() {
                return format("%s-%s", artifact(), version());
            }
        }
    }
}
