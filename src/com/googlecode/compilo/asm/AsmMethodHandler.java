package com.googlecode.compilo.asm;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public interface AsmMethodHandler {
    void process(ClassNode classNode, MethodNode method);
}
