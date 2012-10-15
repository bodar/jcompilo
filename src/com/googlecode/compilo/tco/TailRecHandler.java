package com.googlecode.compilo.tco;

import com.googlecode.compilo.Resource;
import com.googlecode.compilo.ResourceHandler;
import com.googlecode.totallylazy.Debug;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.util.CheckClassAdapter;
import sun.org.mozilla.javascript.internal.debug.Debugger;

import java.lang.annotation.Annotation;
import java.util.List;

import static com.googlecode.compilo.Resource.constructors.resource;
import static com.googlecode.compilo.tco.Asm.functions.desc;
import static com.googlecode.compilo.tco.Asm.functions.name;
import static com.googlecode.compilo.tco.Asm.functions.nextInstruction;
import static com.googlecode.compilo.tco.Asm.functions.opcode;
import static com.googlecode.compilo.tco.Asm.functions.owner;
import static com.googlecode.totallylazy.Debug.debugging;
import static com.googlecode.totallylazy.Predicates.between;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;
import static java.lang.String.format;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Type.getDescriptor;

@SuppressWarnings("unchecked")
public class TailRecHandler implements ResourceHandler {
    @Override
    public boolean matches(String name) {
        return name.endsWith(".class");
    }

    @Override
    public Resource handle(Resource resource) {
        ClassReader reader = new ClassReader(resource.bytes());
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, 0);

        Sequence<MethodNode> methods = Sequences.<MethodNode>sequence(classNode.methods);
        for (MethodNode method : methods) {
            if (hasAnnotation(method, tailrec.class)) {
                if (!tailRecursive(classNode, method))
                    throw new UnsupportedOperationException(format("%s.%s is not tail recursive", classNode.name, method.name));
                LabelNode recur = new LabelNode();
                Type[] argumentTypes = Type.getType(method.desc).getArgumentTypes();
                int numberOfArguments = argumentTypes.length + 1;

                InsnList instructions = method.instructions;
                for (AbstractInsnNode insnNode : instructions(method)) {
//                    if(insnNode instanceof VarInsnNode) {
//                        VarInsnNode varInsnNode = (VarInsnNode) insnNode;
//                        instructions.set(varInsnNode, new VarInsnNode(varInsnNode.getOpcode(), varInsnNode.var + numberOfArguments));
//                    }
                    if(insnNode instanceof MethodInsnNode) {
                        MethodInsnNode methodInsnNode = (MethodInsnNode) insnNode;
                        if(methodInsnNode.owner.equals(classNode.name) && methodInsnNode.name.equals(method.name)) {
                            instructions.remove(methodInsnNode.getNext());
                            instructions.remove(methodInsnNode);
                            instructions.insert(methodInsnNode, end(recur));
                        }
                    }
                }
                instructions.insert(start(classNode, recur));
            }
        }

        ClassWriter writer = new ClassWriter(0);
        classNode.accept(debugging() ? new CheckClassAdapter(writer) : writer);
        return resource(resource.name(), writer.toByteArray());
    }

    private InsnList start(ClassNode classNode, LabelNode recur) {
        InsnList insnList = new InsnList();
//        insnList.add(new VarInsnNode(Opcodes.ALOAD, 0));
//        insnList.add(new VarInsnNode(Opcodes.ASTORE, 1));
        insnList.add(recur);
//        insnList.add(new FrameNode(Opcodes.F_APPEND, 1, new Object[]{classNode.name}, 0, null));
        insnList.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
        return insnList;
    }

    private InsnList end(LabelNode recur) {
        InsnList insnList = new InsnList();
        insnList.add(new VarInsnNode(Opcodes.ASTORE, 0));
        insnList.add(new JumpInsnNode(Opcodes.GOTO, recur));
        return insnList;
    }

    private boolean tailRecursive(ClassNode classNode, MethodNode method) {
        Sequence<MethodInsnNode> recursiveCalls = instructions(method).safeCast(MethodInsnNode.class).
                filter(where(owner, is(classNode.name)).and(where(name, is(method.name))));
        return !recursiveCalls.isEmpty() && recursiveCalls.map(nextInstruction).forAll(where(opcode, between(IRETURN, RETURN)));
    }

    private Sequence<AbstractInsnNode> instructions(MethodNode method) {
        return Sequences.<AbstractInsnNode>forwardOnly(method.instructions.iterator());
    }

    private boolean hasAnnotation(MethodNode method, final Class<? extends Annotation> aClass) {
        return TailRecHandler.<AnnotationNode>seq(method.invisibleAnnotations).exists(where(desc, is(getDescriptor(aClass))));
    }

    @SuppressWarnings("unchecked")
    private static <T> Sequence<T> seq(List list) {
        return list != null ? Sequences.<T>sequence(list) : Sequences.<T>empty();
    }

}
