package com.googlecode.compilo;

import com.googlecode.totallylazy.Bytes;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Runnables;

import java.io.InputStream;
import java.io.Writer;
import java.util.Properties;

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

    public static Function1<Writer, Void> write(final Properties properties) {
        return new Function1<Writer, Void>() {
            @Override
            public Void call(Writer writer) throws Exception {
                properties.store(writer, "");
                return Runnables.VOID;
            }
        };
    }
}
