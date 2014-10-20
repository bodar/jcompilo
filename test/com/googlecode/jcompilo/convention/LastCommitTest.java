package com.googlecode.jcompilo.convention;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import static com.googlecode.jcompilo.Processes.execute;
import static com.googlecode.jcompilo.Processes.executeReturnString;
import static com.googlecode.totallylazy.Files.emptyTemporaryDirectory;
import static com.googlecode.totallylazy.Files.file;
import static com.googlecode.totallylazy.Files.workingDirectory;
import static java.lang.System.out;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class LastCommitTest {
    @BeforeClass
    public static void checkGitInstalled() throws Exception {
        try {
            execute("git", workingDirectory());
        } catch (IOException e) {
            org.junit.Assume.assumeTrue(false);
        }
    }

    @Test
    public void supportsGit() throws Exception {
        File directory = emptyTemporaryDirectory(LastCommitTest.class.getName());
        out.println(executeReturnString("git init", directory));
        file(directory, "afile.txt");
        out.println(executeReturnString("git add .", directory));
        out.println(executeReturnString("git commit -m \"Hello\"", directory));

        Properties properties = LastCommit.lastCommitData(directory);

        assertThat(properties.getProperty("summary"), is("Hello"));
        assertThat(properties.getProperty("user"), is(notNullValue()));
    }
}