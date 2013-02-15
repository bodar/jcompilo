package com.googlecode.jcompilo.lambda;

import com.googlecode.jcompilo.asm.SingleExpression;
import com.googlecode.totallylazy.Either;
import com.googlecode.totallylazy.Mapper;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Unchecked;
import com.googlecode.totallylazy.multi;
import com.googlecode.totallylazy.predicates.LogicalPredicate;
import org.junit.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.Iterator;

import static com.googlecode.jcompilo.asm.Asm.functions.name;
import static com.googlecode.jcompilo.asm.Asm.isStatic;
import static com.googlecode.totallylazy.Either.left;
import static com.googlecode.totallylazy.Either.right;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Sequences.memorise;
import static com.googlecode.totallylazy.Strings.startsWith;
import static org.junit.Assert.assertEquals;

public class LambdaTest {
    @Test
    public void canLift() throws Exception {
        InsnList lambdaCall = lambdaCall();

        InsnList mutated = methodWith(left(lambdaCall, AbstractInsnNode.class));
        LabelNode placeHolder = new LabelNode();

        InsnList lifted = SingleExpression.extract(mutated, LambdaTest.<AbstractInsnNode, MethodInsnNode>typeSafe(MethodInsnNode.class, where(name, startsWith("λ"))), placeHolder);

        assertInsn(lifted, lambdaCall());
        assertInsn(mutated, methodWith(right(InsnList.class, placeHolder)));
        System.out.println(debug(mutated));
    }

    @Test
    public void canRewriteLambdaArgumentsCorrectly() throws Exception {
        InsnList body = lambdaCall();
        LambdaHandler.rewriteArguments(body);
        assertInsn(body, functionBody());
    }


    public static <T, S extends T> LogicalPredicate<T> typeSafe(final Class<S> subClass, final Predicate<? super S> predicate) {
        return new LogicalPredicate<T>() {
            @Override
            public boolean matches(final T other) {
                return subClass.isInstance(other) && predicate.matches(subClass.cast(other));
            }
        };
    }

    private InsnList lambdaCall() {
        InsnList lambdaCall = new InsnList();
        lambdaCall.add(new FieldInsnNode(Opcodes.GETSTATIC, "com/googlecode/totallylazy/lambda/Lambdas", "n", "Ljava/lang/Number;"));
        lambdaCall.add(new FieldInsnNode(Opcodes.GETSTATIC, "com/googlecode/totallylazy/lambda/Lambdas", "n", "Ljava/lang/Number;"));
        lambdaCall.add(lambdaBody());
        lambdaCall.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/googlecode/totallylazy/lambda/Lambdas", "λ", "(Ljava/lang/Object;Ljava/lang/Object;)Lcom/googlecode/totallylazy/Function1;"));
        return lambdaCall;
    }

    private InsnList functionBody() {
        InsnList lambdaCall = new InsnList();
        lambdaCall.add(new VarInsnNode(Opcodes.ALOAD, 1));
        lambdaCall.add(lambdaBody());
        lambdaCall.add(new InsnNode(Opcodes.ARETURN));
        return lambdaCall;
    }

    private InsnList lambdaBody() {
        InsnList body = new InsnList();
        body.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Number", "intValue", "()I"));
        body.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;"));
        return body;
    }

    private void assertInsn(final InsnList actual, final InsnList expected) {
        assertEquals(debug(expected), debug(actual));
    }

    private String debug(final InsnList insnList) {
        Sequence<AbstractInsnNode> sequence = memorise(Unchecked.<Iterator<AbstractInsnNode>>cast(insnList.iterator()));
        return sequence.map(new Mapper<AbstractInsnNode, String>() {
            @Override
            public String call(final AbstractInsnNode node) throws Exception {
                return node.getClass().getSimpleName() + "(" + node.getOpcode() + ")";
            }
        }).toString("\n");
    }

    private InsnList methodWith(final Either<InsnList, ? extends AbstractInsnNode> lambdaCall) {
        InsnList instructions = new InsnList();
        LabelNode start = new LabelNode();
        instructions.add(start);
        instructions.add(new LineNumberNode(9, start));
        if (lambdaCall.isLeft()) instructions.add(lambdaCall.left());
        else instructions.add(lambdaCall.right());
        instructions.add(new FieldInsnNode(Opcodes.PUTSTATIC, "com/googlecode/jcompilo/lambda/LambdaFieldInput", "intValue", "Lcom/googlecode/totallylazy/Function1;"));
        instructions.add(new InsnNode(Opcodes.RETURN));
        return instructions;
    }


}
