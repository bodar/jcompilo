package com.googlecode.jcompilo.asm;

import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.annotations.multimethod;
import com.googlecode.totallylazy.multi;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;

import static com.googlecode.jcompilo.asm.Asm.isStatic;

public class SingleExpression {
    public static InsnList extract(final InsnList insnList, final Predicate<? super AbstractInsnNode> predicate, final LabelNode placeHolder) {
        AbstractInsnNode node = findLast(insnList, predicate);
        insnList.insert(node, placeHolder);
        return remove(insnList, node, 1);
    }

    private static InsnList remove(InsnList input, final AbstractInsnNode node, int count) {
        InsnList result = new InsnList();
        int needed = needed(node) + count - 1;
        AbstractInsnNode previous = node.getPrevious();
        input.remove(node);
        result.insert(node);
        if (needed == 0) return result;
        result.insert(remove(input, previous, needed));
        return result;
    }

    private static int needed(final AbstractInsnNode node) {
        return new multi() {}.<Integer>methodOption(node).getOrElse(0);
    }

    @multimethod
    private static int needed(final MethodInsnNode node) {
        return (isStatic(node) ? 0 : 1) + Type.getType(node.desc).getArgumentTypes().length;
    }

    private static AbstractInsnNode findLast(final InsnList insnList, final Predicate<? super AbstractInsnNode> predicate) {
        AbstractInsnNode node = insnList.getLast();
        while (!predicate.matches(node)) node = node.getPrevious();
        return node;
    }

}
