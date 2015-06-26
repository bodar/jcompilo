package com.googlecode.jcompilo.convention;

import com.googlecode.totallylazy.functions.Function1;

import java.io.File;

import static com.googlecode.totallylazy.Sequences.sequence;

public interface ReleaseFile {
    File file();
    String description();
    Iterable<String> labels();

    class constructors {
        public static ReleaseFile releaseFile(final File file, final String description, final String... labels){
            return new ReleaseFile() {
                @Override
                public File file() {
                    return file;
                }

                @Override
                public String description() {
                    return description;
                }

                @Override
                public Iterable<String> labels() {
                    return sequence(labels);
                }
            };
        }
    }

    class functions {
        public static Function1<ReleaseFile, File> file = releaseFile -> releaseFile.file();
    }
}
