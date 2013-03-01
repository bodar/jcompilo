package com.googlecode.jcompilo.lambda;

import com.googlecode.jcompilo.asm.Asm;
import org.junit.Test;
import org.objectweb.asm.tree.ClassNode;

import static com.googlecode.jcompilo.lambda.ClassGenerator.classGenerator;
import static com.googlecode.jcompilo.lambda.LambdaFixture.localVariableClosure;
import static com.googlecode.jcompilo.lambda.LambdaFixture.numberIntValue;
import static com.googlecode.jcompilo.asm.Asm.verify;
import static com.googlecode.totallylazy.matchers.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class ClassGeneratorTest {
    @Test
    public void canGenerateASimpleLambda() throws Exception {
        ClassNode expected = Asm.classNode(Number_intValue.class);

        ClassNode actual = classGenerator(new ClassResources()).generateClass(numberIntValue());
        assertThat(actual.version, is(expected.version));
        assertThat(actual.access, is(expected.access));
        assertThat(actual.name, notNullValue(String.class));
        assertThat(actual.signature, is(expected.signature));
        assertThat(actual.superName, is(expected.superName));

        LambdaFixture.verifyMethod(actual, expected, 0);
        LambdaFixture.verifyMethod(actual, expected, 1);
//        verifyMethod(actual, expected, 2);

        Asm.verify(actual);
    }

    @Test
    public void canGenerateALocalVariableClosure() throws Exception {
        ClassNode expected = Asm.classNode(String_charAt.class);

        ClassNode actual = classGenerator(new ClassResources()).generateClass(localVariableClosure());
        assertThat(actual.version, is(expected.version));
        assertThat(actual.access, is(expected.access));
        assertThat(actual.name, notNullValue(String.class));
        assertThat(actual.signature, is(expected.signature));
        assertThat(actual.superName, is(expected.superName));

        LambdaFixture.verifyMethod(actual, expected, 0);
        LambdaFixture.verifyMethod(actual, expected, 1);
//        verifyMethod(actual, expected, 2);

        Asm.verify(actual);
    }
}
