package com.googlecode.jcompilo.bytecode;

import com.googlecode.totallylazy.Callers;
import com.googlecode.totallylazy.Destination;
import com.googlecode.totallylazy.Function2;
import com.googlecode.totallylazy.Sources;
import com.googlecode.totallylazy.Streams;
import com.googlecode.totallylazy.Unary;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import static com.googlecode.totallylazy.Closeables.using;
import static com.googlecode.totallylazy.Sequences.sequence;

public class ConstantPoolMapper {
    private final Unary<String> mapper;

    public ConstantPoolMapper(Unary<String> mapper) {
        this.mapper = mapper;
    }

    public ConstantPoolMapper process(Sources sources, Destination destination) {
        return using(sources, destination, new Function2<Sources, Destination, ConstantPoolMapper>() {
            @Override
            public ConstantPoolMapper call(Sources sources, final Destination destination) throws Exception {
                for (Sources.Source source : sources.sources()) {
                    OutputStream outputStream = destination.destination(mapper.call(source.name), source.modified);
                    if (source.name.endsWith(".class")) using(source.input, outputStream, functions.process(ConstantPoolMapper.this));
                    else Streams.copyAndClose(source.input, outputStream);
                }
                return ConstantPoolMapper.this;
            }
        });
    }

    public ConstantPoolMapper process(InputStream inputStream, OutputStream outputStream) throws IOException {
        return process(new BufferedInputStream(inputStream), new BufferedOutputStream(outputStream));
    }

    public ConstantPoolMapper process(BufferedInputStream in, BufferedOutputStream out) throws IOException {
        return process(new DataInputStream(in), new DataOutputStream(out));
    }

    public ConstantPoolMapper process(DataInputStream in, DataOutputStream out) throws IOException {
        int magic = in.readInt();
        if (magic != 0xcafebabe) throw new ClassFormatError("wrong magic: " + magic);
        out.writeInt(magic);

        copy(in, out, 4);

        int size = in.readUnsignedShort();
        out.writeShort(size);

        for (int i = 1; i < size; i++) {
            int tag = in.readUnsignedByte();
            out.writeByte(tag);

            switch (tag) {
                case CONSTANT_UTF8:
                    out.writeUTF(Callers.call(mapper, in.readUTF()));
                    break;
                case CONSTANT_CLASS:
                case CONSTANT_STRING:
                case CONSTANT_MethodType:
                    copy(in, out, 2);
                    break;
                case CONSTANT_INTEGER:
                case CONSTANT_FLOAT:
                case CONSTANT_FIELD:
                case CONSTANT_METHOD:
                case CONSTANT_INTERFACEMETHOD:
                case CONSTANT_NAMEANDTYPE:
                case CONSTANT_InvokeDynamic:
                    copy(in, out, 4);
                    break;
                case CONSTANT_LONG:
                case CONSTANT_DOUBLE:
                    copy(in, out, 8);
                    i++; // "In retrospect, making 8-byte constants take two constant pool entries was a poor choice."
                    // See http://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.4.5
                    break;
                case CONSTANT_MethodHandle:
                    copy(in, out, 3);
                    break;
                default:
                    throw new ClassFormatError("Unknown tag: " + tag + " in " + in);
            }
        }
        Streams.copyAndClose(in, out);
        return this;
    }

    private final byte[] buffer = new byte[1024];

    private void copy(DataInputStream in, DataOutputStream out, int amount) throws IOException {
        in.readFully(buffer, 0, amount);
        out.write(buffer, 0, amount);
    }

    public static final int CONSTANT_UTF8 = 1;
    public static final int CONSTANT_INTEGER = 3;
    public static final int CONSTANT_FLOAT = 4;
    public static final int CONSTANT_LONG = 5;
    public static final int CONSTANT_DOUBLE = 6;
    public static final int CONSTANT_CLASS = 7;
    public static final int CONSTANT_STRING = 8;
    public static final int CONSTANT_FIELD = 9;
    public static final int CONSTANT_METHOD = 10;
    public static final int CONSTANT_INTERFACEMETHOD = 11;
    public static final int CONSTANT_NAMEANDTYPE = 12;
    public static final int CONSTANT_MethodHandle = 15;
    public static final int CONSTANT_MethodType = 16;
    public static final int CONSTANT_InvokeDynamic = 18;

    public static class functions {
        public static Function2<InputStream, OutputStream, ConstantPoolMapper> process(final ConstantPoolMapper poolMapper) {
            return new Function2<InputStream, OutputStream, ConstantPoolMapper>() {
                @Override
                public ConstantPoolMapper call(InputStream inputStream, OutputStream outputStream) throws Exception {
                    return poolMapper.process(inputStream, outputStream);
                }
            };
        }
    }
}
