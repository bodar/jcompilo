package com.googlecode.compilo;

import com.googlecode.totallylazy.Sequences;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;

import static com.googlecode.totallylazy.Sequences.sequence;

public class ByteClassLoader extends URLClassLoader {
    private final Map<String, byte[]> data;

    public ByteClassLoader(Map<String, byte[]> data) {
        this(data, Sequences.empty(URL.class));
    }

    public ByteClassLoader(Map<String, byte[]> data, Iterable<? extends URL> urls) {
        super(sequence(urls).toArray(URL.class));
        this.data = data;
    }

    public ByteClassLoader(Map<String, byte[]> data, Iterable<? extends URL> urls, ClassLoader parent) {
        super(sequence(urls).toArray(URL.class), parent);
        this.data = data;
    }

    @Override
    protected Class<?> findClass(final String name) throws ClassNotFoundException {
        byte[] bytes = data.get(name.replace('.', '/').concat(".class"));
        if (bytes == null) return super.findClass(name);
        return defineClass(name, bytes, 0, bytes.length);
    }
}
