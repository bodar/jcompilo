package com.googlecode.jcompilo.lambda;

import com.googlecode.jcompilo.asm.Asm;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import org.junit.Test;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

import static com.googlecode.jcompilo.lambda.ClassGenerator.classGenerator;
import static com.googlecode.jcompilo.lambda.LambdaFixture.localArgumentLambda;
import static com.googlecode.jcompilo.lambda.LambdaFixture.numberIntValue;
import static com.googlecode.jcompilo.lambda.LambdaFixture.numberLambda;
import static com.googlecode.jcompilo.lambda.LambdaFixture.localVariableClosure;
import static com.googlecode.totallylazy.matchers.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.objectweb.asm.Type.getType;

public class LambdaTest {
    @Test
    public void canRewriteLambdaArgumentsCorrectly() throws Exception {
        InsnList body = numberLambda();
        assertThat(LambdaHandler.functionalInterface(body, Sequences.<Type>empty()), is(numberIntValue()));
    }

    @Test
    public void worksWithAFunction() throws Exception {
        ClassNode classNode = Asm.classNode(LambdaFieldInput.class);
        LambdaHandler handler = new LambdaHandler(classGenerator(new ClassResources()));
        Sequence<ClassNode> classNodes = handler.process(classNode, (MethodNode) classNode.methods.get(1));
        assertThat(classNodes.size(), is(2));
    }

    @Test
    public void rewritesClosedOverLocalVariables() throws Exception {
        InsnList body = localArgumentLambda();
        assertThat(LambdaHandler.functionalInterface(body, Sequences.<Type>sequence(getType("Lcom/example/UsesLambda;"), getType("I"))), is(localVariableClosure()));
    }
}
