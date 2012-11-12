package com.googlecode.jcompilo.convention;

import com.googlecode.jcompilo.Environment;
import com.googlecode.jcompilo.Identifiers;
import org.junit.Test;

import static com.googlecode.totallylazy.Files.directory;
import static com.googlecode.totallylazy.Files.workingDirectory;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class AutoBuildTest {
    @Test
    public void canGuessGroupIdentifier() throws Exception {
        Identifiers identifiers = new AutoBuild(Environment.constructors.environment(directory(workingDirectory(), "example")));
        assertThat(identifiers.group(), is("com.example"));
        assertThat(identifiers.artifact(), is("example"));
    }
}
