package com.googlecode.jcompilo.asm;

import com.googlecode.totallylazy.Sequence;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;

public interface AsmMethodHandler {
    Sequence<ClassNode> process(ClassNode classNode, MethodNode method);
}
