import com.googlecode.compilo.Environment;
import com.googlecode.compilo.convention.AutoBuild;
import com.googlecode.compilo.convention.BuildConvention;

import java.io.File;
import java.io.PrintStream;
import java.util.Properties;

import static com.googlecode.totallylazy.Files.directory;
import static com.googlecode.totallylazy.Files.workingDirectory;

public class build extends AutoBuild {
    // We only need this constructor for testing the build!
    public build(Environment environment) {
        super(environment);
    }
}
