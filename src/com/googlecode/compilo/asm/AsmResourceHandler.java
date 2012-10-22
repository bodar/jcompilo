package com.googlecode.compilo.asm;

import com.googlecode.compilo.Resource;
import com.googlecode.compilo.ResourceHandler;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.collections.ImmutableList;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.CheckClassAdapter;

import java.lang.annotation.Annotation;

import static com.googlecode.compilo.Resource.constructors.resource;
import static com.googlecode.compilo.asm.Asm.annotations;
import static com.googlecode.compilo.asm.Asm.hasAnnotation;
import static com.googlecode.compilo.asm.Asm.predicates.annotation;
import static com.googlecode.totallylazy.Debug.debugging;
import static com.googlecode.totallylazy.collections.ImmutableList.constructors;
import static com.googlecode.totallylazy.collections.ImmutableList.constructors.list;

public class AsmResourceHandler implements ResourceHandler {
    private final ImmutableList<Pair<Type, AsmMethodHandler>> processors;
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
    public Resource handle(Resource resource) {
        if (processors.isEmpty()) return resource;

        ClassReader reader = new ClassReader(resource.bytes());
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, 0);

        boolean foundMatch = false;
        for (MethodNode method : Asm.<MethodNode>seq(classNode.methods)) {
            for (Pair<Type, AsmMethodHandler> p : processors) {
                if (hasAnnotation(method, p.first())) {
                    foundMatch = true;
                    p.second().process(classNode, method);
                    method.invisibleAnnotations.remove(annotations(method).find(annotation(p.first())).get());
                }
            }
        }

        if (!foundMatch) return resource;

        ClassWriter writer = new ClassWriter(0);
        classNode.accept(verify ? new CheckClassAdapter(writer) : writer);
        return resource(resource.name(), resource.modified(), writer.toByteArray());
    }
}
