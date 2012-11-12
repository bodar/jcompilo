package com.googlecode.jcompilo;

import javax.tools.ForwardingJavaFileObject;
import javax.tools.JavaFileObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import static com.googlecode.jcompilo.MoveToTL.classFilename;

public class OutputsObject extends ForwardingJavaFileObject<JavaFileObject> {
    private final String filename;
    private final Outputs outputs;
    private final Date modified;

    public OutputsObject(String className, JavaFileObject output, Date modified, Outputs outputs) {
        super(output);
        this.modified = modified;
        this.filename = classFilename(className);
        this.outputs = outputs;
    }

    @Override
    public OutputStream openOutputStream() throws IOException {
        return new ByteArrayOutputStream(){
            @Override
            public void close() throws IOException {
                outputs.put(Resource.constructors.resource(filename, modified, toByteArray()));
            }
        };
    }


}