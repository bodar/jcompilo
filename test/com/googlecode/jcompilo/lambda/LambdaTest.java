package com.googlecode.jcompilo.lambda;

import com.googlecode.jcompilo.asm.Asm;
import com.googlecode.totallylazy.Sequence;
import org.junit.Ignore;
import org.junit.Test;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

import static com.googlecode.jcompilo.lambda.ClassGenerator.classGenerator;
import static com.googlecode.jcompilo.lambda.LambdaFixture.lambdaCall;
import static com.googlecode.jcompilo.lambda.LambdaFixture.numberIntValue;
import static com.googlecode.totallylazy.matchers.Matchers.is;
import static org.junit.Assert.assertThat;

public class LambdaTest {
    @Test
    @Ignore("Temp disabe")
    public void canRewriteLambdaArgumentsCorrectly() throws Exception {
        InsnList body = lambdaCall();
        assertThat(LambdaHandler.functionalInterface(body), is(numberIntValue()));
    }

    @Test
    public void worksWithAFunction() throws Exception {
        ClassNode classNode = Asm.classNode(LambdaFieldInput.class);
        LambdaHandler handler = new LambdaHandler(classGenerator(new ClassResources()));
        Sequence<ClassNode> classNodes = handler.process(classNode, (MethodNode) classNode.methods.get(1));
        assertThat(classNodes.size(), is(2));
    }
}
