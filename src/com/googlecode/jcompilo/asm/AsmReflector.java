package com.googlecode.jcompilo.asm;

import com.googlecode.jcompilo.Resources;
import com.googlecode.totallylazy.Mapper;
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
        return allClasses(className).flatMap(new Mapper<ClassNode, Sequence<MethodNode>>() {
            @Override
            public Sequence<MethodNode> call(final ClassNode node) throws Exception {
                return Asm.methods(node);
            }
        });
    }

    public Sequence<ClassNode> allClasses(final String className) {
        ClassNode classNode = classNode(className);
        return Asm.<String>seq(classNode.interfaces).
                cons(classNode.superName).
                filter(notNullValue()).
                map(new UnaryFunction<String>() {
                    @Override
                    public String call(final String s) throws Exception {
                        return s.replace('/', '.');
                    }
                }).
                map(classNode());
    }

    public final Mapper<String, ClassNode> classNode() {
        return new Mapper<String, ClassNode>() {
            @Override
            public ClassNode call(final String s) throws Exception {
                return classNode(s);
            }
        };
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
