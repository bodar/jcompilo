package com.googlecode.jcompilo.tool;

import javax.lang.model.SourceVersion;
import javax.tools.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Set;

import static javax.tools.ToolProvider.getSystemJavaCompiler;

public class ForwardingJavaCompiler<C extends JavaCompiler> implements JavaCompiler {
    protected final C compiler;

    protected ForwardingJavaCompiler(C compiler) {
        this.compiler = compiler;
    }

    @Override
    public CompilationTask getTask(Writer out, JavaFileManager fileManager, DiagnosticListener<? super JavaFileObject> diagnosticListener, Iterable<String> options, Iterable<String> classes, Iterable<? extends JavaFileObject> compilationUnits) {
        return compiler.getTask(out, fileManager, diagnosticListener, options, classes, compilationUnits);
    }

    @Override
    public StandardJavaFileManager getStandardFileManager(DiagnosticListener<? super JavaFileObject> diagnosticListener, Locale locale, Charset charset) {
        return compiler.getStandardFileManager(diagnosticListener, locale, charset);
    }

    @Override
    public int isSupportedOption(String option) {
        return compiler.isSupportedOption(option);
    }

    @Override
    public int run(InputStream in, OutputStream out, OutputStream err, String... arguments) {
        return compiler.run(in, out, err, arguments);
    }

    @Override
    public Set<SourceVersion> getSourceVersions() {
        return compiler.getSourceVersions();
    }
}
