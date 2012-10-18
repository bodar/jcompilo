package com.googlecode.compilo.tco;

import com.googlecode.compilo.AsmMethodHandler;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.predicates.LogicalPredicate;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import static com.googlecode.compilo.tco.Asm.functions.localVariableType;
import static com.googlecode.compilo.tco.Asm.functions.name;
import static com.googlecode.compilo.tco.Asm.functions.nextInstruction;
import static com.googlecode.compilo.tco.Asm.functions.opcode;
import static com.googlecode.compilo.tco.Asm.functions.owner;
import static com.googlecode.compilo.tco.Asm.instructions;
import static com.googlecode.compilo.tco.Asm.store;
import static com.googlecode.totallylazy.Predicates.and;
import static com.googlecode.totallylazy.Predicates.between;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;
import static java.lang.String.format;
import static org.objectweb.asm.Opcodes.F_SAME;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.RETURN;

public class TailRecHandler implements AsmMethodHandler {
    private TailRecHandler() {}

    public static TailRecHandler tailRecHandler() {return new TailRecHandler();}

    @Override
    public void process(ClassNode classNode, MethodNode methodNode) {
        if (!tailRecursive(classNode, methodNode))
            throw new UnsupportedOperationException(format("%s.%s is not tail recursive", classNode.name, methodNode.name));

        insertStart(methodNode);
        InsnList gotoStart = gotoStart(methodNode);
        for (MethodInsnNode methodInsnNode : instructions(methodNode).safeCast(MethodInsnNode.class).filter(sameMethod(classNode, methodNode))) {
            methodNode.instructions.remove(methodInsnNode.getNext()); // Remove Return
            methodNode.instructions.insert(methodInsnNode, gotoStart);
            methodNode.instructions.remove(methodInsnNode); // finally remove recursive call
        }
    }

    private void insertStart(MethodNode methodNode) {
        InsnList insnList = new InsnList();
        insnList.add(new LabelNode());
        insnList.add(new FrameNode(F_SAME, 0, null, 0, null));
        methodNode.instructions.insert(insnList);
    }

    private InsnList gotoStart(MethodNode methodNode) {
        InsnList insnList = new InsnList();
        int index = 0;
        for (Type variable : arguments(methodNode)) {
            insnList.insert(new VarInsnNode(store(variable), index));
            index += variable.getSize();
        }
        insnList.add(new JumpInsnNode(GOTO, (LabelNode) methodNode.instructions.getFirst()));
        return insnList;
    }

    private Sequence<Type> arguments(MethodNode methodNode) {
        return Asm.<LocalVariableNode>seq(methodNode.localVariables).map(localVariableType);
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
