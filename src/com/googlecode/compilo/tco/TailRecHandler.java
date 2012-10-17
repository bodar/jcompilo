package com.googlecode.compilo.tco;

import com.googlecode.compilo.AsmMethodHandler;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Unchecked;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.lang.annotation.Annotation;
import java.util.List;

import static com.googlecode.compilo.tco.Asm.annotations;
import static com.googlecode.compilo.tco.Asm.functions.name;
import static com.googlecode.compilo.tco.Asm.functions.nextInstruction;
import static com.googlecode.compilo.tco.Asm.functions.opcode;
import static com.googlecode.compilo.tco.Asm.functions.owner;
import static com.googlecode.compilo.tco.Asm.predicates.annotation;
import static com.googlecode.totallylazy.Predicates.between;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;
import static java.lang.String.format;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.RETURN;

public class TailRecHandler implements AsmMethodHandler {
    private final Class<? extends Annotation> annotation;
    private TailRecHandler(Class<? extends Annotation> annotation) {this.annotation = annotation;}

    public static TailRecHandler tailRecHandler(Class<? extends Annotation> annotation) {return new TailRecHandler(annotation);}

    @Override
    public void process(ClassNode classNode, MethodNode methodNode) {
        if (!tailRecursive(classNode, methodNode))
            throw new UnsupportedOperationException(format("%s.%s is not tail recursive", classNode.name, methodNode.name));
        LabelNode recur = new LabelNode();

        InsnList instructions = methodNode.instructions;
        for (AbstractInsnNode insnNode : Asm.instructions(methodNode)) {
            if (insnNode instanceof MethodInsnNode) {
                MethodInsnNode methodInsnNode = (MethodInsnNode) insnNode;
                if (methodInsnNode.owner.equals(classNode.name) && methodInsnNode.name.equals(methodNode.name)) {
                    instructions.remove(methodInsnNode.getNext()); // Remove Return
                    instructions.insert(methodInsnNode, end(recur, Unchecked.<List<LocalVariableNode>>cast(methodNode.localVariables))); // Insert Goto
                    instructions.remove(methodInsnNode); // finally remove recursive call
                }
            }
        }
        instructions.insert(start(recur));
        methodNode.invisibleAnnotations.remove(annotations(methodNode).find(annotation(annotation)).get());
    }

    private InsnList start(LabelNode recur) {
        InsnList insnList = new InsnList();
        insnList.add(recur);
        insnList.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
        return insnList;
    }

    private InsnList end(LabelNode recur, List<LocalVariableNode> localVariableNodes) {
        InsnList insnList = new InsnList();
        for (int i = 0, localVariableNodesSize = localVariableNodes.size(); i < localVariableNodesSize; i++) {
            insnList.insert(new VarInsnNode(Asm.store(localVariableNodes.get(i)), i));
        }
        insnList.add(new JumpInsnNode(Opcodes.GOTO, recur));
        return insnList;
    }

    private boolean tailRecursive(ClassNode classNode, MethodNode method) {
        Sequence<MethodInsnNode> recursiveCalls = Asm.instructions(method).safeCast(MethodInsnNode.class).
                filter(where(owner, is(classNode.name)).and(where(name, is(method.name))));
        return !recursiveCalls.isEmpty() && recursiveCalls.map(nextInstruction).forAll(where(opcode, between(IRETURN, RETURN)));
    }

}
