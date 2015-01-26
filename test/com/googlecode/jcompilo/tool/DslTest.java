package com.googlecode.jcompilo.tool;

import com.googlecode.totallylazy.Sequences;
import org.junit.Test;

import javax.tools.DiagnosticCollector;
import javax.tools.StandardJavaFileManager;

import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Locale;

import static com.googlecode.jcompilo.tool.Dsl.compiler;
import static com.googlecode.jcompilo.tool.Dsl.tailrec;

public class DslTest {
    @Test
    public void canCreateVanillaCompiler() throws Exception {
        JCompiler compiler = compiler();
    }

    @Test
    public void canAddTailRecSupport() throws Exception {
        JCompiler compiler = compiler(tailrec());
//        DiagnosticCollector diagnosticListener = new DiagnosticCollector();
//        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnosticListener, Locale.getDefault(), Charset.defaultCharset());
//
//        compiler.getTask(new OutputStreamWriter(System.out), fileManager, diagnosticListener, Sequences.empty(String.class), )
    }


}