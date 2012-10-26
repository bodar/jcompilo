package com.example;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import static com.example.Gcd.gcdInt;
import static com.example.Gcd.gcdIntIterative;
import static com.example.Gcd.gcdLong;
import static com.example.Gcd.gcdLongIterative;
import static com.example.Gcd.gcdWithLocalVariable;
import static com.example.RecursiveZipper.zipper;
import static com.googlecode.totallylazy.collections.ImmutableList.constructors.empty;
import static com.googlecode.totallylazy.collections.ImmutableList.constructors.list;
import static com.googlecode.totallylazy.matchers.Matchers.is;
import static com.googlecode.totallylazy.numbers.Numbers.range;
import static org.hamcrest.MatcherAssert.assertThat;

public class TailRecAcceptanceTest {
    @Test
    public void doesNotBlowStack() {
        zipper(empty(Number.class), range(0, 20000).toImmutableList()).top();
    }

    @Test
    public void supportsInstanceMethods() {
        RecursiveZipper<String> zipper = zipper(list("A", "B", "C", "D"));
        final RecursiveZipper<String> newZipper = zipper.next().next();
        Assert.assertThat(newZipper.top(), Matchers.is(zipper));
    }

    @Test
    public void supportsStaticMethods() throws Exception {
        assertThat(gcdInt(259, 111), is(37));
        assertThat(gcdIntIterative(259, 111), is(37));
    }

    @Test
    public void supportsMethodsWith64bitArguments() throws Exception {
        assertThat(gcdLong(259L, 111L), is(37L));
        assertThat(gcdLongIterative(259L, 111L), is(37L));
    }

    @Test
    public void supportsMethodWithLocalVariables() throws Exception {
        assertThat(gcdWithLocalVariable(259, 111), is(37));
        assertThat(gcdWithLocalVariable(259, 111), is(37));
    }
}