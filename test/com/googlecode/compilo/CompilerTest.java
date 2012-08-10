package com.googlecode.compilo;

import com.googlecode.totallylazy.Sequence;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

import static com.googlecode.compilo.Compiler.compiler;
import static com.googlecode.totallylazy.Files.directory;
import static com.googlecode.totallylazy.Files.emptyTemporaryDirectory;
import static com.googlecode.totallylazy.Files.file;
import static com.googlecode.totallylazy.Files.hasSuffix;
import static com.googlecode.totallylazy.Files.recursiveFiles;
import static com.googlecode.totallylazy.Files.temporaryFile;
import static com.googlecode.totallylazy.Files.workingDirectory;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class CompilerTest {
    private Compiler compiler;
    private Sequence<File> dependancies;

    @Before
    public void setUp() throws Exception {
        compiler = compiler();
        dependancies = jars(workingDirectory(), "lib");
    }

    @Test
    public void canCompilerADirectory() throws Exception {
        File input = directory(workingDirectory(), "example");
        File output = emptyTemporaryDirectory("compilo");
        assertThat(compiler.compile(input, output, dependancies), is(true));
        assertThat(new File(output, "HelloWorld.class").exists(), is(true));
    }

    @Test
    public void copiesResourcesByDefault() throws Exception {
        File input = directory(workingDirectory(), "example");
        File output = emptyTemporaryDirectory("compilo");
        assertThat(compiler.compile(input, output, dependancies), is(true));
        assertThat(new File(output, "resource.txt").exists(), is(true));
    }

    @Test
    public void copiesResourcesInSubPackages() throws Exception {
        File input = directory(workingDirectory(), "example");
        File output = emptyTemporaryDirectory("compilo");
        assertThat(compiler.compile(input, output, dependancies), is(true));
        assertThat(new File(output, "sub/another.txt").exists(), is(true));
    }

    @Test
    @Ignore("WIP")
    public void canCompileToJar() throws Exception {
        File input = directory(workingDirectory(), "example");
        File output = file(emptyTemporaryDirectory("compilo"), "foo.jar");
        assertThat(compiler.compile(input, output, dependancies), is(true));
    }

    @Test
    public void canCompileTL() throws Exception {
        File totallylazy = directory(workingDirectory(), "../totallylazy/");
        File src = directory(totallylazy, "src");
        File output = emptyTemporaryDirectory("tl");
        Sequence<File> dependancies = jars(totallylazy, "lib");
        assertThat(compiler.compile(src, output, dependancies), is(true));

        File test = directory(totallylazy, "test");
        assertThat(compiler.compile(test, output, dependancies.cons(output)), is(true));
    }

    private Sequence<File> jars(File totallylazy, final String name) {
        return recursiveFiles(directory(totallylazy, name)).filter(hasSuffix("jar")).realise();
    }
}
