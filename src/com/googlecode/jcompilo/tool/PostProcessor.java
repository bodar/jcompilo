package com.googlecode.jcompilo.tool;

import com.googlecode.jcompilo.ResourceHandler;
import com.googlecode.totallylazy.functions.Unary;

import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import java.io.File;
import java.io.IOException;

import static com.googlecode.totallylazy.Sequences.sequence;

public class PostProcessor extends ForwardingStandardJavaFileManager<StandardJavaFileManager> {
    private final Iterable<ResourceHandler> resourceHandlers;

    public PostProcessor(StandardJavaFileManager fileManager, Iterable<ResourceHandler> resourceHandlers) {
        super(fileManager);
        this.resourceHandlers = resourceHandlers;
    }

    @Override
    public FileObject getFileForOutput(Location location, String packageName, String relativeName, FileObject sibling) throws IOException {
        return super.getFileForOutput(location, packageName, relativeName, sibling);
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
        return postProcessFile.apply(fileManager.getJavaFileForOutput(location, className, kind, sibling));
    }

    @Override
    public Iterable<? extends JavaFileObject> getJavaFileObjectsFromFiles(Iterable<? extends File> files) {
        return sequence(fileManager.getJavaFileObjectsFromFiles(files)).map(postProcessFile);
    }

    @Override
    public Iterable<? extends JavaFileObject> getJavaFileObjects(File... files) {
        return sequence(fileManager.getJavaFileObjects(files)).map(postProcessFile);
    }

    @Override
    public Iterable<? extends JavaFileObject> getJavaFileObjectsFromStrings(Iterable<String> names) {
        return sequence(fileManager.getJavaFileObjectsFromStrings(names)).map(postProcessFile);
    }

    @Override
    public Iterable<? extends JavaFileObject> getJavaFileObjects(String... names) {
        return sequence(fileManager.getJavaFileObjects(names)).map(postProcessFile);
    }

    private Unary<JavaFileObject> postProcessFile = new Unary<JavaFileObject>() {
        @Override
        public JavaFileObject call(JavaFileObject raw) throws Exception {
            return new PostProcessedFile(fileManager, resourceHandlers, raw);
        }
    };

}
