package build.crazyfast;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.googlecode.totallylazy.json.Json;

import java.io.*;
import java.util.Map;

import static com.googlecode.totallylazy.Strings.string;

public class GitHubHandler implements RequestStreamHandler {
    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
        Map<String, Object> map = Json.map(string(input));
        Writer writer = new OutputStreamWriter(output);
        writer.append("Done " + map.get("after"));
        writer.close();
    }
}
