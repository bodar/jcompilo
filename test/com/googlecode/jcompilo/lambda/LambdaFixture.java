package com.googlecode.jcompilo.lambda;

import com.googlecode.jcompilo.asm.Asm;
import com.googlecode.totallylazy.Block;
import com.googlecode.totallylazy.Either;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.Triple;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
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
                functionBody(Number_intValue()),
                Sequences.<Pair<InsnList, Type>>empty());
    }

    public static FunctionalInterface localVariableClosure() {
        InsnList insnList = new InsnList();
        insnList.add(new VarInsnNode(Opcodes.ILOAD, 1));

        return stringCharAt(insnList);
    }

    public static FunctionalInterface fieldClosure() {
        InsnList insnList = new InsnList();
        insnList.add(new VarInsnNode(Opcodes.ALOAD, 0));
        insnList.add(new FieldInsnNode(Opcodes.GETFIELD, "com/example/UsesLambda", "myIndex", "I"));
        return stringCharAt(insnList);
    }

    private static FunctionalInterface stringCharAt(final InsnList insnList) {
        return functionalInterface(
                getType("Lcom/googlecode/totallylazy/Function1;"),
                one(getType("Ljava/lang/String;")),
                getType("Ljava/lang/Character;"),
                functionBody(stringFunctionBody()),
                Sequences.one(Pair.pair(insnList, getType("I"))));
    }

    public static void verify(final ClassNode classNode) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(writer);
        byte[] bytes = writer.toByteArray();
        ClassReader reader = new ClassReader(bytes);
        CheckClassAdapter.verify(reader, false, new PrintWriter(System.out));
    }

    public static Block<ClassNode> verify = new Block<ClassNode>() {
        @Override
        protected void execute(ClassNode classNode) throws Exception {
            LambdaFixture.verify(classNode);
        }
    };

    public static InsnList numberLambda() {
        return function1(new FieldInsnNode(Opcodes.GETSTATIC, "com/googlecode/totallylazy/lambda/Lambdas", "n", "Ljava/lang/Number;"),
                Number_intValue());
    }

    public static InsnList localArgumentLambda() {
        InsnList body = String_charAt();
        body.insert(new VarInsnNode(Opcodes.ILOAD, 1));
        return function1(new FieldInsnNode(Opcodes.GETSTATIC, "com/googlecode/totallylazy/Strings", "s", "Ljava/lang/String;"),
                body);
    }

    private static InsnList function1(final FieldInsnNode lambdaArgument, final InsnList body) {
        return lambdaCall(
                lambdaArgument,
                body,
                new MethodInsnNode(Opcodes.INVOKESTATIC, "com/googlecode/totallylazy/lambda/Lambdas", "λ", "(Ljava/lang/Object;Ljava/lang/Object;)Lcom/googlecode/totallylazy/Function1;"));
    }

    private static InsnList lambdaCall(final FieldInsnNode lambdaArgument, final InsnList body, final MethodInsnNode lambdaInvoke) {

        InsnList lambdaCall = new InsnList();
        lambdaCall.add(lambdaArgument);
        lambdaCall.add(lambdaArgument.clone(null));
        lambdaCall.add(body);
        lambdaCall.add(lambdaInvoke);
        return lambdaCall;
    }

    public static InsnList functionBody(final InsnList bpdy) {
        InsnList lambdaCall = new InsnList();
        lambdaCall.add(new VarInsnNode(Opcodes.ALOAD, 1));
        lambdaCall.add(bpdy);
        lambdaCall.add(new InsnNode(Opcodes.ARETURN));
        return lambdaCall;
    }

    public static InsnList Number_intValue() {
        InsnList body = new InsnList();
        body.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Number", "intValue", "()I"));
        body.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;"));
        return body;
    }

    public static InsnList stringFunctionBody() {
        InsnList body = new InsnList();
        body.add(new VarInsnNode(com.tonicsystems.jarjar.asm.Opcodes.ALOAD, 0));
        body.add(new FieldInsnNode(com.tonicsystems.jarjar.asm.Opcodes.GETFIELD, "this", "argument0", "I"));
        body.add(String_charAt());
        return body;
    }

    public static InsnList String_charAt() {
        InsnList body = new InsnList();
        body.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "charAt", "(I)C"));
        body.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Character", "valueOf", "(I)Ljava/lang/Character;"));
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
