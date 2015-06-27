package com.googlecode.jcompilo.tool;

import com.googlecode.jcompilo.MoveToTL;
import com.googlecode.jcompilo.Resource;
import com.googlecode.jcompilo.ResourceHandler;
import com.googlecode.totallylazy.Files;

import javax.tools.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import static com.googlecode.jcompilo.Resource.constructors.resource;
import static com.googlecode.jcompilo.ResourceHandler.functions.matchAndHandle;
import static com.googlecode.totallylazy.Sequences.head;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.http.Uri.uri;
import static javax.tools.JavaFileObject.Kind.CLASS;
import static javax.tools.StandardLocation.CLASS_OUTPUT;

public class PostProcessedFile extends ForwardingJavaFileObject<JavaFileObject> {
    private final StandardJavaFileManager fileManager;
    private final Iterable<ResourceHandler> resourceHandlers;

    public PostProcessedFile(StandardJavaFileManager fileManager, Iterable<ResourceHandler> resourceHandlers, JavaFileObject fileForOutput) throws IOException {
        super(fileForOutput);
        this.fileManager = fileManager;
        this.resourceHandlers = resourceHandlers;
    }

    @Override
    public OutputStream openOutputStream() throws IOException {
        return new ByteArrayOutputStream() {
            @Override
            public void close() throws IOException {
                Resource original = resource(uri(fileObject.toUri()), getName(), new Date(fileObject.getLastModified()), toByteArray());

                for (Resource generated : sequence(resourceHandlers).flatMap(matchAndHandle(original))) {
                    try (OutputStream outputStream = fileManager.getJavaFileForOutput(CLASS_OUTPUT, MoveToTL.classNameForByteCode(generated.name()), CLASS, fileObject).openOutputStream()) {
                        outputStream.write(generated.bytes());
                    }
                }
            }
        };
    }

    @Override
    public String getName() {
        return relativeName(super.getName());
    }

    private String relativeName(String name) {
        File root = head(fileManager.getLocation(CLASS_OUTPUT));
        return Files.relativePath(root, new File(name));
    }
}
