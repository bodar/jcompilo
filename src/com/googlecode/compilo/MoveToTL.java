package com.googlecode.compilo;

import com.googlecode.totallylazy.Bytes;
import com.googlecode.totallylazy.Function1;

import java.io.InputStream;

public class MoveToTL {
    public static Function1<InputStream, byte[]> read() {
        return new Function1<InputStream, byte[]>() {
            @Override
            public byte[] call(InputStream inputStream) throws Exception {
                return Bytes.bytes(inputStream);
            }
        };
    }

    public static String classNameForSource(String sourceFilename) {
        return sourceFilename.replace(".java", "").replace('/', '.');
    }

    public static String classNameForByteCode(String sourceFilename) {
        return sourceFilename.replace(".class", "").replace('/', '.');
    }

    public static String classFilename(String className) {
        return className.replace('.', '/') + ".class";
    }
}
