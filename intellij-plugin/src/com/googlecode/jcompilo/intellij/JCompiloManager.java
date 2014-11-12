package com.googlecode.jcompilo.intellij;

import com.intellij.compiler.CompilerManagerImpl;
import com.intellij.compiler.impl.CompileContextImpl;
import com.intellij.compiler.impl.CompileDriver;
import com.intellij.compiler.progress.CompilerTask;
import com.intellij.openapi.compiler.CompileScope;
import com.intellij.openapi.compiler.CompileStatusNotification;
import com.intellij.openapi.compiler.CompilerFilter;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.LanguageLevelProjectExtension;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBus;

public class JCompiloManager extends CompilerManagerImpl {
    private final Project project;
    private final MessageBus messageBus;

    public JCompiloManager(Project project, MessageBus messageBus) {
        super(project, messageBus);
        this.project = project;
        this.messageBus = messageBus;
    }

    @Override
    public void compile(VirtualFile[] files, CompileStatusNotification callback) {
        super.compile(files, callback);
    }

    @Override
    public void compile(Module module, CompileStatusNotification callback) {
        super.compile(module, callback);
    }

    @Override
    public void compile(CompileScope scope, CompileStatusNotification callback) {
        super.compile(scope, callback);
    }

    @Override
    public void make(CompileStatusNotification callback) {
        super.make(callback);
    }

    @Override
    public void make(Module module, CompileStatusNotification callback) {
        super.make(module, callback);
    }

    @Override
    public void make(Project project, Module[] modules, CompileStatusNotification callback) {
        super.make(project, modules, callback);
    }

    @Override
    public void make(CompileScope scope, CompileStatusNotification callback) {
        super.make(scope, callback);
    }

    @Override
    public void make(CompileScope scope, CompilerFilter filter, CompileStatusNotification callback) {
        super.make(scope, filter, callback);
    }

    @Override
    public void rebuild(CompileStatusNotification callback) {
        super.rebuild(callback);
    }
}
