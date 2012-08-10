import static com.googlecode.totallylazy.Runnables.printLine;
import static com.googlecode.totallylazy.Sequences.repeat;

public class HelloWorld {
    public static void main(String[] args) {
        repeat(printLine("Hello World")).take(10).realise();
    }
}
