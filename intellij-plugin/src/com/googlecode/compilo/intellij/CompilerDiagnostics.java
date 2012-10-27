package com.googlecode.compilo.intellij;

import com.googlecode.compilo.SourceFileObject;
import com.googlecode.totallylazy.collections.ImmutableMap;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.vfs.LocalFileSystem;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;
import java.util.Locale;

import static com.googlecode.totallylazy.collections.ImmutableSortedMap.constructors.sortedMap;
import static com.intellij.openapi.vfs.VirtualFileManager.constructUrl;
import static java.lang.Long.valueOf;

public class CompilerDiagnostics implements DiagnosticListener<JavaFileObject> {
    private CompileContext compileContext;

    public CompilerDiagnostics(CompileContext compileContext) {
        this.compileContext = compileContext;
    }

    @Override
    public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
        compileContext.addMessage(convert(diagnostic.getKind()), diagnostic.getMessage(Locale.getDefault()), uri(diagnostic),
                valueOf(diagnostic.getLineNumber()).intValue(), valueOf(diagnostic.getColumnNumber()).intValue());
    }

    private static String uri(Diagnostic<? extends JavaFileObject> diagnostic) {
        return constructUrl(LocalFileSystem.PROTOCOL, sourceFileName(diagnostic.getSource()));
    }

    private static String sourceFileName(JavaFileObject source) {
        if (source instanceof SourceFileObject) {
            SourceFileObject sourceFileObject = (SourceFileObject) source;
            if (sourceFileObject.resource() instanceof CompiloBackendCompiler.ResourceWithSource) {
                return ((CompiloBackendCompiler.ResourceWithSource) sourceFileObject.resource()).source.toString();
            }
        }
        return source.getName();
    }

    private static final ImmutableMap<Diagnostic.Kind, CompilerMessageCategory> conversions = sortedMap(
            Diagnostic.Kind.ERROR, CompilerMessageCategory.ERROR,
            Diagnostic.Kind.WARNING, CompilerMessageCategory.WARNING,
            Diagnostic.Kind.MANDATORY_WARNING, CompilerMessageCategory.WARNING,
            Diagnostic.Kind.NOTE, CompilerMessageCategory.INFORMATION,
            Diagnostic.Kind.OTHER, CompilerMessageCategory.INFORMATION);

    private static CompilerMessageCategory convert(Diagnostic.Kind kind) {
        return conversions.get(kind).get();
    }
}
