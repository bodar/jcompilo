package com.googlecode.compilo;

import com.googlecode.totallylazy.*;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class MemoryStore implements Source, Destination {
    private final Map<String, byte[]> data = new HashMap<String, byte[]>();

    private MemoryStore() {}

    public static MemoryStore memoryStore() {
        return new MemoryStore();
    }

    public static MemoryStore copy(Source source) {
        MemoryStore result = memoryStore();
        Source.methods.copyAndClose(source, result);
        return result;
    }

    @Override
    public OutputStream destination(final String name) throws IOException {
        return new ByteArrayOutputStream() {
            @Override
            public void close() throws IOException {
                data.put(name, toByteArray());
            }
        };
    }

    @Override
    public Sequence<Pair<String, InputStream>> sources() {
        return Maps.pairs(data).map(Callables.<String, byte[], InputStream>second(inputStream()));
    }

    @Override
    public void close() throws IOException {
    }

    private static  Function1<byte[], InputStream> inputStream() {
        return new Function1<byte[], InputStream>() {
            @Override
            public InputStream call(byte[] bytes) throws Exception {
                return new ByteArrayInputStream(bytes);
            }
        };
    }
}
