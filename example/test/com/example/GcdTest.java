package com.example;

import org.junit.Test;

import static com.example.Gcd.gcd;
import static com.googlecode.totallylazy.matchers.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class GcdTest {
    @Test
    public void shouldWork() throws Exception {
        assertThat(gcd(259, 111), is(37));
    }
}