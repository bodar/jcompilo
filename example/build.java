import com.googlecode.compilo.convention.AutoBuild;
import com.googlecode.compilo.convention.BuildConvention;

import java.io.File;
import java.io.PrintStream;
import java.util.Properties;

import static com.googlecode.totallylazy.Files.directory;
import static com.googlecode.totallylazy.Files.workingDirectory;

public class build extends AutoBuild {
    public File libDir() { return directory(rootDir(), "../lib"); }

    // We only need this constructor for testing the build!
    public build(File root, Properties properties, PrintStream out) {
        super(root, properties, out);
    }
}
