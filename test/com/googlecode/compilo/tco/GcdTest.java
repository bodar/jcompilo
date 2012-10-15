package com.googlecode.compilo.tco;

import org.junit.Test;

import static com.googlecode.compilo.tco.Gcd.gcd;
import static com.googlecode.compilo.tco.Gcd.iterativeGcd;
import static com.googlecode.totallylazy.matchers.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class GcdTest {
    @Test
    public void shouldWork() throws Exception {
        assertThat(gcd(259, 111), is(37));
        assertThat(iterativeGcd(259, 111), is(37));
    }
}
