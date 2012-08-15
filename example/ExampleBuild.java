import com.googlecode.compilo.convention.BuildConvention;

import java.io.File;
import java.io.PrintStream;
import java.util.Properties;

import static com.googlecode.totallylazy.Files.directory;
import static com.googlecode.totallylazy.Files.workingDirectory;

public class ExampleBuild extends BuildConvention {
    public File libDir() { return directory(rootDir(), "../lib"); }
    public String group() { return "com.example"; }
    public String artifact() { return "example"; }

    public static void main(String[] args) throws Exception {
        new ExampleBuild(directory(workingDirectory(), "example"), System.getProperties(), System.out).build();
    }

    public ExampleBuild(File root, Properties properties, PrintStream out) {
        super(root, properties, out);
    }

    public ExampleBuild() {}
}
