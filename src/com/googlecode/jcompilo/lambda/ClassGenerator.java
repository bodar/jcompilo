package com.googlecode.jcompilo.lambda;

import com.googlecode.jcompilo.Resources;
import com.googlecode.jcompilo.asm.Asm;
import com.googlecode.totallylazy.Sequence;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.List;
import java.util.UUID;

import static com.googlecode.totallylazy.Lists.list;
import static com.googlecode.totallylazy.Sequences.repeat;
import static com.tonicsystems.jarjar.asm.Opcodes.ALOAD;
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
        classNode.name = functionalInterface.type().getInternalName();
        classNode.signature = signature(functionalInterface);
        classNode.superName = functionalInterface.classType.getInternalName();
        classNode.methods = list(Asm.constructor(functionalInterface.classType),
                method(functionalInterface),
                bridgeMethod(functionalInterface));

        return classNode;
    }


    private MethodNode method(FunctionalInterface functionalInterface) {
        MethodNode methodNode = new MethodNode(ACC_PUBLIC, methodName(functionalInterface), methodSignature(functionalInterface), null, exceptions(functionalInterface));
        methodNode.instructions = functionalInterface.body;

        return methodNode;
    }

    private String methodSignature(FunctionalInterface functionalInterface) {
        return "(" + functionalInterface.argumentTypes.toString("") + ")" + functionalInterface.returnType;
    }

    private MethodNode bridgeMethod(final FunctionalInterface functionalInterface) {
        String object = "Ljava/lang/Object;";
        MethodNode methodNode = new MethodNode(ACC_PUBLIC + ACC_BRIDGE + ACC_SYNTHETIC, methodName(functionalInterface), "(" + repeat(object).take(functionalInterface.argumentTypes.size()).toString("") + ")" + object, null, exceptions(functionalInterface));
        InsnList insnList = new InsnList();
        insnList.add(new VarInsnNode(Opcodes.ALOAD, 0));
        List<Type> argumentTypes = functionalInterface.argumentTypes.toList();
        for (int i = 0; i < argumentTypes.size(); i++) {
            Type argumentType = argumentTypes.get(i);
            insnList.add(new VarInsnNode(Opcodes.ALOAD, i + 1));
            insnList.add(new TypeInsnNode(Opcodes.CHECKCAST, argumentType.getInternalName()));
        }
        insnList.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, functionalInterface.type().getInternalName(), methodName(functionalInterface), methodSignature(functionalInterface)));
        insnList.add(new InsnNode(Opcodes.ARETURN));
        methodNode.instructions = insnList;

        return methodNode;
    }

    private String[] exceptions(FunctionalInterface functionalInterface) {
        return new String[]{"java/lang/Exception"}; //TODO
    }

    private String methodName(FunctionalInterface functionalInterface) {
        return "call"; //TODO
    }

    private String signature(final FunctionalInterface functionalInterface) {
        return String.format("L%s<%s%s>;",
                functionalInterface.classType.getInternalName(),
                functionalInterface.argumentTypes.toString(""),
                functionalInterface.returnType);
    }

}
