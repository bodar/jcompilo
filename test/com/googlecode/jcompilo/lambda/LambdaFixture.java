package com.googlecode.jcompilo.lambda;

import com.googlecode.jcompilo.asm.Asm;
import com.googlecode.totallylazy.Either;
import com.googlecode.totallylazy.Sequence;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.CheckClassAdapter;

import java.io.PrintWriter;

import static com.googlecode.jcompilo.lambda.FunctionalInterface.functionalInterface;
import static com.googlecode.totallylazy.Sequences.one;
import static org.junit.Assert.assertEquals;
import static org.objectweb.asm.Type.getType;

public class LambdaFixture {
    public static FunctionalInterface numberIntValue() {
        return functionalInterface(
                getType("Lcom/googlecode/totallylazy/Function1;"),
                one(getType("Ljava/lang/Number;")),
                getType("Ljava/lang/Integer;"),
                functionBody());
    }

    public static void verify(final ClassNode classNode) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(writer);
        byte[] bytes = writer.toByteArray();
        ClassReader reader = new ClassReader(bytes);
        CheckClassAdapter.verify(reader, false, new PrintWriter(System.out));
    }

    public static InsnList lambdaCall() {
        InsnList lambdaCall = new InsnList();
        lambdaCall.add(new FieldInsnNode(Opcodes.GETSTATIC, "com/googlecode/totallylazy/lambda/Lambdas", "n", "Ljava/lang/Number;"));
        lambdaCall.add(new FieldInsnNode(Opcodes.GETSTATIC, "com/googlecode/totallylazy/lambda/Lambdas", "n", "Ljava/lang/Number;"));
        lambdaCall.add(lambdaBody());
        lambdaCall.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/googlecode/totallylazy/lambda/Lambdas", "λ", "(Ljava/lang/Object;Ljava/lang/Object;)Lcom/googlecode/totallylazy/Function1;"));
        return lambdaCall;
    }

    public static InsnList functionBody() {
        InsnList lambdaCall = new InsnList();
        lambdaCall.add(new VarInsnNode(Opcodes.ALOAD, 1));
        lambdaCall.add(lambdaBody());
        lambdaCall.add(new InsnNode(Opcodes.ARETURN));
        return lambdaCall;
    }

    public static InsnList lambdaBody() {
        InsnList body = new InsnList();
        body.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Number", "intValue", "()I"));
        body.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;"));
        return body;
    }

    public static void assertInsn(final InsnList actual, final InsnList expected) {
        assertEquals(Asm.toString(expected), Asm.toString(actual));
    }

    public static void assertTypes(final Sequence<Type> actual, final Sequence<Type> expected) {
        assertEquals(debug(expected), debug(actual));
    }

    public static String debug(final Sequence<Type> types) {
        return types.toString("\n");
    }

    public static InsnList methodWith(final Either<InsnList, ? extends AbstractInsnNode> lambdaCall) {
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
