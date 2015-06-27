package com.googlecode.jcompilo.tco;

import com.googlecode.jcompilo.asm.AsmMethodHandler;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.predicates.LogicalPredicate;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.Type;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import jdk.internal.org.objectweb.asm.tree.FrameNode;
import jdk.internal.org.objectweb.asm.tree.InsnList;
import jdk.internal.org.objectweb.asm.tree.JumpInsnNode;
import jdk.internal.org.objectweb.asm.tree.LabelNode;
import jdk.internal.org.objectweb.asm.tree.MethodInsnNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;
import jdk.internal.org.objectweb.asm.tree.VarInsnNode;

import static com.googlecode.jcompilo.asm.Asm.functions.name;
import static com.googlecode.jcompilo.asm.Asm.functions.nextInstruction;
import static com.googlecode.jcompilo.asm.Asm.functions.opcode;
import static com.googlecode.jcompilo.asm.Asm.functions.owner;
import static com.googlecode.jcompilo.asm.Asm.initialLocalVariables;
import static com.googlecode.jcompilo.asm.Asm.instructions;
import static com.googlecode.jcompilo.asm.Asm.store;
import static com.googlecode.totallylazy.predicates.Predicates.and;
import static com.googlecode.totallylazy.predicates.Predicates.between;
import static com.googlecode.totallylazy.predicates.Predicates.is;
import static com.googlecode.totallylazy.predicates.Predicates.where;
import static com.googlecode.totallylazy.Sequences.one;
import static com.googlecode.totallylazy.Sequences.sequence;
import static java.lang.String.format;
import static jdk.internal.org.objectweb.asm.Opcodes.GOTO;
import static jdk.internal.org.objectweb.asm.Opcodes.IRETURN;
import static jdk.internal.org.objectweb.asm.Opcodes.RETURN;

public class TailRecHandler implements AsmMethodHandler {
    private TailRecHandler() {}

    public static TailRecHandler tailRecHandler() {return new TailRecHandler();}

    @Override
    public Sequence<ClassNode> process(ClassNode classNode, MethodNode methodNode) {
        if (!tailRecursive(classNode, methodNode))
            throw new UnsupportedOperationException(format("%s.%s is not tail recursive", classNode.name, methodNode.name));

        insertStartFrame(methodNode);
        InsnList gotoStart = gotoStart(classNode, methodNode);
        for (MethodInsnNode recursiveCall : instructions(methodNode).safeCast(MethodInsnNode.class).filter(sameMethod(classNode, methodNode))) {
            methodNode.instructions.remove(recursiveCall.getNext()); // Remove Return
            methodNode.instructions.insert(recursiveCall, gotoStart);
            methodNode.instructions.remove(recursiveCall); // finally remove recursive call
        }
        return one(classNode);
    }

    private void insertStartFrame(MethodNode methodNode) {
        InsnList insnList = new InsnList();
        insnList.add(new LabelNode());
        insnList.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null)); // Needed for Java 7+
        methodNode.instructions.insert(insnList);
    }

    private InsnList gotoStart(ClassNode classNode, MethodNode methodNode) {
        InsnList insnList = new InsnList();
        int index = 0;
        for (Type variable : initialLocalVariables(classNode, methodNode)) {
            insnList.insert(new VarInsnNode(store(variable), index));
            index += variable.getSize();
        }
        insnList.add(new JumpInsnNode(GOTO, (LabelNode) methodNode.instructions.getFirst()));
        return insnList;
    }

    private boolean tailRecursive(ClassNode classNode, MethodNode method) {
        Sequence<MethodInsnNode> recursiveCalls = instructions(method).safeCast(MethodInsnNode.class).
                filter(sameMethod(classNode, method));
        return !recursiveCalls.isEmpty() && recursiveCalls.map(nextInstruction).forAll(where(opcode, between(IRETURN, RETURN)));
    }

    private LogicalPredicate<MethodInsnNode> sameMethod(ClassNode classNode, MethodNode method) {
        return and(where(owner, is(classNode.name)), where(name, is(method.name)));
    }

}
