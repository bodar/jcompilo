package com.googlecode.jcompilo.lambda;

import com.googlecode.jcompilo.asm.Asm;
import com.googlecode.totallylazy.Sequence;
import org.junit.Ignore;
import org.junit.Test;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

import static com.googlecode.jcompilo.Resource.constructors.resource;
import static com.googlecode.jcompilo.asm.Asm.instructions;
import static com.googlecode.jcompilo.asm.AsmResourceHandler.asmResourceHandler;
import static com.googlecode.jcompilo.lambda.ClassGenerator.classGenerator;
import static com.googlecode.jcompilo.lambda.LambdaFixture.localArgumentLambda;
import static com.googlecode.jcompilo.lambda.LambdaFixture.numberLambda;
import static com.googlecode.jcompilo.lambda.LambdaFixture.numberIntValue;
import static com.googlecode.jcompilo.lambda.LambdaFixture.stringCharAt;
import static com.googlecode.totallylazy.Predicates.nullValue;
import static com.googlecode.totallylazy.matchers.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class LambdaTest {
    @Test
    public void canRewriteLambdaArgumentsCorrectly() throws Exception {
        InsnList body = numberLambda();
        System.out.println(Asm.toString(body));
        assertThat(LambdaHandler.functionalInterface(body), is(numberIntValue()));
    }

    @Test
    public void worksWithAFunction() throws Exception {
        ClassNode classNode = Asm.classNode(LambdaFieldInput.class);
        LambdaHandler handler = new LambdaHandler(classGenerator(new ClassResources()));
        Sequence<ClassNode> classNodes = handler.process(classNode, (MethodNode) classNode.methods.get(1));
        assertThat(classNodes.size(), is(2));
    }

    @Test
    @Ignore("WIP")
    public void rewritesClosedOverLocalVariables() throws Exception {
        InsnList body = localArgumentLambda();
        assertThat(LambdaHandler.functionalInterface(body), is(stringCharAt()));
    }
}
