package com.googlecode.jcompilo;

import java.util.Map;

public class ByteClassLoader extends ClassLoader {
    private final Map<String, byte[]> data;

    public ByteClassLoader(Map<String, byte[]> data) {
        this.data = data;
    }

    public ByteClassLoader(Map<String, byte[]> data, ClassLoader parent) {
        super(parent);
        this.data = data;
    }

    @Override
    protected Class<?> findClass(final String name) throws ClassNotFoundException {
        byte[] bytes = data.get(name.replace('.', '/').concat(".class"));
        return defineClass(name, bytes, 0, bytes.length);
    }

    static {
        ClassLoader.registerAsParallelCapable();
    }
}
