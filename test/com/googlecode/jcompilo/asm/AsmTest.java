package com.googlecode.jcompilo.asm;

import org.junit.Test;

import static com.googlecode.totallylazy.matchers.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static jdk.internal.org.objectweb.asm.Type.getType;

public class AsmTest {
    @Test
    public void types() throws Exception {
        assertThat(getType(AsmTest.class).equals(getType("Lcom/googlecode/jcompilo/asm/AsmTest;")), is(true));
        assertThat(getType(AsmTest.class).equals(getType("com/googlecode/jcompilo/asm/AsmTest")), is(false));
    }
}
