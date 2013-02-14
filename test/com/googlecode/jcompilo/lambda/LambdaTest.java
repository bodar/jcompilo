package com.googlecode.jcompilo.lambda;

import com.googlecode.totallylazy.Either;
import com.googlecode.totallylazy.Mapper;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Unchecked;
import com.googlecode.totallylazy.multi;
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

import java.util.Iterator;

import static com.googlecode.jcompilo.asm.Asm.isStatic;
import static com.googlecode.totallylazy.Either.left;
import static com.googlecode.totallylazy.Either.right;
import static com.googlecode.totallylazy.Sequences.memorise;
import static org.junit.Assert.assertEquals;

public class LambdaTest {
    @Test
    public void canLift() throws Exception {
        MethodInsnNode lambda = new MethodInsnNode(Opcodes.INVOKESTATIC, "com/googlecode/totallylazy/lambda/Lambdas", "λ", "(Ljava/lang/Object;Ljava/lang/Object;)Lcom/googlecode/totallylazy/Function1;");
        InsnList lambdaCall = lambdaCall(lambda);

        InsnList mutated = methodWith(left(lambdaCall, AbstractInsnNode.class));
        LabelNode placeHolder = new LabelNode();

        InsnList lifted = lift(mutated, lambda, placeHolder);

        assertInsn(lifted, lambdaCall(lambda));
        assertInsn(mutated, methodWith(right(InsnList.class, placeHolder)));
        System.out.println(debug(mutated));
    }

    private InsnList lambdaCall(final MethodInsnNode lambda) {
        InsnList lambdaCall = new InsnList();
        lambdaCall.add(new FieldInsnNode(Opcodes.GETSTATIC, "com/googlecode/totallylazy/lambda/Lambdas", "n", "Ljava/lang/Number;"));
        lambdaCall.add(new FieldInsnNode(Opcodes.GETSTATIC, "com/googlecode/totallylazy/lambda/Lambdas", "n", "Ljava/lang/Number;"));
        lambdaCall.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Number", "intValue", "()I"));
        lambdaCall.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;"));
        lambdaCall.add(lambda);
        return lambdaCall;
    }

    private void assertInsn(final InsnList actual, final InsnList expected) {
        assertEquals(debug(expected), debug(actual));
    }

    private String debug(final InsnList insnList) {
        Sequence<AbstractInsnNode> sequence = memorise(Unchecked.<Iterator<AbstractInsnNode>>cast(insnList.iterator()));
        return sequence.map(new Mapper<AbstractInsnNode, String>() {
            @Override
            public String call(final AbstractInsnNode node) throws Exception {
                return "Class: " + node.getClass().getSimpleName() + " OpCode:" + node.getOpcode() + " Type:" + node.getType();
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

    private InsnList lift(final InsnList insnList, final MethodInsnNode methodCall, final LabelNode placeHolder) {
        InsnList result = new InsnList();
        MethodInsnNode node = findLast(insnList, methodCall);
        insnList.insert(node, placeHolder);
        remove(insnList, result, node, 1);
        return result;

    }

    private void remove(InsnList input, final InsnList output, final AbstractInsnNode node, int count) {
        int needed = needed(node) + count - 1;
        AbstractInsnNode previous = node.getPrevious();
        input.remove(node);
        output.insert(node);
        if (needed == 0) return;
        remove(input, output, previous, needed);
    }

    private int needed(final MethodInsnNode node) {
        return (isStatic(node) ? 0 : 1) + Type.getType(node.desc).getArgumentTypes().length;
    }

    private int needed(final AbstractInsnNode node) {
        return new multi() {
        }.<Integer>methodOption(node).getOrElse(0);
    }

    private MethodInsnNode findLast(final InsnList insnList, final MethodInsnNode methodCall) {
        AbstractInsnNode node = insnList.getLast();
        while (!matches(methodCall, node)) node = node.getPrevious();
        return (MethodInsnNode) node;
    }

    private boolean matches(final MethodInsnNode methodCall, final AbstractInsnNode node) {
        return node.equals(methodCall);
    }

}
