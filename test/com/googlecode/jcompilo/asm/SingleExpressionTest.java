package com.googlecode.jcompilo.asm;

import com.googlecode.jcompilo.lambda.LambdaHandler;
import com.googlecode.totallylazy.Pair;
import org.junit.Test;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;

import static com.googlecode.jcompilo.lambda.LambdaFixture.assertInsn;
import static com.googlecode.jcompilo.lambda.LambdaFixture.numberLambda;
import static com.googlecode.jcompilo.lambda.LambdaFixture.methodWith;
import static com.googlecode.totallylazy.Either.left;
import static com.googlecode.totallylazy.Either.right;

public class SingleExpressionTest {
    @Test
    public void canExtractInstructions() throws Exception {
        InsnList lambdaCall = numberLambda();

        InsnList mutated = methodWith(left(lambdaCall, AbstractInsnNode.class));

        Pair<InsnList, LabelNode> lifted = SingleExpression.extract(mutated, LambdaHandler.lambda);

        assertInsn(lifted.first(), numberLambda());
        assertInsn(mutated, methodWith(right(InsnList.class, lifted.second())));
    }
}