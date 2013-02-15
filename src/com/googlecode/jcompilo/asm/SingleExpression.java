package com.googlecode.jcompilo.asm;

import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.multi;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;

import static com.googlecode.jcompilo.asm.Asm.isStatic;

public class SingleExpression {
    public static InsnList extract(final InsnList insnList, final Predicate<? super AbstractInsnNode> predicate, final LabelNode placeHolder) {
        InsnList result = new InsnList();
        AbstractInsnNode node = findLast(insnList, predicate);
        insnList.insert(node, placeHolder);
        remove(insnList, result, node, 1);
        return result;
    }

    private static void remove(InsnList input, final InsnList output, final AbstractInsnNode node, int count) {
        int needed = needed(node) + count - 1;
        AbstractInsnNode previous = node.getPrevious();
        input.remove(node);
        output.insert(node);
        if (needed == 0) return;
        remove(input, output, previous, needed);
    }

    private static int needed(final MethodInsnNode node) {
        return (isStatic(node) ? 0 : 1) + Type.getType(node.desc).getArgumentTypes().length;
    }

    private static int needed(final AbstractInsnNode node) {
        return new multi() {
        }.<Integer>methodOption(node).getOrElse(0);
    }

    private static AbstractInsnNode findLast(final InsnList insnList, final Predicate<? super AbstractInsnNode> predicate) {
        AbstractInsnNode node = insnList.getLast();
        while (!predicate.matches(node)) node = node.getPrevious();
        return node;
    }

}
