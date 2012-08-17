package com.googlecode.compilo.convention;

import com.googlecode.totallylazy.Files;
import com.googlecode.totallylazy.Lazy;
import com.googlecode.totallylazy.Sequence;

import java.io.File;
import java.io.PrintStream;
import java.util.Properties;

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

    public AutoBuild(File root, Properties properties, PrintStream out) {
        super(root, properties, out);
    }

    @Override
    public String group() {
        return Files.relativePath(srcDir(), rootPackage.value()).replace('/', '.').replaceFirst(".$", "");
    }

    private File rootPackage(File root) {
        Sequence<File> children = files(root);
        if(children.isEmpty()) throw new IllegalStateException("No source files found");
        if(!children.filter(isFile()).isEmpty()) return root;
        Sequence<File> subPackages = children.filter(isDirectory());
        if(subPackages.size() > 1) throw new IllegalStateException("Unable to auto detect root package");
        return rootPackage(subPackages.head());
    }

    @Override
    public String artifact() {
        return rootPackage.value().getName();
    }
}
