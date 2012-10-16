package com.googlecode.compilo.tco;

import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.predicates.LogicalPredicate;
import com.tonicsystems.jarjar.asm.Type;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.annotation.Annotation;
import java.util.List;

import static com.googlecode.compilo.tco.Asm.predicates.annotation;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;
import static org.objectweb.asm.Type.getDescriptor;

public final class Asm {
    public static boolean hasAnnotation(MethodNode method, final Class<? extends Annotation> aClass) {
        return annotations(method).exists(annotation(aClass));
    }

    public static Sequence<AnnotationNode> annotations(MethodNode method) {
        return Asm.<AnnotationNode>seq(method.visibleAnnotations).join(Asm.<AnnotationNode>seq(method.invisibleAnnotations));
    }

    @SuppressWarnings("unchecked")
    public static <T> Sequence<T> seq(List list) {
        return list != null ? Sequences.<T>sequence(list) : Sequences.<T>empty();
    }

    @SuppressWarnings("unchecked")
    public static Sequence<AbstractInsnNode> instructions(MethodNode method) {
        return Sequences.<AbstractInsnNode>forwardOnly(method.instructions.iterator());
    }

    public static Sequence<LocalVariableNode> localVariables(MethodNode methodNode) {
        return Asm.<LocalVariableNode>seq(methodNode.localVariables);
    }

    public static int store(LocalVariableNode localVariableNode) {
        return Type.getType(localVariableNode.desc).getOpcode(Opcodes.ISTORE);
    }

    public static class predicates {
        public static LogicalPredicate<AnnotationNode> annotation(Class<? extends Annotation> aClass) {
            return where(functions.desc, is(getDescriptor(aClass)));
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
    }

}
