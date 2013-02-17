package com.googlecode.jcompilo.lambda;

import com.googlecode.jcompilo.Resources;
import com.googlecode.jcompilo.asm.Asm;
import org.objectweb.asm.tree.ClassNode;

import java.util.UUID;

import static com.googlecode.totallylazy.Lists.list;
import static org.objectweb.asm.Opcodes.*;

public class ClassGenerator {
    private final Resources resources;
    private final int version;

    private ClassGenerator(final Resources resources, final int version) {
        this.resources = resources;
        this.version = version;
    }

    public static ClassGenerator classGenerator(final Resources resources) {
        return classGenerator(resources, V1_6);
    }

    public static ClassGenerator classGenerator(final Resources resources, final int version) {
        return new ClassGenerator(resources, version);
    }

    public ClassNode generateClass(final FunctionalInterface functionalInterface) {
        ClassNode classNode = new ClassNode();
        classNode.version = version;
        classNode.access = ACC_PUBLIC + ACC_SUPER;
        classNode.name = functionalInterface.classType.getInternalName() + UUID.randomUUID().toString().replace("-", "");
        classNode.signature = signature(functionalInterface);
        classNode.superName = functionalInterface.classType.getInternalName();
        classNode.methods = list(Asm.constructor(functionalInterface.classType));

        return classNode;
    }

    private String signature(final FunctionalInterface functionalInterface) {
        return String.format("L%s<%s%s>;",
                functionalInterface.classType.getInternalName(),
                functionalInterface.argumentTypes.toString(""),
                functionalInterface.returnType);
    }

}
