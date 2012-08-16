package com.googlecode.compilo;

import org.junit.Test;

import static com.googlecode.totallylazy.Files.directory;
import static com.googlecode.totallylazy.Files.workingDirectory;

public class MainTest {
    @Test
    public void canBuildExampleProject() throws Exception {
        new Main(directory(workingDirectory(), "example"), System.getProperties(), System.out).build();
    }
}
