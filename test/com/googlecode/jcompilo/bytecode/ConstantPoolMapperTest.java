package com.googlecode.jcompilo.bytecode;

import com.googlecode.totallylazy.Functions;
import com.googlecode.totallylazy.UnaryFunction;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import static com.googlecode.totallylazy.Bytes.bytes;
import static com.googlecode.totallylazy.Functions.identity;
import static org.junit.Assert.assertArrayEquals;

public class ConstantPoolMapperTest {
    @Test
    public void supportsRenamingAClass() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        new ConstantPoolMapper(new UnaryFunction<String>() {
            @Override
            public String call(String value) throws Exception {
                return value.replace("Dan", "Bob");
            }
        }).process(getClass().getResourceAsStream("Dan.class"), output);

        byte[] originalBob = bytes(getClass().getResourceAsStream("Bob.class"));
        byte[] newBob = output.toByteArray();

        assertArrayEquals(originalBob, newBob);
    }

    @Test
    public void correctlyHandlesLongsAndDoubles() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] originalClass = bytes(getClass().getResourceAsStream("ClassWithLong.class"));
        new ConstantPoolMapper(identity(String.class)).process(new ByteArrayInputStream(originalClass), output);

        byte[] preservesLongs = output.toByteArray();

        assertArrayEquals(originalClass, preservesLongs);
    }

}

// must be on the same line as this appears in the bytecode
class Dan { } class Bob { }

class ClassWithLong { long x = 1; double y = 2; }

