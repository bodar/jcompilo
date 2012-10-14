package com.googlecode.compilo.tco;

import com.googlecode.compilo.Resource;
import org.junit.Test;

import static com.googlecode.compilo.Resource.constructors.resource;
import static com.googlecode.totallylazy.Bytes.bytes;

public class TailRecHandlerTest {
    @Test
    public void canProcessAResource() throws Exception {
        Resource resource = new TailRecHandler().handle(resourceFor(TailRecursive.class));
    }

    private Resource resourceFor(Class<?> aClass) {
        String name = aClass.getSimpleName() + ".class";
        return resource(name, bytes(aClass.getResourceAsStream(name)));
    }
}
