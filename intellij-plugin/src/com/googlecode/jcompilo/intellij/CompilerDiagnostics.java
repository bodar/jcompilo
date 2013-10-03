package com.googlecode.jcompilo.intellij;

import com.googlecode.totallylazy.collections.PersistentMap;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.vfs.LocalFileSystem;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;
import java.util.Locale;

import static com.googlecode.totallylazy.collections.PersistentSortedMap.constructors.sortedMap;
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
        JavaFileObject source = diagnostic.getSource();
        if(source == null) return null;

        return constructUrl(LocalFileSystem.PROTOCOL, source.toUri().getPath());
    }

    private static final PersistentMap<Diagnostic.Kind, CompilerMessageCategory> conversions = sortedMap(
            Diagnostic.Kind.ERROR, CompilerMessageCategory.ERROR,
            Diagnostic.Kind.WARNING, CompilerMessageCategory.WARNING,
            Diagnostic.Kind.MANDATORY_WARNING, CompilerMessageCategory.WARNING,
            Diagnostic.Kind.NOTE, CompilerMessageCategory.INFORMATION,
            Diagnostic.Kind.OTHER, CompilerMessageCategory.INFORMATION);

    private static CompilerMessageCategory convert(Diagnostic.Kind kind) {
        return conversions.lookup(kind).get();
    }
}
