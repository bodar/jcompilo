package com.googlecode.jcompilo.lambda;

import org.junit.Test;
import org.objectweb.asm.tree.InsnList;

import static com.googlecode.jcompilo.lambda.LambdaFixture.lambdaCall;
import static com.googlecode.jcompilo.lambda.LambdaFixture.numberIntValue;
import static com.googlecode.totallylazy.matchers.Matchers.is;
import static org.junit.Assert.assertThat;

public class LambdaTest {
    @Test
    public void canRewriteLambdaArgumentsCorrectly() throws Exception {
        InsnList body = lambdaCall();
        assertThat(LambdaHandler.rewriteArguments(body), is(numberIntValue()));
    }
}
