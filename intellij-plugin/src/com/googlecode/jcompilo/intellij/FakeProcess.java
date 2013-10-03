package com.googlecode.jcompilo.intellij;

import com.googlecode.jcompilo.*;
import com.googlecode.jcompilo.Compiler;
import com.googlecode.totallylazy.Exceptions;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Sequence;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.diagnostic.Logger;

import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;
import java.io.*;

import static com.googlecode.totallylazy.Exceptions.asString;

public class FakeProcess extends Process {
    private static final Logger LOG = Logger.getInstance(FakeProcess.class);

    private final Compiler compiler;
    private final Inputs inputs;
    private final Outputs outputs;
    private int exit;

    public FakeProcess(final Inputs inputs, final Outputs outputs, final Iterable<File> dependencies, final Sequence<CompileOption> compileOptions, DiagnosticListener<JavaFileObject> diagnosticListener, final CompileContext context) throws IOException {
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
            Option<JCompiloException> notExceptional = Exceptions.find(e, JCompiloException.class);
            if(notExceptional.isDefined()) {
                LOG.info(notExceptional.get());
            } else {
                LOG.error(e);
            }
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
