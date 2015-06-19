package com.googlecode.jcompilo.bytecode;

import com.googlecode.totallylazy.Bytes;
import com.googlecode.totallylazy.UnaryFunction;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import static com.googlecode.totallylazy.Functions.identity;
import static org.junit.Assert.assertArrayEquals;

public class ConstantPoolMapperTest {
    @Test
    public void supportsRenamingAClass() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        new ConstantPoolMapper(value -> value.replace("Dan", "Bob")).process(inputStream(Dan.class), output);

        byte[] originalBob = bytes(Bob.class);
        byte[] newBob = output.toByteArray();

        assertArrayEquals(originalBob, newBob);
    }

    @Test
    public void correctlyHandlesLongsAndDoubles() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] originalClass = bytes(ClassWithLongAndDouble.class);
        new ConstantPoolMapper(identity(String.class)).
                process(new ByteArrayInputStream(originalClass), output);

        byte[] preservesLongs = output.toByteArray();

        assertArrayEquals(originalClass, preservesLongs);
    }

    private static byte[] bytes(Class<?> aClass) {
        return Bytes.bytes(inputStream(aClass));
    }

    private static InputStream inputStream(Class<?> aClass) {
        return aClass.getResourceAsStream(aClass.getSimpleName() + ".class");
    }

}

// must be on the same line as this appears in the bytecode
class Dan { } class Bob { }

class ClassWithLongAndDouble { long x = 1; double y = 2; }

