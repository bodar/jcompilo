package com.googlecode.jcompilo.tco;

import com.googlecode.jcompilo.ByteClassLoader;
import com.googlecode.jcompilo.Resource;
import com.googlecode.totallylazy.Files;
import com.googlecode.totallylazy.Maps;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.annotations.tailrec;
import org.junit.Test;

import java.util.Date;

import static com.googlecode.jcompilo.MoveToTL.classFilename;
import static com.googlecode.jcompilo.MoveToTL.classNameForByteCode;
import static com.googlecode.jcompilo.Resource.constructors.resource;
import static com.googlecode.jcompilo.asm.AsmResourceHandler.asmResourceHandler;
import static com.googlecode.jcompilo.tco.TailRecHandler.tailRecHandler;
import static com.googlecode.totallylazy.Bytes.bytes;
import static com.googlecode.totallylazy.Files.file;

public class TailRecHandlerTest {
    @Test
    public void canProcessAResource() throws Exception {
        Resource resource = asmResourceHandler(true).add(tailrec.class, tailRecHandler()).
                handle(resourceFor(TailRecursive.class));
        Files.write(resource.bytes(), file(Files.temporaryDirectory(TailRecHandlerTest.class.getSimpleName()), resource.name()));
    }

    private Class<?> classFor(Resource resource) {
        try {
            ClassLoader classLoader = new ByteClassLoader(Maps.map(Pair.pair(resource.name(), resource.bytes())));
            return classLoader.loadClass(classNameForByteCode(resource.name()));
        } catch (ClassNotFoundException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    public static Resource resourceFor(Class<?> aClass) {
        return resource(classFilename(aClass.getName()), new Date(), bytes(aClass.getResourceAsStream(aClass.getSimpleName() + ".class")));
    }
}
