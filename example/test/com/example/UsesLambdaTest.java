package com.example;

import org.junit.Test;

import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.matchers.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class UsesLambdaTest {
    @Test
    public void handlesSingleLambda() throws Exception {
        assertThat(new UsesLambda().singleLambda(), is(sequence('d', 'm')));
    }

    @Test
    public void handlesMultipleLambdas() throws Exception {
        assertThat(new UsesLambda().multipleLambdas(), is(sequence('D', 'M')));
    }
}
