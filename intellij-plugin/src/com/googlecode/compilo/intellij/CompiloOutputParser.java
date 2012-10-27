package com.googlecode.compilo.intellij;

import com.googlecode.totallylazy.collections.ImmutableMap;
import com.googlecode.totallylazy.collections.ImmutableSortedMap;
import com.intellij.compiler.OutputParser;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.vfs.LocalFileSystem;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import java.util.Locale;

import static com.intellij.openapi.vfs.VirtualFileManager.constructUrl;
import static java.lang.Long.valueOf;

public class CompiloOutputParser extends OutputParser {
    private static final ImmutableMap<Diagnostic.Kind, CompilerMessageCategory> conversions =
            ImmutableSortedMap.constructors.sortedMap(
                    Diagnostic.Kind.ERROR, CompilerMessageCategory.ERROR,
                    Diagnostic.Kind.WARNING, CompilerMessageCategory.WARNING,
                    Diagnostic.Kind.MANDATORY_WARNING, CompilerMessageCategory.WARNING,
                    Diagnostic.Kind.NOTE, CompilerMessageCategory.INFORMATION,
                    Diagnostic.Kind.OTHER, CompilerMessageCategory.INFORMATION
            );

    private final DiagnosticCollector<JavaFileObject> diagnosticCollector;

    private CompiloOutputParser(DiagnosticCollector<JavaFileObject> diagnosticCollector) {
        this.diagnosticCollector = diagnosticCollector;
    }

    public static CompiloOutputParser compiloOutputParser(DiagnosticCollector<JavaFileObject> diagnosticCollector) {
        return new CompiloOutputParser(diagnosticCollector);
    }

    @Override
    public boolean processMessageLine(Callback callback) {
        for (Diagnostic<? extends JavaFileObject> diagnostic : diagnosticCollector.getDiagnostics()) {
            callback.message(convert(diagnostic.getKind()), diagnostic.getMessage(Locale.getDefault()), uri(diagnostic),
                    valueOf(diagnostic.getLineNumber()).intValue(), valueOf(diagnostic.getColumnNumber()).intValue());
        }
        return super.processMessageLine(callback);
    }

    public static String uri(Diagnostic<? extends JavaFileObject> diagnostic) {
        return constructUrl(LocalFileSystem.PROTOCOL, diagnostic.getSource().getName());
    }

    public static CompilerMessageCategory convert(Diagnostic.Kind kind) {
        return conversions.get(kind).get();
    }
}