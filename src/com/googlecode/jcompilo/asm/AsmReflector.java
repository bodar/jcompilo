package com.googlecode.jcompilo.asm;

import com.googlecode.jcompilo.Resources;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.UnaryFunction;
import com.googlecode.totallylazy.predicates.LogicalPredicate;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import static com.googlecode.totallylazy.Predicates.notNullValue;

public class AsmReflector {
    private final Resources resources;

    public AsmReflector(final Resources resources) {
        this.resources = resources;
    }

    public Sequence<MethodNode> allMethods(final String className) {
        return allClasses(className).flatMap(Asm::methods);
    }

    public Sequence<ClassNode> allClasses(final String className) {
        ClassNode classNode = classNode(className);
        return Asm.<String>seq(classNode.interfaces).
                cons(classNode.superName).
                filter(notNullValue()).
                map(s -> s.replace('/', '.')).
                map(classNode());
    }

    public final Function1<String, ClassNode> classNode() {
        return AsmReflector.this::classNode;
    }

    public ClassNode classNode(final String name) {
        return Asm.classNode(resources.get(name).get().bytes());
    }

    public static LogicalPredicate<Integer> contains(final int value) {
        return new LogicalPredicate<Integer>() {
            @Override
            public boolean matches(final Integer other) {
                return (other & value) != 0;
            }
        };
    }

}
