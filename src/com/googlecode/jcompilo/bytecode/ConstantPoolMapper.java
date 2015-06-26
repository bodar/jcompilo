package com.googlecode.jcompilo.bytecode;

import com.googlecode.totallylazy.Callers;
import com.googlecode.totallylazy.Destination;
import com.googlecode.totallylazy.functions.CurriedFunction2;
import com.googlecode.totallylazy.Sources;
import com.googlecode.totallylazy.Streams;
import com.googlecode.totallylazy.functions.UnaryFunction;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.googlecode.totallylazy.Closeables.using;

public class ConstantPoolMapper {
    private final UnaryFunction<String> mapper;

    public ConstantPoolMapper(UnaryFunction<String> mapper) {
        this.mapper = mapper;
    }

    public ConstantPoolMapper process(Sources sources, Destination destination) {
        return using(sources, destination, (sources1, destination1) -> {
            for (Sources.Source source : sources1.sources()) {
                OutputStream outputStream = destination1.destination(mapper.call(source.name), source.modified);
                if (source.name.endsWith(".class"))
                    using(source.input, outputStream, functions.process(ConstantPoolMapper.this));
                else Streams.copyAndClose(source.input, outputStream);
            }
            return ConstantPoolMapper.this;
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

            Constant constant = Constant.constant(tag);
            switch (constant) {
                case Utf8:
                    out.writeUTF(Callers.call(mapper, in.readUTF()));
                    break;
                case Double:
                case Long:
                    i++; // "In retrospect, making 8-byte constants take two constant pool entries was a poor choice."
                    // See http://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.4.5
                default:
                    copy(in, out, constant.size);
                    break;
            }
        }
        Streams.copyAndClose(in, out);
        return this;
    }

    private final byte[] buffer = new byte[8];

    private void copy(DataInputStream in, DataOutputStream out, int amount) throws IOException {
        in.readFully(buffer, 0, amount);
        out.write(buffer, 0, amount);
    }

    public static class functions {
        public static CurriedFunction2<InputStream, OutputStream, ConstantPoolMapper> process(final ConstantPoolMapper poolMapper) {
            return poolMapper::process;
        }
    }
}