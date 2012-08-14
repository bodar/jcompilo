package com.googlecode.compilo;

import com.googlecode.totallylazy.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.googlecode.compilo.Compiler.compiler;
import static com.googlecode.totallylazy.Closeables.using;
import static com.googlecode.totallylazy.Files.*;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.ZipDestination.zipDestination;
import static com.googlecode.totallylazy.predicates.WherePredicate.where;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

public class CompilerTest {
    private Compiler compiler;
    private File compilo;
    private File workingDirectory;

    @Before
    public void setUp() throws Exception {
        workingDirectory = new File(".");
        compiler = compiler(jars(workingDirectory, "lib"));
        compilo = emptyTemporaryDirectory("compilo");
    }

    @Test
    public void canCompilerADirectory() throws Exception {
        File input = directory(workingDirectory, "example");
        File output = file(compilo, "compilo.jar");
        assertThat(compiler.compile(input, output).size(), is(greaterThan(0)));
        assertThat(jarContains(output, "HelloWorld.class"), is(true));
        assertThat(jarContains(output, "resource.txt"), is(true));
        assertThat(jarContains(output, "sub/another.txt"), is(true));
    }

    private static boolean jarContains(File jar, final String name) throws FileNotFoundException {
        return using(new ZipInputStream(new FileInputStream(jar)), new Function1<ZipInputStream, Boolean>() {
            @Override
            public Boolean call(ZipInputStream zipInputStream) throws Exception {
                return Zip.entries(zipInputStream).exists(where(name(), Predicates.is(name)));
            }
        });
    }

    private static Function1<ZipEntry, String> name() {
        return new Function1<ZipEntry, String>() {
            @Override
            public String call(ZipEntry zipEntry) throws Exception {
                return zipEntry.getName();
            }
        };
    }

    @Test
    @Ignore("Manual")
    public void canCompileTL() throws Exception {
        Sequence<?> options = sequence(CompileOption.Debug, CompileOption.UncheckedWarnings, CompileOption.WarningAsErrors, CompileOption.Target, 6, CompileOption.Source, 6);
        File totallylazy = directory(workingDirectory, "../totallylazy/");
        Sequence<File> dependencies = jars(totallylazy, "lib");
        Compiler compiler = Compiler.compiler(dependencies, options);
        File src = directory(totallylazy, "src");
        File output = file(compilo, "totallylazy.jar");
        assertThat(compiler.compile(src, output).size(), is(greaterThan(0)));

        File test = directory(totallylazy, "test");
        File testJar = file(compilo, "totallylazy-test.jar");
        TestProcessor testProcessor = new TestProcessor();
        Sequence<File> productionDependencies = dependencies.cons(output);
        compiler = Compiler.compiler(productionDependencies, options).add(testProcessor);
        assertThat(compiler.compile(test, testJar).size(), is(greaterThan(0)));

        assertThat(execute(testProcessor.tests(), productionDependencies.cons(testJar)), is(true));
    }

    private boolean execute(Sequence<String> tests, Sequence<File> jars) throws Exception {
        File testExecutor = file(compilo, "compilo.test.hook.jar");
        Destination destination = zipDestination(new FileOutputStream(testExecutor));

        Source source = sourceOf(TestExecutor.class, TestExecutor.ResultCallable.class, TestExecutor.NullOutputStream.class, TestExecutor.NullPrintStream.class);
        Source.methods.copyAndClose(source, destination);

        final URLClassLoader classLoader = new URLClassLoader(asUrls(jars.cons(testExecutor).toList()), null);

        Class<?> executor = classLoader.loadClass(TestExecutor.class.getName());
        Method execute = Methods.method(executor, "execute", List.class).get();
        return Methods.<TestExecutor, Boolean>invoke(execute, null, tests.toList());
    }

    private Source sourceOf(final Class<?>... classes) {
        return new Source() {
            @Override
            public Sequence<Pair<String, InputStream>> sources() {
                return sequence(classes).map(new Function1<Class<?>, Pair<String, InputStream>>() {
                    @Override
                    public Pair<String, InputStream> call(Class<?> aClass) throws Exception {
                        String className = aClass.getName();
                        String fileName = String.format("%s.class", className.replace('.', '/'));
                        URL url = aClass.getResource("/" + fileName);
                        return Pair.pair(fileName, url.openStream());
                    }
                });
            }

            @Override
            public void close() throws IOException {
            }
        };
    }

    private static URL[] asUrls(List<File> jars) throws MalformedURLException {
        URL[] urls = new URL[jars.size()];
        for (int i = 0; i < jars.size(); i++) {
            urls[i] = jars.get(i).toURI().toURL();
        }
        return urls;
    }

    private Sequence<File> jars(File totallylazy, final String name) {
        return recursiveFiles(directory(totallylazy, name)).filter(hasSuffix("jar")).realise();
    }

}
