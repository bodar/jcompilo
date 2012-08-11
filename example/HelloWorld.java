import com.googlecode.totallylazy.Function;
import com.googlecode.totallylazy.lambda.Lambdas;

import static com.googlecode.totallylazy.Runnables.printLine;
import static com.googlecode.totallylazy.Sequences.repeat;
import static com.googlecode.totallylazy.lambda.Lambdas.λ;

public class HelloWorld {
    public static void main(String[] args) {
        repeat(λ("Hello World")).take(10).realise();
    }
}
