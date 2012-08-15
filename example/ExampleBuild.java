import com.googlecode.compilo.Build;

public class ExampleBuild extends Build.Convention {
    @Override
    public Build.Identifiers identifiers() {
        return new Build.Convention.Identifiers(){
            @Override
            public String group() {
                return "com.example";
            }

            @Override
            public String artifact() {
                return "example";
            }
        };
    }
}
