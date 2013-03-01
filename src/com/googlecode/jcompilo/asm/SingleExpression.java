package com.googlecode.jcompilo.asm;

import com.googlecode.totallylazy.Callables;
import com.googlecode.totallylazy.Function;
import com.googlecode.totallylazy.Mapper;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Some;
import com.googlecode.totallylazy.annotations.multimethod;
import com.googlecode.totallylazy.multi;
import com.tonicsystems.jarjar.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;

import static com.googlecode.jcompilo.asm.Asm.isStatic;
import static com.googlecode.totallylazy.Option.option;
import static com.googlecode.totallylazy.Predicates.instanceOf;
import static com.googlecode.totallylazy.Sequences.repeat;

public class SingleExpression {
    public static Pair<InsnList, LabelNode> extract(final InsnList insnList, final Predicate<? super AbstractInsnNode> predicate) {
        return extractLast(insnList, predicate).get();
    }

    public static Sequence<Pair<InsnList, LabelNode>> extractAll(final InsnList insnList, final Predicate<? super AbstractInsnNode> predicate) {
        return repeat(new Function<Option<Pair<InsnList, LabelNode>>>() {
            @Override
            public Option<Pair<InsnList, LabelNode>> call() throws Exception {
                return extractLast(insnList, predicate);
            }
        }).takeWhile(instanceOf(Some.class)).map(Callables.<Pair<InsnList, LabelNode>>value());
    }

    private static Option<Pair<InsnList, LabelNode>> extractLast(final InsnList insnList, final Predicate<? super AbstractInsnNode> predicate) {
        return findLast(insnList, predicate).map(new Mapper<AbstractInsnNode, Pair<InsnList, LabelNode>>() {
            @Override
            public Pair<InsnList, LabelNode> call(final AbstractInsnNode matchedNode) throws Exception {
                return replace(insnList, matchedNode);
            }
        });
    }

    private static Pair<InsnList, LabelNode> replace(final InsnList insnList, final AbstractInsnNode matchedNode) {
        LabelNode placeHolder = new LabelNode();
        insnList.insert(matchedNode, placeHolder);
        return Pair.pair(remove(insnList, matchedNode, 1), placeHolder);
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

    @multimethod
    private static int needed(final FieldInsnNode node) {
        int opcode = node.getOpcode();
        if(opcode == Opcodes.GETFIELD) return 1;
        return 0;
    }

    private static Option<AbstractInsnNode> findLast(final InsnList insnList, final Predicate<? super AbstractInsnNode> predicate) {
        AbstractInsnNode node = insnList.getLast();
        while (node != null && !predicate.matches(node)) node = node.getPrevious();
        return option(node);
    }

}
