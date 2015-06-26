package com.googlecode.jcompilo.convention;

import com.googlecode.jcompilo.Environment;
import com.googlecode.jcompilo.JCompiloException;
import com.googlecode.totallylazy.Files;
import com.googlecode.totallylazy.functions.Lazy;
import com.googlecode.totallylazy.Sequence;

import java.io.File;

import static com.googlecode.totallylazy.Files.*;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.not;
import static com.googlecode.totallylazy.Predicates.where;

public class AutoBuild extends BuildConvention {
    private final Lazy<File> rootPackage = Lazy.lazy( () -> rootPackage(srcDir()));
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
        if(children.isEmpty()) throw new JCompiloException("No source files found");
        if(!children.filter(isFile()).isEmpty()) return root;
        Sequence<File> subPackages = children.filter(isDirectory()).reject(where(name(), is("META-INF")));
        if(subPackages.size() > 1) throw new JCompiloException("Unable to auto detect root package");
        return rootPackage(subPackages.head());
    }
}
