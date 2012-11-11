package com.googlecode.compilo.convention;

import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Function2;

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
        public static Function1<ReleaseFile, File> file = new Function1<ReleaseFile, File>() {
            @Override
            public File call(ReleaseFile releaseFile) throws Exception {
                return releaseFile.file();
            }
        };
    }
}
