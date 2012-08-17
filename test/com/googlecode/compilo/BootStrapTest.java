package com.googlecode.compilo;

import org.junit.Test;

import static com.googlecode.totallylazy.Files.directory;
import static com.googlecode.totallylazy.Files.workingDirectory;

public class BootStrapTest {
    @Test
    public void canBuildExampleProject() throws Exception {
        new BootStrap(directory(workingDirectory(), "example"), System.getProperties(), System.out).build();
    }
}
