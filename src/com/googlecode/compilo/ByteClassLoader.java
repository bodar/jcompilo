package com.googlecode.compilo;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;

public class ByteClassLoader extends URLClassLoader {
    private final Map<String, byte[]> data;

    public ByteClassLoader(Map<String, byte[]> data) {
        super(new URL[0]);
        this.data = data;
    }

    @Override
    protected Class<?> findClass(final String name) throws ClassNotFoundException {
        byte[] bytes = data.get(name.replace('.', '/').concat(".class"));
        if (bytes == null) return super.findClass(name);
        return defineClass(name, bytes, 0, bytes.length);
    }
}
