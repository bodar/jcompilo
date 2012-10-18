package com.example;

import org.junit.Test;

import static com.example.Gcd.gcdInt;
import static com.example.Gcd.gcdIntIterative;
import static com.example.Gcd.gcdLong;
import static com.example.Gcd.gcdLongIterative;
import static com.googlecode.totallylazy.matchers.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class GcdTest {
    @Test
    public void supportsIntegers() throws Exception {
        assertThat(gcdInt(259, 111), is(37));
        assertThat(gcdIntIterative(259, 111), is(37));
    }

    @Test
    public void supportsLongs() throws Exception {
        assertThat(gcdLong(259L, 111L), is(37L));
        assertThat(gcdLongIterative(259L, 111L), is(37L));
    }
}