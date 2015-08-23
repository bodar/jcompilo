package build.crazyfast;

import org.junit.Test;

import java.io.ByteArrayOutputStream;

import static com.googlecode.totallylazy.Assert.assertThat;
import static com.googlecode.totallylazy.predicates.Predicates.is;

public class GitHubHandlerTest {
    @Test
    public void supportsBeingTriggeredFromGitHub() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        new GitHubHandler().handleRequest(getClass().getResourceAsStream("push.json"), outputStream, null);
        assertThat(outputStream.toString(), is("Done 13c3f0831b21aaa4e81917d6f683c6faca47f8cf"));
    }
}