package com.googlecode.jcompilo;

import com.googlecode.jcompilo.tool.JCompiler;
import com.googlecode.totallylazy.Callables;
import com.googlecode.totallylazy.Destination;
import com.googlecode.totallylazy.Curried2;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sources;

import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import java.io.File;
import java.io.OutputStreamWriter;

import static com.googlecode.jcompilo.CompileOption.Debug;
import static com.googlecode.jcompilo.CompileOption.Implicit;
import static com.googlecode.jcompilo.CompileOption.Implicit.None;
import static com.googlecode.totallylazy.Closeables.using;
import static com.googlecode.totallylazy.Sequences.sequence;

public class CompileProcessor implements Processor {
    @Deprecated
    public static final JavaCompiler DEFAULT_COMPILER = JCompiler.DEFAULT_COMPILER;
    public static final Sequence<CompileOption> DEFAULT_OPTIONS = sequence(Debug, Implicit(None));
    private final Environment env;
    private final JavaCompiler compiler;
    private final Option<DiagnosticListener<JavaFileObject>> diagnosticListener;
    private final Sequence<CompileOption> options;
    private final CompilerResources resources;

    private CompileProcessor(Environment env, JavaCompiler compiler, Iterable<CompileOption> options, Option<DiagnosticListener<JavaFileObject>> diagnosticListener, final CompilerResources resources) {
        this.env = env;
        this.compiler = compiler;
        this.diagnosticListener = diagnosticListener;
        this.options = sequence(options);
        this.resources = resources;
    }

    public static CompileProcessor compile(final Environment env, final Iterable<CompileOption> options, final JavaCompiler compiler, Iterable<File> dependancies, Option<DiagnosticListener<JavaFileObject>> diagnosticListener) {
        return compile(env, options, compiler, new CompilerResources(compiler, dependancies), diagnosticListener);
    }

    public static CompileProcessor compile(final Environment env, final Iterable<CompileOption> options, final JavaCompiler compiler, final CompilerResources resources, final Option<DiagnosticListener<JavaFileObject>> diagnosticListener) {
        return new CompileProcessor(env, compiler, options, diagnosticListener, resources);
    }

    public static boolean compile(final Environment env, final Iterable<File> dependancies, Sources source, Destination destination) throws Exception {
        return using(source, destination, new Curried2<Sources, Destination, Boolean>() {
            @Override
            public Boolean call(Sources source, Destination destination) throws Exception {
                return compile(env, DEFAULT_OPTIONS, JCompiler.DEFAULT_COMPILER, dependancies, Option.<DiagnosticListener<JavaFileObject>>none()).process(Inputs.constructors.inputs(source), Outputs.constructors.output(destination));
            }
        });
    }

    @Override
    public boolean process(Inputs sources, Outputs outputs) throws Exception {
        env.out().prefix("    [javac] ");
        env.out().printf("Compiling %s source files%n", sources.size());
        Boolean success = compiler.getTask(new OutputStreamWriter(env.out()), resources.output(outputs), diagnosticListener.getOrNull(), options.flatMap(Callables.<Iterable<String>>value()), null, javaFileObjects(sources)).call();
        env.out().clearPrefix();
        if (!success) env.out().println("Compile failed");
        return success;
    }

    private Sequence<JavaFileObject> javaFileObjects(Inputs javaFiles) {
        return sequence(javaFiles).map(SourceFileObject.sourceFileObject());
    }

    @Override
    public boolean matches(String other) {
        return Compiler.JAVA_FILES.matches(other);
    }
}
