package com.googlecode.compilo;

import com.googlecode.totallylazy.Bytes;
import com.googlecode.totallylazy.Closeables;
import com.googlecode.totallylazy.Destination;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.googlecode.totallylazy.ZipDestination.zipDestination;

public class BackgroundZip {
    public static Destination backgroundZip(File destinationJar) throws FileNotFoundException {
        final ExecutorService zipWriter = Executors.newSingleThreadExecutor();
        final Destination destination = zipDestination(new FileOutputStream(destinationJar));
        return new MemoryStore(new HashMap<String, byte[]>() {
            @Override
            public byte[] put(final String key, final byte[] value) {
                zipWriter.submit(new Callable<OutputStream>() {
                    @Override
                    public OutputStream call() throws IOException {
                        return Closeables.using(destination.destination(key), Bytes.write(value));
                    }
                });
                return super.put(key, value);
            }
        }) {
            @Override
            public void close() throws IOException {
                try {
                    zipWriter.shutdown();
                    zipWriter.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
                    destination.close();
                } catch (InterruptedException e) {
                    throw new UnsupportedOperationException(e);
                }
            }
        };
    }
}
