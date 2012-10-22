package com.googlecode.compilo;

import com.googlecode.totallylazy.Destination;
import com.googlecode.totallylazy.Sources;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MemoryStore implements Inputs, Destination, Outputs {
    private final Map<String, Resource> data;

    public MemoryStore(Map<String, Resource> data) {
        this.data = data;
    }

    public static MemoryStore memoryStore() {
        return memoryStore(new HashMap<String, Resource>());
    }

    public static MemoryStore memoryStore(Map<String, Resource> data) {
        return new MemoryStore(data);
    }

    public static MemoryStore memoryStore(Sources source) {
        MemoryStore result = memoryStore();
        Sources.methods.copyAndClose(source, result);
        return result;
    }

    public static MemoryStore memoryStore(Iterable<? extends Resource> resources) {
        MemoryStore memoryStore = memoryStore();
        Inputs.methods.copy(resources, memoryStore);
        return memoryStore;
    }

    @Override
    public OutputStream destination(final String name, final Date modified) throws IOException {
        return new ByteArrayOutputStream() {
            @Override
            public void close() throws IOException {
                data.put(name, Resource.constructors.resource(name, modified, toByteArray()));
            }
        };
    }

    @Override
    public void close() throws IOException {
    }

    public Map<String, Resource> data() {
        return data;
    }

    @Override
    public boolean isEmpty() {
        return data.isEmpty();
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public Iterator<Resource> iterator() {
        return data.values().iterator();
    }

    @Override
    public void put(Resource resource) {
        data.put(resource.name(), resource);
    }
}
