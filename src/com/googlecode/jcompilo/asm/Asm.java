package com.googlecode.jcompilo.asm;

import com.googlecode.jcompilo.Resource;
import com.googlecode.totallylazy.Block;
import com.googlecode.totallylazy.Callables;
import com.googlecode.totallylazy.Fields;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.annotations.multimethod;
import com.googlecode.totallylazy.multi;
import com.googlecode.totallylazy.predicates.LogicalPredicate;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.util.CheckClassAdapter;

import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;

import static com.googlecode.jcompilo.asm.Asm.predicates.annotation;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Unchecked.cast;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Type.getDescriptor;

public final class Asm {
    public static final String CONSTRUCTOR = "<init>";
    public static final String STATIC_CONSTRUCTOR = "<clinit>";
    public static final String CONSTRUCTOR_NO_ARGUMENTS = "()V";

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
        return instructions(method.instructions);
    }

    @SuppressWarnings("unchecked")
    public static Sequence<AbstractInsnNode> instructions(final InsnList instructions) {
        return new Sequence<AbstractInsnNode>() {
            @Override
            public Iterator<AbstractInsnNode> iterator() {
                return new InsnIterator(instructions);
            }
        };
    }

    public static Sequence<LocalVariableNode> localVariables(MethodNode methodNode) {
        return Asm.<LocalVariableNode>seq(methodNode.localVariables);
    }

    public static int store(LocalVariableNode localVariableNode) {
        return store(Type.getType(localVariableNode.desc));
    }

    public static int store(Type type) {
        return type.getOpcode(Opcodes.ISTORE);
    }

    public static int load(Type type) {
        return type.getOpcode(Opcodes.ILOAD);
    }

    public static int returns(Type type) {
        return type.getOpcode(Opcodes.IRETURN);
    }

    public static Sequence<Type> initialLocalVariables(ClassNode classNode, MethodNode methodNode) {
        Sequence<Type> sequence = argumentTypes(methodNode);
        return isStatic(methodNode) ? sequence : sequence.cons(Type.getType("L" + classNode.name + ";"));
    }

    public static boolean isAbstract(MethodNode methodNode) {
        return (methodNode.access & Opcodes.ACC_ABSTRACT) != 0;
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

    public static Function1<AbstractInsnNode, String> toString = Asm::toString;

    public static String toString(AbstractInsnNode node) {
        if (node == null) return "";
        return new multi() {
        }.<String>methodOption(node).
                getOrElse(Asm.toString(node.getOpcode()) + "(" + node.getClass().getSimpleName() + ")");
    }

    @multimethod
    public static String toString(VarInsnNode node) {
        return Asm.toString(node.getOpcode()) + " " + node.var;
    }

    @multimethod
    public static String toString(MethodInsnNode node) {
        return Asm.toString(node.getOpcode()) + " " + node.owner + "." + node.name + " " + node.desc;
    }

    @multimethod
    public static String toString(FieldInsnNode node) {
        return Asm.toString(node.getOpcode()) + " " + node.owner + "." + node.name + " " + node.desc;
    }

    @multimethod
    public static String toString(TypeInsnNode node) {
        return Asm.toString(node.getOpcode()) + " " + node.desc;
    }

    @multimethod
    public static String toString(LabelNode node) {
        return "LABEL";
    }

    @multimethod
    public static String toString(LineNumberNode node) {
        return "LINENUMBER " + node.line + " " + toString(node.start);
    }

    @multimethod
    public static String toString(InsnNode node) {
        return Asm.toString(node.getOpcode());
    }

    public static String toString(final int opcode) {
        return sequence(Opcodes.class.getFields()).
                find(where(Asm.<Integer>value(null), is(opcode))).
                map(Fields.name).
                getOrElse(String.valueOf(opcode));
    }

    public static ClassNode classNode(final Class<?> aClass) {
        return classNode(Resource.constructors.resource(aClass).bytes());
    }

    public static ClassNode classNode(final byte[] bytes) {
        ClassReader reader = new ClassReader(bytes);
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, 0);
        return classNode;
    }

    public static MethodNode constructor(final Type superType, final String name, final Sequence<Pair<String, Type>> types) {
        MethodNode constructor = new MethodNode(ACC_PUBLIC, CONSTRUCTOR, argumentSignature(types), null, new String[0]);
        InsnList insnList = new InsnList();
        insnList.add(new VarInsnNode(ALOAD, 0));
        insnList.add(new MethodInsnNode(INVOKESPECIAL, superType.getInternalName(), CONSTRUCTOR, CONSTRUCTOR_NO_ARGUMENTS, false));

        for (int i = 0; i < types.size(); i++) {
            Pair<String, Type> pair = types.get(i);
            insnList.add(new VarInsnNode(ALOAD, 0));
            insnList.add(new VarInsnNode(Asm.load(pair.second()), i + 1));
            insnList.add(new FieldInsnNode(Opcodes.PUTFIELD, name, pair.first(), pair.second().getDescriptor()));
        }

        insnList.add(new InsnNode(RETURN));
        constructor.instructions = insnList;

        return constructor;
    }

    private static String argumentSignature(final Sequence<? extends Pair<?, Type>> types) {
        return types.map(Callables.second(Type.class)).toString("(", "", ")V");
    }

    public static InsnList construct(Type type, Sequence<Pair<InsnList, Type>> arguments) {
        InsnList construct = new InsnList();
        construct.add(new TypeInsnNode(NEW, type.getInternalName()));
        construct.add(new InsnNode(DUP));
        for (InsnList argument : arguments.map(Callables.first(InsnList.class))) {
            construct.add(argument);
        }
        construct.add(new MethodInsnNode(INVOKESPECIAL, type.getInternalName(), CONSTRUCTOR, argumentSignature(arguments), false));
        return construct;
    }

    public static Sequence<MethodNode> methods(final ClassNode classNode) {
        return Asm.<MethodNode>seq(classNode.methods);
    }

    public static void verify(final ClassNode classNode) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(writer);
        byte[] bytes = writer.toByteArray();
        verify(bytes);
    }

    public static void verify(final byte[] bytes) {
        ClassReader reader = new ClassReader(bytes);
        CheckClassAdapter.verify(reader, false, new PrintWriter(System.out));
    }

    public static Block<ClassNode> verify = Asm::verify;

    public static <T> Function1<Field, T> value(final Object instance) {
        return field -> cast(field.get(instance));
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
        public static final Function1<AbstractInsnNode, Integer> opcode = AbstractInsnNode::getOpcode;
        public static final Function1<AnnotationNode, String> desc = annotationNode -> annotationNode.desc;
        public static final Function1<AbstractInsnNode, AbstractInsnNode> nextInstruction = AbstractInsnNode::getNext;
        public static final Function1<MethodInsnNode, String> owner = methodInsnNode -> methodInsnNode.owner;
        public static final Function1<MethodInsnNode, String> name = methodInsnNode -> methodInsnNode.name;
        public static final Function1<LocalVariableNode, Type> localVariableType = localVariableNode -> Type.getType(localVariableNode.desc);
        public static final Function1<MethodNode, Integer> access = node -> node.access;
    }
}
