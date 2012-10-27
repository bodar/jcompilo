package com.googlecode.compilo.intellij;

import com.googlecode.compilo.CompileOption;
import com.googlecode.compilo.Compiler;
import com.googlecode.compilo.Environment;
import com.googlecode.compilo.Inputs;
import com.googlecode.compilo.Outputs;
import com.googlecode.totallylazy.Sequence;

import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FakeProcess extends Process {
    private final Compiler compiler;
    private final Inputs inputs;
    private final Outputs outputs;
    private int exit;

    public FakeProcess(final Inputs inputs, final Outputs outputs, final Iterable<File> dependencies, final Sequence<CompileOption> compileOptions, DiagnosticListener<JavaFileObject> diagnosticListener) throws IOException {
        this.inputs = inputs;
        this.outputs = outputs;
        compiler = Compiler.compiler(Environment.constructors.environment(), dependencies, compileOptions, diagnosticListener);
    }

    @Override
    public OutputStream getOutputStream() {
        throw new UnsupportedOperationException();
    }

    @Override
    public InputStream getInputStream() {
        return null;
    }

    @Override
    public InputStream getErrorStream() {
        return null;
    }

    @Override
    public int waitFor() throws InterruptedException {
        try {
            compiler.compile(inputs, outputs);
            return exit = 0;
        } catch (Exception e) {
            return exit = -1;
        }
    }

    @Override
    public int exitValue() {
        return exit;
    }

    @Override
    public void destroy() {
    }
}
