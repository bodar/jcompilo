package com.googlecode.compilo.convention;

import com.googlecode.compilo.Identifiers;
import org.junit.Test;

import static com.googlecode.totallylazy.Files.directory;
import static com.googlecode.totallylazy.Files.workingDirectory;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class AutoBuildTest {
    @Test
    public void canGuessGroupIdentifier() throws Exception {
        Identifiers identifiers = new AutoBuild(directory(workingDirectory(), "example"), System.getProperties(), System.out);
        assertThat(identifiers.group(), is("com.example"));
        assertThat(identifiers.artifact(), is("example"));
    }
}
