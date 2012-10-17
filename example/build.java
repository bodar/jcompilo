import com.googlecode.compilo.Build;
import com.googlecode.compilo.Environment;
import com.googlecode.compilo.convention.AutoBuild;
import com.googlecode.compilo.convention.BuildConvention;
import com.googlecode.totallylazy.annotations.tailrec;

import java.io.File;
import java.io.PrintStream;
import java.util.Properties;

import static com.googlecode.compilo.Compiler.compiler;
import static com.googlecode.compilo.tco.AsmResourceHandler.asmResourceHandler;
import static com.googlecode.compilo.tco.TailRecHandler.tailRecHandler;
import static com.googlecode.totallylazy.Files.directory;
import static com.googlecode.totallylazy.Files.workingDirectory;

public class build extends AutoBuild {
    // We only need this constructor for testing the build!
    public build(Environment environment) {
        super(environment);
    }

    @Override
    public Build compile() throws Exception {
        stage("compile");
        compiler(env, dependencies(), compileOptions()).
                add(asmResourceHandler(true).add(tailrec.class, tailRecHandler(tailrec.class))).
                compile(srcDir(), mainJar());
        return this;

    }
}
