package com.googlecode.jcompilo.asm;

import com.googlecode.jcompilo.Resource;
import com.googlecode.jcompilo.ResourceHandler;
import com.googlecode.totallylazy.functions.Function1;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.collections.PersistentList;
import com.googlecode.totallylazy.predicates.Predicate;
import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.Type;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;

import java.lang.annotation.Annotation;
import java.util.Date;

import static com.googlecode.jcompilo.Resource.constructors.resource;
import static com.googlecode.jcompilo.asm.Asm.annotations;
import static com.googlecode.jcompilo.asm.Asm.predicates.annotation;
import static com.googlecode.totallylazy.functions.Callables.first;
import static com.googlecode.totallylazy.predicates.Predicates.where;
import static com.googlecode.totallylazy.Sequences.one;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.collections.PersistentList.constructors;
import static com.googlecode.totallylazy.collections.PersistentList.constructors.list;

public class AsmResourceHandler implements ResourceHandler {
    private final PersistentList<Pair<Type, AsmMethodHandler>> processors;

    private AsmResourceHandler(Iterable<? extends Pair<Type, AsmMethodHandler>> processors) {
        this.processors = list(processors);
    }

    public static AsmResourceHandler asmResourceHandler(Iterable<? extends Pair<Type, AsmMethodHandler>> processors) {
        return new AsmResourceHandler(processors);
    }

    public static AsmResourceHandler asmResourceHandler() {
        return asmResourceHandler(constructors.<Pair<Type, AsmMethodHandler>>empty());
    }

    public AsmResourceHandler add(Class<? extends Annotation> annotation, AsmMethodHandler asmProcessor) {
        return add(Type.getType(annotation), asmProcessor);
    }

    public AsmResourceHandler add(Type annotation, AsmMethodHandler asmProcessor) {
        return asmResourceHandler(processors.cons(Pair.<Type, AsmMethodHandler>pair(annotation, asmProcessor)));
    }

    @Override
    public boolean matches(String name) {
        return name.endsWith(".class");
    }

    @Override
    public Sequence<Resource> handle(final Resource resource) {
        if (processors.isEmpty()) return one(resource);

        final ClassNode classNode = Asm.classNode(resource.bytes());

        Sequence<ClassNode> classNodes = Asm.methods(classNode).flatMap(method -> sequence(processors).
                filter(where(first(Type.class), hasAnnotation(method))).
                flatMap(processPair(method, classNode))).realise();

        if (classNodes.isEmpty()) return one(resource);

        return classNodes.map(asResource(resource.modified()));
    }

    private Function1<ClassNode, Resource> asResource(final Date modified) {
        return node -> {
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            node.accept(writer);
            return resource(node.name + ".class", modified, writer.toByteArray());
        };
    }

    private static Function1<Pair<Type, AsmMethodHandler>, Sequence<ClassNode>> processPair(final MethodNode method, final ClassNode classNode) {
        return pair -> {
            method.invisibleAnnotations.remove(annotations(method).find(annotation(pair.first())).get());
            return pair.second().process(classNode, method);
        };
    }

    private static Predicate<Type> hasAnnotation(final MethodNode methodNode) {
        return other -> Asm.hasAnnotation(methodNode, other);
    }
}
