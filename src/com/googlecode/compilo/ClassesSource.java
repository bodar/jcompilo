package com.googlecode.compilo;

import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Source;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static com.googlecode.totallylazy.Sequences.sequence;

public class ClassesSource implements Source {
    private final Sequence<Class<?>> classes;

    private ClassesSource(final Sequence<Class<?>> classes) {
        this.classes = classes;
    }

    public static ClassesSource classesSource(Class<?>... classes) {
        return new ClassesSource(sequence(classes));
    }

    @Override
    public Sequence<Pair<String, InputStream>> sources() {
        return classes.map(new Function1<Class<?>, Pair<String, InputStream>>() {
            @Override
            public Pair<String, InputStream> call(Class<?> aClass) throws Exception {
                String fileName = String.format("%s.class", aClass.getName().replace('.', '/'));
                URL url = aClass.getResource("/" + fileName);
                return Pair.pair(fileName, url.openStream());
            }
        });
    }

    @Override
    public void close() throws IOException {
    }
}
