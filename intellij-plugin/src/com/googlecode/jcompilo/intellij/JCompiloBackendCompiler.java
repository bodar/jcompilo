package com.googlecode.jcompilo.intellij;

import com.googlecode.jcompilo.CompileOption;
import com.googlecode.jcompilo.CompileProcessor;
import com.googlecode.jcompilo.Inputs;
import com.googlecode.jcompilo.ModifiedPredicate;
import com.googlecode.jcompilo.Outputs;
import com.googlecode.jcompilo.Resource;
import com.googlecode.totallylazy.FileDestination;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.Sets;
import com.googlecode.totallylazy.predicates.LogicalPredicate;
import com.intellij.compiler.OutputParser;
import com.intellij.compiler.impl.javaCompiler.BackendCompiler;
import com.intellij.compiler.impl.javaCompiler.ModuleChunk;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileScope;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Set;

import static com.googlecode.jcompilo.MemoryStore.memoryStore;
import static com.googlecode.totallylazy.Files.relativePath;
import static com.googlecode.totallylazy.Predicates.not;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Strings.startsWith;

public class JCompiloBackendCompiler implements BackendCompiler {
    private static final Set<FileType> JAVA = Sets.<FileType>set(StdFileTypes.JAVA);

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

    @SuppressWarnings("deprecation")
    private static int version(final ModuleChunk moduleChunk) {
        return moduleChunk.getLanguageLevel().getIndex();
    }

    public static Iterable<File> dependencies(ModuleChunk moduleChunk) {
        return Sequences.sequence(moduleChunk.getCompilationClasspathFiles()).map(new Function1<VirtualFile, File>() {
            @Override
            public File call(VirtualFile virtualFile) throws Exception {
                return file(virtualFile);
            }
        });
    }

    public static Inputs inputsFor(ModuleChunk moduleChunk, String outputPath) throws IOException {
        File root = root(moduleChunk);
        Sequence<Resource> inputs = sequence(moduleChunk.getFilesToCompile()).
                filter(not(modifiedMatches(root, new File(outputPath)))).
                map(asResource(root));

        return memoryStore(inputs);
    }

    private static LogicalPredicate<VirtualFile> modifiedMatches(File sourceDirectory, File destinationDirectory) {
        final LogicalPredicate<File> predicate = ModifiedPredicate.modifiedMatches(sourceDirectory, destinationDirectory);
        return new LogicalPredicate<VirtualFile>() {
            @Override
            public boolean matches(VirtualFile other) {
                return predicate.matches(file(other));
            }
        };
    }

    private static Function1<VirtualFile, Resource> asResource(final File root) {
        return new Function1<VirtualFile, Resource>() {
            @Override
            public Resource call(VirtualFile virtualFile) throws Exception {
                final File source = file(virtualFile);
                String relative = relativePath(root, source);
                Date modified = modified(source);
                byte[] bytes = virtualFile.contentsToByteArray();
                return new ResourceWithSource(relative, modified, bytes, source);
            }
        };
    }

    private static File root(ModuleChunk moduleChunk) {
        return file(sequence(moduleChunk.getSourceRoots()).
                find(where(getPath(), startsWith(moduleChunk.getProject().getBasePath()))).
                get());
    }

    private static Function1<VirtualFile, String> getPath() {
        return new Function1<VirtualFile, String>() {
            @Override
            public String call(VirtualFile virtualFile) throws Exception {
                return virtualFile.getPath();
            }
        };
    }

    private static File file(VirtualFile virtualFile) {
        return new File(virtualFile.getPresentableUrl());
    }

    private static Date modified(final File file) {
        return new Date(file.lastModified());
    }

    public static class ResourceWithSource extends Resource.AResource {
        public final File source;

        public ResourceWithSource(String relative, Date modified, byte[] bytes, File source) {
            super(relative, modified, bytes);
            this.source = source;
        }
    }

}