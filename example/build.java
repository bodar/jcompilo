import com.googlecode.compilo.convention.BuildConvention;

import java.io.File;
import java.io.PrintStream;
import java.util.Properties;

import static com.googlecode.totallylazy.Files.directory;
import static com.googlecode.totallylazy.Files.workingDirectory;

public class build extends BuildConvention {
    public File libDir() { return directory(rootDir(), "../lib"); }
    public String group() { return "com.example"; }
    public String artifact() { return "example"; }

    // We only need this constructor for testing the build!
    public build(File root, Properties properties, PrintStream out) {
        super(root, properties, out);
    }
}
