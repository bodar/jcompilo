package com.googlecode.compilo.intellij;

import com.intellij.openapi.compiler.CompileContext;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;
import java.util.Locale;

import static com.googlecode.compilo.intellij.CompiloOutputParser.convert;
import static com.googlecode.compilo.intellij.CompiloOutputParser.uri;
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
}
