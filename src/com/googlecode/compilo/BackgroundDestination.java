package com.googlecode.compilo;

import com.googlecode.totallylazy.Bytes;
import com.googlecode.totallylazy.Closeables;
import com.googlecode.totallylazy.Destination;
import com.googlecode.totallylazy.Runnables;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.newSingleThreadExecutor;

public class BackgroundDestination implements Destination {
    private final Destination destination;
    private final ExecutorService executor;

    private BackgroundDestination(Destination destination, ExecutorService executor) {
        this.executor = executor;
        this.destination = destination;
    }

    public static BackgroundDestination backgroundDestination(Destination destination) {
        return backgroundDestination(destination, newSingleThreadExecutor());
    }

    public static BackgroundDestination backgroundDestination(Destination destination, ExecutorService executor) {
        return new BackgroundDestination(destination, executor);
    }

    @Override
    public OutputStream destination(final String name) throws IOException {
        return new ByteArrayOutputStream() {
            @Override
            public void close() throws IOException {
                executor.submit(toDestination(name, toByteArray()));
            }

        };
    }

    private Callable<Void> toDestination(final String name1, final byte[] value) {
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Closeables.using(destination.destination(name1), Bytes.write(value));
                return Runnables.VOID;
            }
        };
    }

    @Override
    public void close() throws IOException {
        BackgroundOutputs.close(executor);
        destination.close();
    }
}