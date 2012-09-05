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

    public static String className(String fileName) {
        return fileName.replace(".java", "").replace('.', '/');
    }
}
