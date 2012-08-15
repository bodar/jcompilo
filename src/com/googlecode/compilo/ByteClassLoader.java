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
        byte[] classBytes = data.get(name.replace('.', '/').concat(".class"));
        if (classBytes == null) {return super.findClass(name);}
        return defineClass(name, classBytes, 0, classBytes.length);
    }
}
