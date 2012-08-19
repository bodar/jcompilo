package com.googlecode.compilo;

import org.junit.Test;

import static com.googlecode.compilo.Environment.constructors.environment;
import static com.googlecode.totallylazy.Files.directory;
import static com.googlecode.totallylazy.Files.workingDirectory;
import static com.googlecode.totallylazy.matchers.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class BootStrapTest {
    @Test
    public void canBuildExampleProject() throws Exception {
        int exitCode = new BootStrap(environment(directory(workingDirectory(), "example"))).build();
        assertThat(exitCode, is(0));
    }
}
