import com.googlecode.compilo.Build;
import com.googlecode.compilo.Environment;
import com.googlecode.compilo.convention.AutoBuild;
import com.googlecode.totallylazy.annotations.tailrec;

import static com.googlecode.compilo.Compiler.compiler;
import static com.googlecode.compilo.asm.AsmResourceHandler.asmResourceHandler;
import static com.googlecode.compilo.tco.TailRecHandler.tailRecHandler;

public class build extends AutoBuild {
    // We only need this constructor for testing the build!
    public build(Environment environment) {
        super(environment);
    }
}
