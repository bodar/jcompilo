package com.googlecode.jcompilo.tool;

import com.googlecode.jcompilo.ResourceHandler;
import com.googlecode.totallylazy.collections.PersistentList;

import javax.tools.*;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Locale;

import static javax.tools.ToolProvider.getSystemJavaCompiler;

public class JCompiler extends ForwardingJavaCompiler<JavaCompiler> {
    public static final JavaCompiler DEFAULT_COMPILER = defaultCompiler();

    private static JavaCompiler defaultCompiler() {
        JavaCompiler javaCompiler = getSystemJavaCompiler();
        if(javaCompiler == null) throw new IllegalStateException("No SystemJavaCompiler installed");
        return javaCompiler;
    }

    private final PersistentList<ResourceHandler> resourceHandlers;

    public JCompiler(JavaCompiler javaCompiler, PersistentList<ResourceHandler> resourceHandlers) {
        super(javaCompiler);
        this.resourceHandlers = resourceHandlers;
    }

    @Override
    public CompilationTask getTask(Writer out, JavaFileManager fileManager, DiagnosticListener<? super JavaFileObject> diagnosticListener, Iterable<String> options, Iterable<String> classes, Iterable<? extends JavaFileObject> compilationUnits) {
        return super.getTask(out, ensurePostProcessingPossible((StandardJavaFileManager) fileManager, diagnosticListener), diagnosticListener, options, classes, compilationUnits);
    }

    private StandardJavaFileManager ensurePostProcessingPossible(StandardJavaFileManager original, DiagnosticListener<? super JavaFileObject> diagnosticListener) {
        if(original instanceof PostProcessor) return original;
        StandardJavaFileManager newFileManager = getStandardFileManager(diagnosticListener, Locale.getDefault(), Charset.forName("UTF-8"));
        for (StandardLocation location : StandardLocation.values()) {
            try {
                newFileManager.setLocation(location, original.getLocation(location));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return newFileManager;
    }

    @Override
    public StandardJavaFileManager getStandardFileManager(DiagnosticListener<? super JavaFileObject> diagnosticListener, Locale locale, Charset charset) {
        return new PostProcessor(compiler.getStandardFileManager(diagnosticListener, locale, charset), resourceHandlers);
    }

    public PersistentList<ResourceHandler> resourceHandlers() {
        return resourceHandlers;
    }

    public JavaCompiler compiler() {
        return compiler;
    }

}
