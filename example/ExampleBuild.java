import com.googlecode.compilo.Build;

import java.io.File;

import static com.googlecode.totallylazy.Files.directory;
import static com.googlecode.totallylazy.Files.workingDirectory;

public class ExampleBuild extends Build.Convention {
    public ExampleBuild(File root) {
        super(root);
    }

    @Override
    public Locations locations() {
        return new Locations(root(), identifiers()) {
            public File lib() { return directory(root(), "../lib"); }
        };
    }

    @Override
    public Build.Identifiers identifiers() {
        return new Identifiers() {
            public String group() { return "com.example"; }
            public String artifact() { return "example"; }
        };
    }

    public static void main(String[] args) throws Exception {
        new ExampleBuild(directory(workingDirectory(), "example")).build();
    }
}
