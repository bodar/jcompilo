package com.googlecode.jcompilo.asm;

import com.googlecode.jcompilo.lambda.FunctionalInterface;
import com.googlecode.totallylazy.Fields;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Mapper;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.multi;
import com.googlecode.totallylazy.predicates.LogicalPredicate;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.lang.annotation.Annotation;
import java.util.List;

import static com.googlecode.jcompilo.asm.Asm.predicates.annotation;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.tonicsystems.jarjar.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Type.getDescriptor;

public final class Asm {
    public static boolean hasAnnotation(MethodNode method, final Class<? extends Annotation> aClass) {
        return annotations(method).exists(annotation(aClass));
    }

    public static boolean hasAnnotation(MethodNode method, final Type aClass) {
        return annotations(method).exists(annotation(aClass));
    }

    public static Sequence<AnnotationNode> annotations(MethodNode method) {
        return Asm.<AnnotationNode>seq(method.visibleAnnotations).join(Asm.<AnnotationNode>seq(method.invisibleAnnotations));
    }

    @SuppressWarnings("unchecked")
    public static <T> Sequence<T> seq(List list) {
        return Sequences.<T>sequence(list);
    }

    @SuppressWarnings("unchecked")
    public static Sequence<AbstractInsnNode> instructions(MethodNode method) {
        return Sequences.<AbstractInsnNode>memorise(method.instructions.iterator());
    }

    @SuppressWarnings("unchecked")
    public static Sequence<AbstractInsnNode> instructions(InsnList instructions) {
        return Sequences.<AbstractInsnNode>memorise(instructions.iterator());
    }

    public static Sequence<LocalVariableNode> localVariables(MethodNode methodNode) {
        return Asm.<LocalVariableNode>seq(methodNode.localVariables);
    }

    public static int store(LocalVariableNode localVariableNode) {
        return store(Type.getType(localVariableNode.desc));
    }

    public static int store(Type type) {return type.getOpcode(Opcodes.ISTORE);}
    public static int load(Type type) {return type.getOpcode(Opcodes.ILOAD);}
    public static int returns(Type type) {return type.getOpcode(Opcodes.IRETURN);}

    public static Sequence<Type> initialLocalVariables(ClassNode classNode, MethodNode methodNode) {
        Sequence<Type> sequence = argumentTypes(methodNode);
        return isStatic(methodNode) ? sequence : sequence.cons(Type.getType(classNode.name)) ;
    }

    public static boolean isStatic(MethodNode methodNode) {
        return (methodNode.access & Opcodes.ACC_STATIC) != 0;
    }

    public static boolean isStatic(MethodInsnNode methodNode) {
        return methodNode.getOpcode() == Opcodes.INVOKESTATIC;
    }

    public static Sequence<Type> argumentTypes(MethodNode methodNode) {
        Type type = Type.getType(methodNode.desc);
        return sequence(type.getArgumentTypes());
    }

    public static Sequence<Type> argumentTypes(MethodInsnNode methodNode) {
        Type type = Type.getType(methodNode.desc);
        return sequence(type.getArgumentTypes());
    }

    public static int numberOfArguments(MethodInsnNode methodNode) {
        Type type = Type.getType(methodNode.desc);
        return type.getArgumentTypes().length;
    }

    public static String toString(final InsnList insnList) {
        return instructions(insnList).map(toString).toString("\n");
    }

    public static Mapper<AbstractInsnNode, String> toString = new Mapper<AbstractInsnNode, String>() {
        @Override
        public String call(final AbstractInsnNode node) throws Exception {
            return Asm.toString(node);
        }
    };

    public static String toString(AbstractInsnNode node){
        return new multi(){}.<String>methodOption(node).getOrElse(Asm.toString(node.getOpcode()) + "(" + node.getClass().getSimpleName() + ")");
    }

    public static String toString(VarInsnNode node){
        return Asm.toString(node.getOpcode()) + " " + node.var;
    }

    public static String toString(MethodInsnNode node){
        return Asm.toString(node.getOpcode()) + " " + node.owner + "." + node.name + " " + node.desc;
    }

    public static String toString(InsnNode node){
        return Asm.toString(node.getOpcode());
    }

    public static String toString(final int opcode) {
        return sequence(Opcodes.class.getFields()).
                find(where(FunctionalInterface.<Integer>value(null), is(opcode))).
                map(Fields.name).
                getOrElse(String.valueOf(opcode));
    }

    public static ClassNode classNode(final byte[] bytes) {
        ClassReader reader = new ClassReader(bytes);
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, 0);
        return classNode;
    }

    public static MethodNode constructor(final Type superType) {
        MethodNode constructor = new MethodNode();
        constructor.access = ACC_PUBLIC;
        constructor.name = "<init>";
        constructor.desc = "()V";
        InsnList insnList = new InsnList();
        insnList.add(new VarInsnNode(ALOAD, 0));
        insnList.add(new MethodInsnNode(com.tonicsystems.jarjar.asm.Opcodes.INVOKESPECIAL, superType.getInternalName(), "<init>", "()V" ));
        insnList.add(new InsnNode(com.tonicsystems.jarjar.asm.Opcodes.RETURN));
        constructor.instructions = insnList;

        return constructor;
    }

    public static class predicates {
        public static LogicalPredicate<AnnotationNode> annotation(Class<? extends Annotation> aClass) {
            return where(functions.desc, is(getDescriptor(aClass)));
        }
        public static LogicalPredicate<AnnotationNode> annotation(Type aClass) {
            return where(functions.desc, is(aClass.getDescriptor()));
        }
    }

    public static class functions {
        public static Function1<AbstractInsnNode, Integer> opcode = new Function1<AbstractInsnNode, Integer>() {
            @Override
            public Integer call(AbstractInsnNode abstractInsnNode) throws Exception {
                return abstractInsnNode.getOpcode();
            }
        };

        public static Function1<AnnotationNode, String> desc = new Function1<AnnotationNode, String>() {
            @Override
            public String call(AnnotationNode annotationNode) throws Exception {
                return annotationNode.desc;
            }
        };

        public static Function1<AbstractInsnNode, AbstractInsnNode> nextInstruction = new Function1<AbstractInsnNode, AbstractInsnNode>() {
            @Override
            public AbstractInsnNode call(AbstractInsnNode insnNode) throws Exception {
                return insnNode.getNext();
            }
        };

        public static Function1<MethodInsnNode, String> owner = new Function1<MethodInsnNode, String>() {
            @Override
            public String call(MethodInsnNode methodInsnNode) throws Exception {
                return methodInsnNode.owner;
            }
        };
        public static Function1<MethodInsnNode, String> name = new Function1<MethodInsnNode, String>() {
            @Override
            public String call(MethodInsnNode methodInsnNode) throws Exception {
                return methodInsnNode.name;
            }
        };
        public static final Function1<LocalVariableNode, Type> localVariableType = new Function1<LocalVariableNode, Type>() {
            @Override
            public Type call(LocalVariableNode localVariableNode) throws Exception {
                return Type.getType(localVariableNode.desc);
            }
        };

    }

}
