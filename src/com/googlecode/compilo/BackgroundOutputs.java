package com.googlecode.compilo;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BackgroundOutputs implements Outputs, Closeable {
    private final Environment env;
    private final Outputs outputs;
    private final ExecutorService service;

    private BackgroundOutputs(Environment env, Outputs outputs) {
        this.env = env;
        this.outputs = outputs;
        service = Executors.newFixedThreadPool(Compiler.CPUS);
    }

    public static BackgroundOutputs backgroundOutputs(Environment env, Outputs outputs) {
        return new BackgroundOutputs(env, outputs);
    }

    @Override
    public void close() throws IOException {
        close(service);
    }

    @Override
    public void put(final Resource resource) {
        service.execute(new Runnable() {
            @Override
            public void run() {
                outputs.put(resource);
            }
        });
    }

    public static void close(ExecutorService service1) {
        try {
            service1.shutdown();
            service1.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            throw new UnsupportedOperationException(e);
        }
    }
}
