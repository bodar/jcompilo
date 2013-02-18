package com.googlecode.jcompilo.asm;

import com.googlecode.totallylazy.Sequence;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public interface AsmMethodHandler {
    Sequence<ClassNode> process(ClassNode classNode, MethodNode method);
}
