package com.googlecode.jcompilo.intellij;

import com.googlecode.jcompilo.CompileOption;
import com.googlecode.jcompilo.CompileProcessor;
import com.googlecode.jcompilo.Inputs;
import com.googlecode.jcompilo.Outputs;
import com.googlecode.jcompilo.Resource;
import com.googlecode.totallylazy.FileDestination;
import com.googlecode.totallylazy.Files;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sets;
import com.googlecode.totallylazy.Uri;
import com.intellij.compiler.CompilerWorkspaceConfiguration;
import com.intellij.compiler.OutputParser;
import com.intellij.compiler.impl.javaCompiler.BackendCompiler;
import com.intellij.compiler.impl.javaCompiler.ModuleChunk;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileScope;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Set;

import static com.googlecode.jcompilo.MemoryStore.memoryStore;
import static com.googlecode.totallylazy.Predicates.not;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.intellij.openapi.project.ProjectUtil.guessProjectForFile;
import static com.intellij.openapi.roots.ProjectRootManager.getInstance;

public class JCompiloBackendCompiler implements BackendCompiler {
    private static final Set<FileType> JAVA = Sets.<FileType>set(StdFileTypes.JAVA);

    public JCompiloBackendCompiler() {
        Project project = ProjectUtil.guessCurrentProject(null);
        CompilerWorkspaceConfiguration.getInstance(project).USE_OUT_OF_PROCESS_BUILD = true;
    }

    @NotNull
    public String getId() {
        return "jcompilo";
    }

    @NotNull
    public String getPresentableName() {
        return "JCompilo";
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

    @NotNull
    public Process launchProcess(@NotNull ModuleChunk moduleChunk, @NotNull String outputPath, @NotNull CompileContext compileContext) throws IOException {
        return new FakeProcess(inputsFor(moduleChunk, outputPath), outputs(outputPath), dependencies(moduleChunk), compileOptions(moduleChunk), new CompilerDiagnostics(compileContext), compileContext);
    }

    public void compileFinished() {
    }

    public static Outputs outputs(String outputPath) {
        return Outputs.constructors.output(FileDestination.fileDestination(new File(outputPath)));
    }

    public static Sequence<CompileOption> compileOptions(final ModuleChunk moduleChunk) {
        return ApplicationManager.getApplication().runReadAction(new Computable<Sequence<CompileOption>>() {
            public Sequence<CompileOption> compute() {
                int version = version(moduleChunk);
                String bootClasspath = moduleChunk.getCompilationBootClasspath();
                return CompileProcessor.DEFAULT_OPTIONS.cons(CompileOption.Sources(version)).cons(CompileOption.BootClassPath(bootClasspath));
            }
        });
    }

    private static int version(final ModuleChunk moduleChunk) {
        return moduleChunk.getLanguageLevel().ordinal() + 3;
    }

    public static Iterable<File> dependencies(ModuleChunk moduleChunk) {
        return sequence(moduleChunk.getCompilationClasspathFiles()).map(new Function1<VirtualFile, File>() {
            @Override
            public File call(VirtualFile virtualFile) throws Exception {
                return file(virtualFile);
            }
        });
    }

    public static Inputs inputsFor(ModuleChunk moduleChunk, String outputPath) throws IOException {
        return memoryStore(sequence(moduleChunk.getFilesToCompile()).
                map(asResource()).
                filter(not(ResourceModified.modifiedMatches(new File(outputPath)))));
    }

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