package com.googlecode.jcompilo.intellij;

import com.googlecode.jcompilo.Outputs;
import com.googlecode.jcompilo.Resource;
import com.googlecode.totallylazy.*;
import com.intellij.compiler.CompilerConfiguration;
import com.intellij.compiler.CompilerConfigurationImpl;
import com.intellij.compiler.CompilerWorkspaceConfiguration;
import com.intellij.compiler.OutputParser;
import com.intellij.compiler.impl.javaCompiler.BackendCompiler;
import com.intellij.openapi.compiler.CompileScope;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Date;
import java.util.Set;

import static com.intellij.openapi.project.ProjectUtil.guessProjectForFile;
import static com.intellij.openapi.roots.ProjectRootManager.getInstance;

public class JCompiloBackendCompiler implements BackendCompiler {
    private static final Set<FileType> JAVA = Sets.<FileType>set(StdFileTypes.JAVA);
    public static final String ID = "jcompilo";
    public static final String NAME = "JCompilo";

    public JCompiloBackendCompiler(Project project) {
        String default_compiler = ((CompilerConfigurationImpl) CompilerConfiguration.getInstance(project)).DEFAULT_COMPILER;
        CompilerWorkspaceConfiguration instance = CompilerWorkspaceConfiguration.getInstance(project);
//        instance.USE_OUT_OF_PROCESS_BUILD = !default_compiler.equals(getId());
    }

    @NotNull
    public String getId() {
        return ID;
    }

    @NotNull
    public String getPresentableName() {
        return NAME;
    }

    @NotNull
    public Configurable createConfigurable() {
        return new JCompiloConfigurable(this);
    }

    @NotNull
    public Set<FileType> getCompilableFileTypes() {
        return JAVA;
    }

    public OutputParser createErrorParser(@NotNull String s, Process process) {
        return null;
    }

    public OutputParser createOutputParser(@NotNull String s) {
        return null;
    }


    public boolean checkCompiler(CompileScope compileScope) {
        return true;
    }

//    @NotNull
//    public Process launchProcess(@NotNull ModuleChunk moduleChunk, @NotNull String outputPath, @NotNull CompileContext compileContext) throws IOException {
//        return new FakeProcess(inputsFor(moduleChunk, outputPath), outputs(outputPath), dependencies(moduleChunk), compileOptions(moduleChunk), new CompilerDiagnostics(compileContext), compileContext);
//    }

    public void compileFinished() {
    }

    public static Outputs outputs(String outputPath) {
        return Outputs.constructors.output(FileDestination.fileDestination(new File(outputPath)));
    }

//    public static Sequence<CompileOption> compileOptions(final ModuleChunk moduleChunk) {
//        return ApplicationManager.getApplication().runReadAction(new Computable<Sequence<CompileOption>>() {
//            public Sequence<CompileOption> compute() {
//                int version = 7;
//                String bootClasspath = ProjectPaths.moduleChunk.getCompilationBootClasspath();
//                return CompileProcessor.DEFAULT_OPTIONS.cons(CompileOption.Sources(version)).cons(CompileOption.BootClassPath(bootClasspath));
//            }
//        });
//    }
//
//    public static Iterable<File> dependencies(ModuleChunk moduleChunk) {
//        return ProjectPaths.getCompilationClasspath(moduleChunk, true);
//    }
//
//    public static Inputs inputsFor(ModuleChunk moduleChunk, String outputPath) throws IOException {
//        return memoryStore(sequence(moduleChunk.getFilesToCompile()).
//                map(asResource()).
//                filter(not(ResourceModified.modifiedMatches(new File(outputPath)))));
//    }

    public static String relativePathV(VirtualFile file) {
        return Files.relativePath(file(root(file)), file(file));
    }

    private static VirtualFile root(final VirtualFile file) {
        return getInstance(guessProjectForFile(file)).getFileIndex().getSourceRootForFile(file);
    }

    private static Function1<VirtualFile, Resource> asResource() {
        return new Function1<VirtualFile, Resource>() {
            @Override
            public Resource call(VirtualFile virtualFile) throws Exception {
                final File source = file(virtualFile);
                String relative = relativePathV(virtualFile);
                Date modified = modified(source);
                byte[] bytes = virtualFile.contentsToByteArray();
                return Resource.constructors.resource(Uri.uri(source), relative, modified, bytes);
            }
        };
    }

    public static File file(VirtualFile virtualFile) {
        return new File(virtualFile.getPresentableUrl());
    }

    private static Date modified(final File file) {
        return new Date(file.lastModified());
    }


}