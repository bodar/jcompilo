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

        MethodNode actualConstructor = (MethodNode) actual.methods.get(0);
        MethodNode expectedConstructor = (MethodNode) expected.methods.get(0);
        assertThat(actualConstructor.access, is(expectedConstructor.access));
        assertThat(actualConstructor.name, is(expectedConstructor.name));
        assertThat(actualConstructor.desc, is(expectedConstructor.desc));
        assertThat(Asm.toString(expectedConstructor.instructions), containsString(Asm.toString(actualConstructor.instructions)));

        verify(actual);
    }
}
