package com.googlecode.jcompilo.tests.junit;

import org.junit.Test;

import static com.googlecode.totallylazy.Assert.assertThat;
import static com.googlecode.totallylazy.predicates.Predicates.is;

public class TestExecutorTest {
    @Test
    public void classNameDoesNotReplaceJavaInPath() throws Exception {
        String result = TestExecutor.className("foo/bar/javascript/Source.java");
        assertThat(result, is("foo.bar.javascript.Source"));
    }

}