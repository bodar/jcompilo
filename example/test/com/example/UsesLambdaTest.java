package com.example;

import com.googlecode.totallylazy.Sequence;
import org.junit.Test;

import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.matchers.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class UsesLambdaTest {
    @Test
    public void canProcessLambda() throws Exception {
        Sequence<Character> characters = new UsesLambda().lambdaExample();
        assertThat(characters, is(sequence('d', 'm')));
    }
}
