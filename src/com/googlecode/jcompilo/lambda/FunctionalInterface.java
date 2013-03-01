package com.googlecode.jcompilo.lambda;

import com.googlecode.jcompilo.asm.Asm;
import com.googlecode.totallylazy.Callables;
import com.googlecode.totallylazy.Eq;
import com.googlecode.totallylazy.Mapper;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Triple;
import com.googlecode.totallylazy.annotations.multimethod;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

import static com.googlecode.jcompilo.asm.Asm.instructions;
import static com.googlecode.totallylazy.Callables.second;
import static com.googlecode.totallylazy.Callables.third;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Unchecked.cast;

public class FunctionalInterface extends Eq {
    public final Type classType;
    public final Sequence<Type> argumentTypes;
    public final Type returnType;
    public final InsnList body;
    public final Sequence<Pair<InsnList, Type>> constructorArguments;
    private final String name;
    private final String bodyString;

    private FunctionalInterface(final Type classType, final Sequence<Type> argumentTypes, final Type returnType,
                                final InsnList body, final Sequence<Pair<InsnList, Type>> constructorArguments) {
        this.classType = classType;
        this.argumentTypes = argumentTypes;
        this.returnType = returnType;
        this.body = body;
        this.constructorArguments = constructorArguments;
        this.bodyString = Asm.toString(body);
        this.name = generateName(classType);
    }

    private String generateName(final Type classType) {
        return "com/googlecode/jcompilo/lambda/" + classType.getInternalName() + "/hash" + String.valueOf(bodyString.hashCode()).replace("-", "");
    }

    public static FunctionalInterface functionalInterface(final Type classType, final Sequence<Type> argumentTypes, final Type returnType, final InsnList body, final Sequence<Pair<InsnList, Type>> constructorArguments) {
        return new FunctionalInterface(classType, argumentTypes, returnType, body, constructorArguments);
    }

    public static <T> Mapper<Field, T> value(final Object instance) {
        return new Mapper<Field, T>() {
            @Override
            public T call(final Field field) throws Exception {
                return cast(field.get(instance));
            }
        };
    }

    public Type type() {
        return Type.getType("L" + name + ";");
    }

    @Override
    public String toString() {
        return classType.getClassName() + "\nconstructor(" + constructorArguments.map(Callables.second(Type.class)) + ")\n" +
                constructorArguments.map(Callables.first(InsnList.class)).map(asString()).toString("\n\n") + "\n\n" +
                "method(" + argumentTypes.toString(", ") + ")" + returnType + "\n" +
                bodyString + "\n";
    }

    private Mapper<InsnList, String> asString() {
        return new Mapper<InsnList, String>() {
            @Override
            public String call(final InsnList list) throws Exception {
                return Asm.toString(list);
            }
        };
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @multimethod
    public boolean equals(final FunctionalInterface functionalInterface) {
        return this.toString().equals(functionalInterface.toString());
    }

    public InsnList construct() {
        return Asm.construct(type(), constructorArguments);
    }

    public Sequence<Pair<String, Type>> fields() {
        return constructorArguments.map(second(Type.class)).zipWithIndex().map(Callables.<Number, Type, String>first(new Mapper<Number, String>() {
            @Override
            public String call(final Number index) throws Exception {
                return "argument" + index;
            }
        })).realise();
    }
}