package com.googlecode.jcompilo;

import com.googlecode.totallylazy.Lists;
import com.googlecode.totallylazy.Strings;
import org.junit.Test;

import java.io.File;

import static com.googlecode.jcompilo.Environment.constructors.environment;
import static com.googlecode.totallylazy.Files.directory;
import static com.googlecode.totallylazy.Files.workingDirectory;
import static com.googlecode.totallylazy.matchers.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;

public class BootStrapTest {
    @Test
    public void canBuildExampleProject() throws Exception {
//        System.setProperty("jcompilo.debug", "true");
        File exampleProject = directory(workingDirectory(), "example");
        int exitCode = new BootStrap(environment(exampleProject)).build(Lists.<String>list());
        assertThat(exitCode, is(0));
        File pom = new File(exampleProject, "build/artifacts/example-dev.build.pom");
        assertThat(pom.toString(), pom.exists(), is(true));
        assertThat(Strings.toString(pom), not(""));
    }
}