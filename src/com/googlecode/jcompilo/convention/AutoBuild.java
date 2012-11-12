package com.googlecode.jcompilo.convention;

import com.googlecode.jcompilo.Environment;
import com.googlecode.totallylazy.Files;
import com.googlecode.totallylazy.Lazy;
import com.googlecode.totallylazy.Sequence;

import java.io.File;

import static com.googlecode.totallylazy.Files.files;
import static com.googlecode.totallylazy.Files.isDirectory;
import static com.googlecode.totallylazy.Files.isFile;

public class AutoBuild extends BuildConvention {
    private final Lazy<File> rootPackage = new Lazy<File>() {
        @Override
        protected File get() throws Exception {
            return rootPackage(srcDir());
        }
    };
    public AutoBuild() {
    }

    public AutoBuild(Environment environment) {
        super(environment);
    }

    @Override
    public String group() {
        return Files.relativePath(srcDir(), rootPackage.value()).replace('/', '.').replaceFirst(".$", "");
    }

    @Override
    public String artifact() {
        return rootPackage.value().getName();
    }

    private File rootPackage(File root) {
        Sequence<File> children = files(root);
        if(children.isEmpty()) throw new IllegalStateException("No source files found");
        if(!children.filter(isFile()).isEmpty()) return root;
        Sequence<File> subPackages = children.filter(isDirectory());
        if(subPackages.size() > 1) throw new IllegalStateException("Unable to auto detect root package");
        return rootPackage(subPackages.head());
    }
}
