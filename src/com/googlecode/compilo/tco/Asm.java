package com.googlecode.compilo.tco;

import com.googlecode.totallylazy.Function1;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodInsnNode;

public interface Asm {
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
