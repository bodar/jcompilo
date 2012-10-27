package com.googlecode.compilo.intellij;

import com.googlecode.totallylazy.collections.ImmutableMap;
import com.googlecode.totallylazy.collections.ImmutableSortedMap;
import com.intellij.compiler.OutputParser;
import com.intellij.openapi.compiler.CompilerMessageCategory;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import java.util.Locale;

public class DiagnosticsOutputParser extends OutputParser {
    private final DiagnosticCollector<JavaFileObject> diagnosticCollector;

    private static final ImmutableMap<Diagnostic.Kind, CompilerMessageCategory> conversions = ImmutableSortedMap.constructors.sortedMap(
            Diagnostic.Kind.ERROR, CompilerMessageCategory.ERROR,
            Diagnostic.Kind.WARNING, CompilerMessageCategory.WARNING,
            Diagnostic.Kind.MANDATORY_WARNING, CompilerMessageCategory.WARNING,
            Diagnostic.Kind.NOTE, CompilerMessageCategory.INFORMATION,
            Diagnostic.Kind.OTHER, CompilerMessageCategory.INFORMATION
    );

    public DiagnosticsOutputParser(DiagnosticCollector<JavaFileObject> diagnosticCollector) {
        this.diagnosticCollector = diagnosticCollector;
    }

    @Override
    public boolean processMessageLine(Callback callback) {
        for (Diagnostic<? extends JavaFileObject> diagnostic : diagnosticCollector.getDiagnostics()) {
            callback.message(convert(diagnostic.getKind()), diagnostic.getMessage(Locale.getDefault()),
                    diagnostic.getSource().getName(), ((Long) diagnostic.getLineNumber()).intValue(), ((Long) diagnostic.getColumnNumber()).intValue());
        }
        return super.processMessageLine(callback);
    }

    private CompilerMessageCategory convert(Diagnostic.Kind kind) {
        return conversions.get(kind).get();
    }
}
