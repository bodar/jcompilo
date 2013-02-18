package com.googlecode.jcompilo.lambda;

import com.googlecode.jcompilo.Resource;
import com.googlecode.jcompilo.Resources;
import com.googlecode.jcompilo.asm.Asm;
import com.googlecode.jcompilo.asm.SingleExpression;
import com.googlecode.totallylazy.Either;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Sequence;
import org.junit.Test;
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
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.util.CheckClassAdapter;

import java.io.PrintWriter;

import static com.googlecode.jcompilo.lambda.FunctionalInterface.functionalInterface;
import static com.googlecode.totallylazy.Either.left;
import static com.googlecode.totallylazy.Either.right;
import static com.googlecode.totallylazy.Sequences.one;
import static com.googlecode.totallylazy.matchers.Matchers.is;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.objectweb.asm.Type.getType;

public class LambdaTest {
    @Test
    public void canLift() throws Exception {
        InsnList lambdaCall = lambdaCall();

        InsnList mutated = methodWith(left(lambdaCall, AbstractInsnNode.class));
        LabelNode placeHolder = new LabelNode();

        InsnList lifted = SingleExpression.extract(mutated, LambdaHandler.lambda, placeHolder);

        assertInsn(lifted, lambdaCall());
        assertInsn(mutated, methodWith(right(InsnList.class, placeHolder)));
        System.out.println(Asm.toString(mutated));
    }

    @Test
    public void canRewriteLambdaArgumentsCorrectly() throws Exception {
        InsnList body = lambdaCall();
        assertThat(LambdaHandler.rewriteArguments(body), is(numberIntValue()));
    }

    @Test
    public void canGenerateANewClass() throws Exception {
        ClassNode expected = Asm.classNode(Resource.constructors.resource(Number_intValue.class).bytes());

        Resources resources = new Resources() {
            @Override
            public Option<Resource> get(final String name) {
                try {
                    return Option.some(Resource.constructors.resource(Class.forName(name)));
                } catch (ClassNotFoundException e) {
                    return Option.none();
                }
            }
        };

        ClassNode actual = ClassGenerator.classGenerator(resources).generateClass(numberIntValue());
        assertThat(actual.version, is(expected.version));
        assertThat(actual.access, is(expected.access));
        assertThat(actual.name, notNullValue(String.class));
        assertThat(actual.signature, is(expected.signature));
        assertThat(actual.superName, is(expected.superName));

        MethodNode actualConstructor = (MethodNode) actual.methods.get(0);
        MethodNode expectedConstructor = (MethodNode) expected.methods.get(0);
        assertThat(actualConstructor.access, is(expectedConstructor.access));
        assertThat(actualConstructor.name, is(expectedConstructor.name));
        assertThat(actualConstructor.desc, is(expectedConstructor.desc));
        assertThat(Asm.toString(expectedConstructor.instructions), containsString(Asm.toString(actualConstructor.instructions)));

        verify(actual);
    }

    private FunctionalInterface numberIntValue() {
        return functionalInterface(
                getType("Lcom/googlecode/totallylazy/Function1;"),
                one(getType("Ljava/lang/Number;")),
                getType("Ljava/lang/Integer;"),
                functionBody());
    }

    private void verify(final ClassNode classNode) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(writer);
        byte[] bytes = writer.toByteArray();
        ClassReader reader = new ClassReader(bytes);
        CheckClassAdapter.verify(reader, false, new PrintWriter(System.out));
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
        assertEquals(Asm.toString(expected), Asm.toString(actual));
    }

    private void assertTypes(final Sequence<Type> actual, final Sequence<Type> expected) {
        assertEquals(debug(expected), debug(actual));
    }

    private String debug(final Sequence<Type> types) {
        return types.toString("\n");
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
