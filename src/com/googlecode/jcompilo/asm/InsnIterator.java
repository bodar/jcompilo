package com.googlecode.jcompilo.asm;

import com.googlecode.totallylazy.iterators.ReadOnlyIterator;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

import java.util.NoSuchElementException;

public class InsnIterator extends ReadOnlyIterator<AbstractInsnNode> {
    private final InsnList list;
    private int index = 0;

    public InsnIterator(final InsnList list) {
        this.list = list;
    }

    public final boolean hasNext() {
        return index < list.size();
    }

    public final AbstractInsnNode next() {
        if(hasNext()){
            return list.get(index++);
        }
        throw new NoSuchElementException();
    }
}
