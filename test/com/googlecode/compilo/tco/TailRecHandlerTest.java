package com.googlecode.compilo.tco;

import com.googlecode.compilo.ByteClassLoader;
import com.googlecode.compilo.Resource;
import com.googlecode.totallylazy.Files;
import com.googlecode.totallylazy.Maps;
import com.googlecode.totallylazy.Pair;
import org.junit.Test;

import java.io.File;

import static com.googlecode.compilo.MoveToTL.classFilename;
import static com.googlecode.compilo.MoveToTL.classNameForByteCode;
import static com.googlecode.compilo.Resource.constructors.resource;
import static com.googlecode.compilo.tco.AsmResourceHandler.asmResourceHandler;
import static com.googlecode.compilo.tco.TailRecHandler.defaultAnnotation;
import static com.googlecode.compilo.tco.TailRecHandler.tailRecHandler;
import static com.googlecode.totallylazy.Bytes.bytes;
import static com.googlecode.totallylazy.Files.file;

public class TailRecHandlerTest {
    @Test
    public void canProcessAResource() throws Exception {
        Resource resource = asmResourceHandler(true).add(defaultAnnotation, tailRecHandler()).
                handle(resourceFor(Gcd.class));
        Files.write(resource.bytes(), file(new File("out/hacked/"), resource.name()));
    }

    private Class<?> classFor(Resource resource) {
        try {
            ClassLoader classLoader = new ByteClassLoader(Maps.map(Pair.pair(resource.name(), resource.bytes())));
            return classLoader.loadClass(classNameForByteCode(resource.name()));
        } catch (ClassNotFoundException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    private Resource resourceFor(Class<?> aClass) {
        return resource(classFilename(aClass.getName()), bytes(aClass.getResourceAsStream(aClass.getSimpleName() + ".class")));
    }
}
