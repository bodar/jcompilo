package com.googlecode.compilo;

import com.googlecode.totallylazy.Destination;
import com.googlecode.totallylazy.Source;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.googlecode.compilo.Resource.functions.resource;
import static com.googlecode.totallylazy.Maps.pairs;

public class MemoryStore implements Inputs, Destination, Outputs {
    private final Map<String, byte[]> data;

    public MemoryStore(Map<String, byte[]> data) {
        this.data = data;
    }

    public static MemoryStore memoryStore() {
        return memoryStore(new HashMap<String, byte[]>());
    }

    public static MemoryStore memoryStore(Map<String, byte[]> data) {
        return new MemoryStore(data);
    }

    public static MemoryStore memoryStore(Source source) {
        MemoryStore result = memoryStore();
        Source.methods.copyAndClose(source, result);
        return result;
    }

    public static MemoryStore memoryStore(Iterable<? extends Resource> resources) {
        MemoryStore memoryStore = memoryStore();
        Inputs.methods.copy(resources, memoryStore);
        return memoryStore;
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
    public void close() throws IOException {
    }

    public Map<String, byte[]> data() {
        return data;
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public void copyTo(Outputs outputs) {
        Inputs.methods.copy(this, outputs);
    }

    @Override
    public Iterator<Resource> iterator() {
        return pairs(data).map(resource).iterator();
    }

    @Override
    public void put(Resource resource) {
        data.put(resource.name(), resource.bytes());
    }
}
