package com.googlecode.jcompilo.lambda;

import com.googlecode.jcompilo.asm.Asm;
import com.googlecode.totallylazy.Callables;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.multi;
import com.googlecode.totallylazy.predicates.LogicalPredicate;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.HashMap;
import java.util.Map;

import static com.googlecode.jcompilo.asm.Asm.load;
import static com.googlecode.totallylazy.Callables.first;
import static com.googlecode.totallylazy.Maps.pairs;
import static com.googlecode.totallylazy.Predicates.where;

public class LambdaHandler {
    public static void rewriteArguments(final InsnList body) {
        MethodInsnNode lambda = (MethodInsnNode) body.getLast();
        InsnList arguments = drop(body, Asm.numberOfArguments(lambda) - 1);
        replace(body, arguments);
        body.remove(lambda);
        Type returnType = returnType(body.getLast());
        body.add(new InsnNode(Asm.returns(returnType)));
    }

    private static void replace(final InsnList body, final InsnList arguments) {
        Map<FieldInsnNode, VarInsnNode> load = fieldToLoad(arguments);

        for (FieldInsnNode node : fieldInstructions(body)) {
            Option<VarInsnNode> loadInstructions = pairs(load).
                    find(where(first(FieldInsnNode.class), matches(node))).
                    map(Callables.second(VarInsnNode.class));
            if (!loadInstructions.isEmpty()) {
                body.insert(node, loadInstructions.get());
                body.remove(node);
            }
        }
    }

    private static Map<FieldInsnNode, VarInsnNode> fieldToLoad(final InsnList arguments) {
        Map<FieldInsnNode, VarInsnNode> load = new HashMap<FieldInsnNode, VarInsnNode>();
        int index = 1;
        for (FieldInsnNode fieldInsnNode : fieldInstructions(arguments)) {
            Type type = Type.getType(fieldInsnNode.desc);
            load.put(fieldInsnNode, new VarInsnNode(load(type), index));
            index += type.getSize();
        }
        return load;
    }

    private static LogicalPredicate<FieldInsnNode> matches(final FieldInsnNode fieldInsnNode) {
        return new LogicalPredicate<FieldInsnNode>() {
            @Override
            public boolean matches(final FieldInsnNode other) {
                return fieldInsnNode.desc.equals(other.desc) &&
                        fieldInsnNode.owner.equals(other.owner) &&
                        fieldInsnNode.name.equals(other.name) &&
                        fieldInsnNode.getOpcode() == other.getOpcode();
            }
        };
    }

    private static Sequence<FieldInsnNode> fieldInstructions(final InsnList body) {
        return Asm.instructions(body).safeCast(FieldInsnNode.class);
    }

    private static InsnList drop(final InsnList insnList, final int count) {
        InsnList result = new InsnList();
        for (int i = 0; i < count; i++) {
            AbstractInsnNode instruction = insnList.getFirst();
            insnList.remove(instruction);
            result.add(instruction);
        }
        return result;
    }

    private static Type returnType(final AbstractInsnNode node) {
        return new multi() {}.method(node);
    }

    private static Type returnType(final MethodInsnNode node) {
        return Type.getReturnType(node.desc);
    }

    private static Type returnType(final FieldInsnNode node) {
        return Type.getReturnType(node.desc);
    }
}