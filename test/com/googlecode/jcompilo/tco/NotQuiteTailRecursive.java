package com.googlecode.jcompilo.tco;

import com.googlecode.totallylazy.Segment;
import com.googlecode.totallylazy.annotations.tailrec;

import static com.googlecode.totallylazy.Segment.constructors.segment;
import static com.googlecode.totallylazy.Segment.constructors.unique;
import static com.googlecode.totallylazy.numbers.Numbers.*;

public class NotQuiteTailRecursive {
    @tailrec
    static Segment<Number> factor(Segment<Number> primes, Number number) {
        Number prime = primes.head();
        if (greaterThan(squared(prime), number)) return segment(number);
        if (isZero(remainder(number, prime))) return unique(prime, factor(primes, quotient(number, prime)));
        return factor(primes.tail(), number);
    }
}
