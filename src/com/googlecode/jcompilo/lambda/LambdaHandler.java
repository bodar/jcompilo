package com.googlecode.jcompilo.lambda;

import com.googlecode.jcompilo.asm.Asm;
import com.googlecode.jcompilo.asm.AsmMethodHandler;
import com.googlecode.jcompilo.asm.SingleExpression;
import com.googlecode.totallylazy.Callables;
import com.googlecode.totallylazy.Mapper;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.annotations.multimethod;
import com.googlecode.totallylazy.multi;
import com.googlecode.totallylazy.predicates.LogicalPredicate;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.HashMap;
import java.util.Map;

import static com.googlecode.jcompilo.asm.Asm.functions.name;
import static com.googlecode.jcompilo.asm.Asm.instructions;
import static com.googlecode.jcompilo.asm.Asm.load;
import static com.googlecode.totallylazy.Callables.first;
import static com.googlecode.totallylazy.Maps.pairs;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Strings.startsWith;

public class LambdaHandler implements AsmMethodHandler {
    public static final LogicalPredicate<AbstractInsnNode> lambda = LambdaHandler.<AbstractInsnNode, MethodInsnNode>typeSafe(MethodInsnNode.class, where(name, startsWith("λ")));
    private final ClassGenerator generator;

    public LambdaHandler(final ClassGenerator generator) {
        this.generator = generator;
    }

    @Override
    public Sequence<ClassNode> process(final ClassNode classNode, final MethodNode method) {
        InsnList original = method.instructions;
        Pair<InsnList, LabelNode> lambdaBody = SingleExpression.extract(original, LambdaHandler.lambda);
        FunctionalInterface functionalInterface = functionalInterface(lambdaBody.first());
        ClassNode lambdaClass = generator.generateClass(functionalInterface);
        InsnList createLambda = Asm.construct(functionalInterface.type());
        LabelNode placeHolder = lambdaBody.second();
        original.insert(placeHolder, createLambda);
        original.remove(placeHolder);
        return sequence(classNode, lambdaClass);
    }

    public static <T, S extends T> LogicalPredicate<T> typeSafe(final Class<S> subClass, final Predicate<? super S> predicate) {
        return new LogicalPredicate<T>() {
            @Override
            public boolean matches(final T other) {
                return subClass.isInstance(other) && predicate.matches(subClass.cast(other));
            }
        };
    }

    public static FunctionalInterface functionalInterface(final InsnList body) {
        MethodInsnNode lambda = (MethodInsnNode) body.getLast();
        InsnList arguments = drop(body, Asm.numberOfArguments(lambda) - 1);
        Sequence<Type> argumentTypes = argTypes(arguments);
        replace(body, arguments);
        body.remove(lambda);
        Type returnType = returnType(body.getLast());
        body.add(new InsnNode(Asm.returns(returnType)));
        return FunctionalInterface.functionalInterface(returnType(lambda), argumentTypes, returnType, body);
    }

    private static Sequence<Type> argTypes(final InsnList arguments) {
        return instructions(arguments).safeCast(FieldInsnNode.class).map(new Mapper<FieldInsnNode, Type>() {
            @Override
            public Type call(final FieldInsnNode node) throws Exception {
                return returnType(node);
            }
        });
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

    private static Type returnType(final AbstractInsnNode node) { return new multi() {}.method(node); }
    @multimethod private static Type returnType(final MethodInsnNode node) { return Type.getReturnType(node.desc); }
    @multimethod private static Type returnType(final FieldInsnNode node) { return Type.getReturnType(node.desc); }
}