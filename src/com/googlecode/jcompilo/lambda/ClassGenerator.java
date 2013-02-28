package com.googlecode.jcompilo.lambda;

import com.googlecode.jcompilo.Resources;
import com.googlecode.jcompilo.asm.Asm;
import com.googlecode.jcompilo.asm.AsmReflector;
import com.googlecode.totallylazy.Mapper;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.List;

import static com.googlecode.jcompilo.asm.Asm.functions.access;
import static com.googlecode.jcompilo.asm.Asm.instructions;
import static com.googlecode.totallylazy.Lists.list;
import static com.googlecode.totallylazy.Predicates.notNullValue;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Sequences.repeat;
import static org.objectweb.asm.Opcodes.*;

public class ClassGenerator {
    private final AsmReflector reflector;
    private final int version;

    private ClassGenerator(final Resources resources, final int version) {
        this.reflector = new AsmReflector(resources);
        this.version = version;
    }

    public static ClassGenerator classGenerator(final Resources resources) {
        return classGenerator(resources, V1_6);
    }

    public static ClassGenerator classGenerator(final Resources resources, final int version) {
        return new ClassGenerator(resources, version);
    }

    public ClassNode generateClass(final FunctionalInterface functionalInterface) {
        MethodNode methodToOverride = findMethodToOverride(functionalInterface);
        String name = functionalInterface.type().getInternalName();
        ClassNode classNode = new ClassNode();
        classNode.version = version;
        classNode.access = ACC_PUBLIC + ACC_SUPER;
        classNode.name = name;
        classNode.signature = signature(functionalInterface);
        classNode.superName = functionalInterface.classType.getInternalName();
        Sequence<Pair<String,Type>> types = functionalInterface.fields();
        classNode.fields = fields(types);
        classNode.methods = list(Asm.constructor(functionalInterface.classType, name, types),
                method(functionalInterface, methodToOverride),
                bridgeMethod(functionalInterface, methodToOverride));

        return classNode;
    }

    private List<FieldNode> fields(final Sequence<Pair<String, Type>> types) {
        return types.map(new Mapper<Pair<String, Type>, FieldNode>() {
            @Override
            public FieldNode call(final Pair<String, Type> pair) throws Exception {
                return new FieldNode(Opcodes.ACC_PRIVATE, pair.first(), pair.second().getDescriptor(), null, null);
            }
        }).toList();
    }

    private MethodNode method(FunctionalInterface functionalInterface, final MethodNode methodToOverride) {
        MethodNode methodNode = new MethodNode(ACC_PUBLIC,
                methodToOverride.name,
                methodSignature(functionalInterface), null,
                exceptions(methodToOverride));

        for (FieldInsnNode fieldInsnNode : instructions(functionalInterface.body).safeCast(FieldInsnNode.class)) {
            if(fieldInsnNode.owner.equals("this")){
                fieldInsnNode.owner = functionalInterface.type().getInternalName();
            }
        }

        methodNode.instructions = functionalInterface.body;

        return methodNode;
    }

    private String methodSignature(FunctionalInterface functionalInterface) {
        return "(" + functionalInterface.argumentTypes.toString("") + ")" + functionalInterface.returnType;
    }

    private MethodNode bridgeMethod(final FunctionalInterface functionalInterface, final MethodNode methodToOverride) {
        String object = "Ljava/lang/Object;";
        MethodNode methodNode = new MethodNode(ACC_PUBLIC + ACC_BRIDGE + ACC_SYNTHETIC,
                methodToOverride.name,
                "(" + repeat(object).take(functionalInterface.argumentTypes.size()).toString("") + ")" + object, null,
                exceptions(methodToOverride));

        InsnList insnList = new InsnList();
        insnList.add(new VarInsnNode(Opcodes.ALOAD, 0));
        List<Type> argumentTypes = functionalInterface.argumentTypes.toList();
        for (int i = 0; i < argumentTypes.size(); i++) {
            Type argumentType = argumentTypes.get(i);
            insnList.add(new VarInsnNode(Opcodes.ALOAD, i + 1));
            insnList.add(new TypeInsnNode(Opcodes.CHECKCAST, argumentType.getInternalName()));
        }
        insnList.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, functionalInterface.type().getInternalName(),
                methodToOverride.name, methodSignature(functionalInterface)));
        insnList.add(new InsnNode(Opcodes.ARETURN));
        methodNode.instructions = insnList;

        return methodNode;
    }

    private String[] exceptions(MethodNode methodToOverride) {
        return Asm.<String>seq(methodToOverride.exceptions).toArray(String.class);
    }

    private MethodNode findMethodToOverride(final FunctionalInterface functionalInterface) {
        String className = functionalInterface.classType.getClassName();
        Sequence<MethodNode> methods = reflector.allMethods(className);
        return methods.find(where(access, AsmReflector.contains(Opcodes.ACC_ABSTRACT))).get();
    }


    private String signature(final FunctionalInterface functionalInterface) {
        return String.format("L%s<%s%s>;",
                functionalInterface.classType.getInternalName(),
                functionalInterface.argumentTypes.toString(""),
                functionalInterface.returnType);
    }
}