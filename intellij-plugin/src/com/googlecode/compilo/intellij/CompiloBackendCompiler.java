package com.googlecode.compilo.intellij;

import com.googlecode.compilo.CompileOption;
import com.googlecode.compilo.Inputs;
import com.googlecode.compilo.ModifiedPredicate;
import com.googlecode.compilo.Outputs;
import com.googlecode.compilo.Resource;
import com.googlecode.totallylazy.FileDestination;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.Sets;
import com.googlecode.totallylazy.predicates.LogicalPredicate;
import com.intellij.compiler.OutputParser;
import com.intellij.compiler.impl.CompilerUtil;
import com.intellij.compiler.impl.javaCompiler.BackendCompiler;
import com.intellij.compiler.impl.javaCompiler.ModuleChunk;
import com.intellij.compiler.impl.javaCompiler.api.CompilerAPIConfiguration;
import com.intellij.compiler.impl.javaCompiler.javac.JavacConfigurable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileScope;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.googlecode.compilo.MemoryStore.memoryStore;
import static com.googlecode.compilo.Resource.constructors.resource;
import static com.googlecode.totallylazy.Files.relativePath;
import static com.googlecode.totallylazy.Predicates.not;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Strings.startsWith;

public class CompiloBackendCompiler implements BackendCompiler {
    private static final Set<FileType> JAVA = Sets.<FileType>set(StdFileTypes.JAVA);

    private final Project myProject;
    private final DiagnosticCollector<JavaFileObject> diagnosticListener = new DiagnosticCollector<JavaFileObject>();

    public CompiloBackendCompiler(Project project) {
        myProject = project;
    }

    @NotNull
    public String getId() {
        return "compilo";
    }

    @NotNull
    public String getPresentableName() {
        return "Compilo";
    }

    @NotNull
    public Configurable createConfigurable() {
        return new JavacConfigurable(CompilerAPIConfiguration.getSettings(myProject, CompilerAPIConfiguration.class));
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
        return new FakeProcess(inputsFor(moduleChunk, outputPath), outputs(outputPath), dependencies(moduleChunk), compileOptions(moduleChunk), new CompilerDiagnostics(compileContext));
    }

    public void compileFinished() {}

    public static Outputs outputs(String outputPath) {
        return Outputs.constructors.output(FileDestination.fileDestination(new File(outputPath)));
    }

    public static Sequence<CompileOption> compileOptions(final ModuleChunk moduleChunk) {
        return ApplicationManager.getApplication().runReadAction(new Computable<Sequence<CompileOption>>() {
            public Sequence<CompileOption> compute() {
                List<String> commandLine = new ArrayList<String>();
                CompilerUtil.addSourceCommandLineSwitch(moduleChunk.getJdk(), moduleChunk.getLanguageLevel(), commandLine);
                return Sequences.sequence(commandLine).map(compileOption);
            }
        });
    }

    public static final Function1<String, CompileOption> compileOption = new Function1<String, CompileOption>() {
        @Override
        public CompileOption call(String s) throws Exception {
            return CompileOption.compileOption(s);
        }
    };

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