package com.googlecode.jcompilo.lambda;

import com.googlecode.jcompilo.Resource;
import com.googlecode.jcompilo.Resources;
import com.googlecode.jcompilo.asm.Asm;
import com.googlecode.totallylazy.Option;
import org.junit.Test;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import static com.googlecode.jcompilo.lambda.LambdaFixture.numberIntValue;
import static com.googlecode.jcompilo.lambda.LambdaFixture.verify;
import static com.googlecode.totallylazy.matchers.Matchers.is;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class ClassGeneratorTest {
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

        verifyMethod(actual, expected, 0);
        verifyMethod(actual, expected, 1);
//        verifyMethod(actual, expected, 2);

        verify(actual);
    }

    private void verifyMethod(ClassNode actual, ClassNode expected, int index) {
        verifyMethod((MethodNode) actual.methods.get(index), (MethodNode) expected.methods.get(index));
    }

    private void verifyMethod(MethodNode actual, MethodNode expected) {
        assertThat(actual.access, is(expected.access));
        assertThat(actual.name, is(expected.name));
        assertThat(actual.desc, is(expected.desc));
        assertThat(actual.exceptions, is(expected.exceptions));
        assertThat(Asm.toString(expected.instructions), containsString(Asm.toString(actual.instructions)));
    }
}
