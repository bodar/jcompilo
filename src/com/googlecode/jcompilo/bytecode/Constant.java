package com.googlecode.jcompilo.bytecode;

public enum Constant {
    Utf8(1, -1),
    Integer(3, 4),
    Float(4, 4),
    Long(5, 8),
    Double(6, 8),
    Class(7, 2),
    String(8, 2),
    Field(9, 4),
    Method(10, 4),
    InterfaceMethod(11, 4),
    NameAndType(12, 4),
    MethodHandle(15, 3),
    MethodType(16, 2),
    InvokeDynamic(18, 4);

    public final int tag, size;

    Constant(int tag, int size) {
        this.tag = tag;
        this.size = size;
    }

    private static final Constant[] constants;

    static {
        constants = new Constant[19];
        for (Constant c : Constant.values()) constants[c.tag] = c;
    }

    public static Constant constant(int tag) {
        try {
            Constant constant = constants[tag];
            if (constant != null) return constant;
        } catch (IndexOutOfBoundsException ignored) {
        }
        throw new ClassFormatError("Unknown tag: " + tag);
    }
}
