package com.googlecode.jcompilo.tco;

import com.googlecode.jcompilo.Resource;
import com.googlecode.totallylazy.Files;
import com.googlecode.totallylazy.Segment;
import com.googlecode.totallylazy.annotations.tailrec;
import org.junit.Test;

import static com.googlecode.jcompilo.Resource.constructors.resource;
import static com.googlecode.jcompilo.asm.AsmResourceHandler.asmResourceHandler;
import static com.googlecode.jcompilo.tco.TailRecHandler.tailRecHandler;
import static com.googlecode.totallylazy.Files.file;
import static com.googlecode.totallylazy.Segment.constructors.segment;
import static com.googlecode.totallylazy.Segment.constructors.unique;
import static com.googlecode.totallylazy.numbers.Numbers.*;

public class TailRecHandlerTest {
    @Test
    public void canProcessAResource() throws Exception {
        Resource resource = asmResourceHandler().add(tailrec.class, tailRecHandler()).
                handle(resource(TailRecursive.class)).head();
        Files.write(resource.bytes(), file(Files.temporaryDirectory(TailRecHandlerTest.class.getSimpleName()), resource.name()));
    }

    static class TailRecursive {
        @tailrec
        TailRecursive top() {
            if (isTop()) return this;
            return up().top();
        }

        TailRecursive up() {
            return this;
        }

        boolean isTop() {
            return false;
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldNotAllowMethodsThatAreNotFullyTailRecursive() throws Exception {
        asmResourceHandler().add(tailrec.class, tailRecHandler()).
                handle(resource(NotQuiteTailRecursive.class));
    }

    static class NotQuiteTailRecursive {
        @tailrec
        static Segment<Number> factor(Segment<Number> primes, Number number) {
            Number prime = primes.head();
            if (greaterThan(squared(prime), number)) return segment(number);
            if (isZero(remainder(number, prime))) return unique(prime, factor(primes, quotient(number, prime)));
            return factor(primes.tail(), number);
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void doesNotSupportVoidRecursiveMethods() throws Exception {
        asmResourceHandler().add(tailrec.class, tailRecHandler()).
                handle(resource(VoidTailRecursive.class));
    }

    static class VoidTailRecursive {
        @tailrec
        static <T> void print(Segment<T> segment) {
            System.out.printf("%s ", segment.head());
            print(segment.tail());
        }
    }
}