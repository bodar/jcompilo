package com.googlecode.compilo;

import com.googlecode.totallylazy.Callables;
import com.googlecode.totallylazy.Characters;
import com.googlecode.totallylazy.Destination;
import com.googlecode.totallylazy.Function2;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sources;

import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;

import static com.googlecode.compilo.CompileOption.Debug;
import static com.googlecode.compilo.CompileOption.Implicit;
import static com.googlecode.compilo.CompileOption.Implicit.None;
import static com.googlecode.totallylazy.Closeables.using;
import static com.googlecode.totallylazy.Files.name;
import static com.googlecode.totallylazy.LazyException.lazyException;
import static com.googlecode.totallylazy.Predicates.not;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Strings.endsWith;
import static javax.tools.StandardLocation.CLASS_PATH;
import static javax.tools.ToolProvider.getSystemJavaCompiler;

public class CompileProcessor implements Processor {
    public static final JavaCompiler DEFAULT_COMPILER = getSystemJavaCompiler();
    public static final Sequence<CompileOption> DEFAULT_OPTIONS = sequence(Debug, Implicit(None));
    private final Environment env;
    private final JavaCompiler compiler;
    private final Option<DiagnosticListener<JavaFileObject>> diagnosticListener;
    private final Sequence<CompileOption> options;
    private final StandardJavaFileManager standardFileManager;

    private CompileProcessor(Environment env, JavaCompiler compiler, Iterable<CompileOption> options, Iterable<File> dependancies, Option<DiagnosticListener<JavaFileObject>> diagnosticListener) {
        this.env = env;
        this.compiler = compiler;
        this.diagnosticListener = diagnosticListener;
        this.options = sequence(options);
        standardFileManager = compiler.getStandardFileManager(null, null, Characters.UTF8);
        setDependencies(sequence(dependancies).filter(where(name(), not(endsWith("-sources.jar")))));
    }

    public static CompileProcessor compile(Environment env, final Iterable<CompileOption> options, final JavaCompiler compiler, Iterable<File> dependancies, Option<DiagnosticListener<JavaFileObject>> diagnosticListener) {
        return new CompileProcessor(env, compiler, options, dependancies, diagnosticListener);
    }

    public static boolean compile(final Environment env, final Iterable<File> dependancies, Sources source, Destination destination) throws Exception {
        return using(source, destination, new Function2<Sources, Destination, Boolean>() {
            @Override
            public Boolean call(Sources source, Destination destination) throws Exception {
                return compile(env, DEFAULT_OPTIONS, DEFAULT_COMPILER, dependancies, Option.<DiagnosticListener<JavaFileObject>>none()).process(Inputs.constructors.inputs(source), Outputs.constructors.output(destination));
            }
        });
    }

    @Override
    public boolean process(Inputs sources, Outputs outputs) throws Exception {
        env.out().prefix("    [javac] ");
        env.out().printf("Compiling %s source files%n", sources.size());
        Boolean success = compiler.getTask(new OutputStreamWriter(env.out()), manager(outputs), diagnosticListener.getOrNull(), options.flatMap(Callables.<Iterable<String>>value()), null, javaFileObjects(sources)).call();
        env.out().clearPrefix();
        if (!success) throw new IllegalStateException("Compile failed");
        return success;
    }

    private JavaFileManager manager(final Outputs outputs) throws FileNotFoundException {
        return new OutputsManager(standardFileManager, outputs);
    }

    private Sequence<JavaFileObject> javaFileObjects(Inputs javaFiles) {
        return sequence(javaFiles).map(SourceFileObject.sourceFileObject());
    }

    private void setDependencies(Sequence<File> dependancies) {
        try {
            if (dependancies.isEmpty()) return;
            standardFileManager.setLocation(CLASS_PATH, dependancies);
        } catch (IOException e) {
            throw lazyException(e);
        }
    }

    @Override
    public boolean matches(String other) {
        return Compiler.JAVA_FILES.matches(other);
    }
}
