package com.googlecode.jcompilo.tool;

import com.googlecode.jcompilo.ResourceHandler;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import java.io.File;
import java.io.IOException;

public class ForwardingStandardJavaFileManager<M extends StandardJavaFileManager> extends ForwardingJavaFileManager<M> implements StandardJavaFileManager {
    public ForwardingStandardJavaFileManager(M fileManager) {
        super(fileManager);
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
        return fileManager.getJavaFileForOutput(location, className, kind, sibling);
    }

    @Override
    public Iterable<? extends JavaFileObject> getJavaFileObjectsFromFiles(Iterable<? extends File> files) {
        return fileManager.getJavaFileObjectsFromFiles(files);
    }

    @Override
    public Iterable<? extends JavaFileObject> getJavaFileObjects(File... files) {
        return fileManager.getJavaFileObjects(files);
    }

    @Override
    public Iterable<? extends JavaFileObject> getJavaFileObjectsFromStrings(Iterable<String> names) {
        return fileManager.getJavaFileObjectsFromStrings(names);
    }

    @Override
    public Iterable<? extends JavaFileObject> getJavaFileObjects(String... names) {
        return fileManager.getJavaFileObjects(names);
    }

    @Override
    public void setLocation(Location location, Iterable<? extends File> path) throws IOException {
        fileManager.setLocation(location, path);
    }

    @Override
    public Iterable<? extends File> getLocation(Location location) {
        return fileManager.getLocation(location);
    }

    @Override
    public int isSupportedOption(String option) {
        return fileManager.isSupportedOption(option);
    }
}
