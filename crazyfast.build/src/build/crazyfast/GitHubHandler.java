package build.crazyfast;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.googlecode.jcompilo.tool.JCompiler;
import com.googlecode.totallylazy.Maps;
import com.googlecode.totallylazy.json.Json;

import java.io.*;
import java.util.Map;

import static com.googlecode.totallylazy.Maps.map;
import static com.googlecode.totallylazy.Strings.string;
import static com.googlecode.totallylazy.json.Json.json;

public class GitHubHandler implements RequestStreamHandler {
    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
        Map<String, Object> map = Json.map(string(input));
        Writer writer = new OutputStreamWriter(output);
        writer.append(json(map(
                "commit", map.get("after"),
                "compiler", JCompiler.defaultCompiler())));
        writer.close();
    }
}
