import com.googlecode.compilo.Build;
import com.googlecode.compilo.Environment;
import com.googlecode.compilo.convention.AutoBuild;
import com.googlecode.totallylazy.annotations.tailrec;

import static com.googlecode.compilo.Compiler.compiler;
import static com.googlecode.compilo.tco.AsmResourceHandler.asmResourceHandler;
import static com.googlecode.compilo.tco.TailRecHandler.tailRecHandler;

public class build extends AutoBuild {
    // We only need this constructor for testing the build!
    public build(Environment environment) {
        super(environment);
    }

    @Override
    public Build compile() throws Exception {
        stage("compile");
        compiler(env, dependencies(), compileOptions()).
                add(asmResourceHandler(true).add(tailrec.class, tailRecHandler())).
                compile(srcDir(), mainJar());
        return this;

    }
}
