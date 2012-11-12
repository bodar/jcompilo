import com.googlecode.jcompilo.Environment;
import com.googlecode.jcompilo.convention.AutoBuild;

import static com.googlecode.jcompilo.Compiler.compiler;
import static com.googlecode.jcompilo.asm.AsmResourceHandler.asmResourceHandler;

public class build extends AutoBuild {
    // We only need this constructor for testing the build!
    public build(Environment environment) {
        super(environment);
    }
}
