package com.googlecode.jcompilo.asm;

import com.googlecode.jcompilo.Resource;
import com.googlecode.jcompilo.ResourceHandler;
import com.googlecode.totallylazy.Mapper;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.collections.PersistentList;
import com.googlecode.totallylazy.predicates.LogicalPredicate;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.CheckClassAdapter;

import java.lang.annotation.Annotation;
import java.util.Date;

import static com.googlecode.jcompilo.Resource.constructors.resource;
import static com.googlecode.jcompilo.asm.Asm.annotations;
import static com.googlecode.jcompilo.asm.Asm.predicates.annotation;
import static com.googlecode.totallylazy.Callables.first;
import static com.googlecode.totallylazy.Debug.debugging;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Sequences.one;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.collections.PersistentList.constructors;
import static com.googlecode.totallylazy.collections.PersistentList.constructors.list;

public class AsmResourceHandler implements ResourceHandler {
    private final PersistentList<Pair<Type, AsmMethodHandler>> processors;
    private final boolean verify;

    private AsmResourceHandler(Iterable<? extends Pair<Type, AsmMethodHandler>> processors, boolean verify) {
        this.processors = list(processors);
        this.verify = verify;
    }

    public static AsmResourceHandler asmResourceHandler(Iterable<? extends Pair<Type, AsmMethodHandler>> processors, boolean verify) {
        return new AsmResourceHandler(processors, verify);
    }

    public static AsmResourceHandler asmResourceHandler(Iterable<? extends Pair<Type, AsmMethodHandler>> processors) {
        return asmResourceHandler(processors, debugging());
    }

    public static AsmResourceHandler asmResourceHandler(boolean verify) {
        return asmResourceHandler(constructors.<Pair<Type, AsmMethodHandler>>empty(), verify);
    }

    public static AsmResourceHandler asmResourceHandler() {
        return asmResourceHandler(debugging());
    }

    public AsmResourceHandler add(Class<? extends Annotation> annotation, AsmMethodHandler asmProcessor) {
        return add(Type.getType(annotation), asmProcessor);
    }

    public AsmResourceHandler add(Type annotation, AsmMethodHandler asmProcessor) {
        return asmResourceHandler(processors.cons(Pair.<Type, AsmMethodHandler>pair(annotation, asmProcessor)), verify);
    }

    @Override
    public boolean matches(String name) {
        return name.endsWith(".class");
    }

    @Override
    public Sequence<Resource> handle(final Resource resource) {
        if (processors.isEmpty()) return one(resource);

        final ClassNode classNode = Asm.classNode(resource.bytes());

        Sequence<ClassNode> classNodes = Asm.<MethodNode>seq(classNode.methods).flatMap(new Mapper<MethodNode, Sequence<ClassNode>>() {
            @Override
            public Sequence<ClassNode> call(final MethodNode method) throws Exception {
                return sequence(processors).
                        filter(where(first(Type.class), hasAnnotation(method))).
                        flatMap(processPair(method, classNode));
            }
        }).realise();

        if (classNodes.isEmpty()) return one(resource);

        return classNodes.map(asResource(resource.modified()));
    }

    private Mapper<ClassNode, Resource> asResource(final Date modified) {
        return new Mapper<ClassNode, Resource>() {
            @Override
            public Resource call(final ClassNode node) throws Exception {
                ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                node.accept(verify ? new CheckClassAdapter(writer) : writer);
                return resource(node.name + ".class", modified, writer.toByteArray());
            }
        };
    }

    private static Mapper<Pair<Type, AsmMethodHandler>, Sequence<ClassNode>> processPair(final MethodNode method, final ClassNode classNode) {
        return new Mapper<Pair<Type, AsmMethodHandler>, Sequence<ClassNode>>() {
            @Override
            public Sequence<ClassNode> call(final Pair<Type, AsmMethodHandler> pair) throws Exception {
                method.invisibleAnnotations.remove(annotations(method).find(annotation(pair.first())).get());
                return pair.second().process(classNode, method);
            }
        };
    }

    private static LogicalPredicate<Type> hasAnnotation(final MethodNode methodNode) {
        return new LogicalPredicate<Type>() {
            @Override
            public boolean matches(final Type other) {
                return Asm.hasAnnotation(methodNode, other);
            }
        };
    }
}
