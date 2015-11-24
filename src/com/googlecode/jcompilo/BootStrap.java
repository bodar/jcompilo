package com.googlecode.jcompilo;

import com.googlecode.jcompilo.convention.AutoBuild;
import com.googlecode.totallylazy.*;
import com.googlecode.totallylazy.io.Sources;
import com.googlecode.totallylazy.predicates.LogicalPredicate;
import com.googlecode.totallylazy.reflection.Methods;
import com.googlecode.yadic.SimpleContainer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import static com.googlecode.jcompilo.CompileProcessor.compile;
import static com.googlecode.jcompilo.Compiler.iterableSource;
import static com.googlecode.jcompilo.Environment.constructors.environment;
import static com.googlecode.jcompilo.Resource.functions.bytes;
import static com.googlecode.shavenmaven.Dependencies.load;
import static com.googlecode.totallylazy.Arrays.empty;
import static com.googlecode.totallylazy.Files.directory;
import static com.googlecode.totallylazy.Files.files;
import static com.googlecode.totallylazy.Files.hasSuffix;
import static com.googlecode.totallylazy.Files.name;
import static com.googlecode.totallylazy.Files.recursiveFiles;
import static com.googlecode.totallylazy.Files.relativePath;
import static com.googlecode.totallylazy.Lists.list;
import static com.googlecode.totallylazy.functions.Functions.andPair;
import static com.googlecode.totallylazy.reflection.Methods.genericParameterTypes;
import static com.googlecode.totallylazy.reflection.Methods.methodName;
import static com.googlecode.totallylazy.reflection.Methods.modifier;
import static com.googlecode.totallylazy.reflection.Methods.returnType;
import static com.googlecode.totallylazy.predicates.Predicates.and;
import static com.googlecode.totallylazy.predicates.Predicates.classAssignableTo;
import static com.googlecode.totallylazy.predicates.Predicates.is;
import static com.googlecode.totallylazy.predicates.Predicates.not;
import static com.googlecode.totallylazy.predicates.Predicates.where;
import static com.googlecode.totallylazy.Runnables.printLine;
import static com.googlecode.totallylazy.Sequences.one;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Strings.endsWith;
import static java.lang.String.format;
import static java.lang.System.nanoTime;
import static java.lang.reflect.Modifier.PUBLIC;
import static java.lang.reflect.Modifier.STATIC;

public class BootStrap {
    private final Environment env;
    private final File libDir;

    public BootStrap(Environment env) {
        this.env = env;
        libDir = directory(env.workingDirectory(), "lib");
    }

    public static void main(String[] args) throws Exception {
        System.exit(new BootStrap(environment()).build(list(args)));
    }

    public int build(List<String> targets) {
        long start = nanoTime();
        try {
            Option<File> buildFile = buildFile();
            env.out().printf("build: %s%n", buildFile.isEmpty() ? AutoBuild.class : buildFile.get());
            Class<?> buildClass = findBuildClass(buildFile);
            if(sequence(targets).contains("-p")) {
                printTargets(buildClass);
                return 0;
            }
            boolean success = call(targets, createBuildClass(buildClass));
            if(!success) {
                report("FAILED", start);
                return -1;
            }

            report("SUCCESSFUL", start);
            return 0;
        } catch (Exception e) {
            report(format("FAILED: %s%n", Exceptions.asString(e)), start);
            return -1;
        }

    }

    private void printTargets(Class<?> buildClass) {
        env.out().printf("targets:%n");
        env.out().prefix("            ");
        sequence(buildClass.getMethods()).filter(targets()).map(methodName()).each(printLine(env.out(), "%s"));
    }

    private boolean call(List<String> targets, final Build build) {
        return sequence(targets.isEmpty() ? one("build") : targets).map(target -> {
            Option<Method> method = sequence(build.getClass().getMethods()).
                    find(targets().and(where(methodName(), is(target.toLowerCase()))));
            if(method.isEmpty()) return false;
            return Methods.invoke(method.get(), build);
        }).reduceRight(andPair());
    }

    private LogicalPredicate<Method> targets() {
        return and(
                modifier(PUBLIC), not(modifier(STATIC)),
                where(returnType(), classAssignableTo(boolean.class)),
                where(genericParameterTypes(), empty()));
    }

    private void report(String message, long start) {
        env.out().println();
        env.out().println("BUILD " + message);
        env.out().printf("Total time: %s seconds%n", calculateSeconds(start));
    }

    private long calculateSeconds(long start) {
        return (nanoTime() - start) / 1000000000;
    }

    private void update() {
        Sequence<File> dependencies = files(directory(env.workingDirectory(), "build")).filter(hasSuffix("dependencies"));
        if (dependencies.isEmpty()) return;
        env.out().printf("update:%n");
        env.out().prefix("      [lib] ");
        dependencies.mapConcurrently(file ->
                load(file, env.out()).update(directory(libDir, file.getName().replace(".dependencies", "")))).realise();
        env.out().clearPrefix();
    }

    private Build createBuildClass(Class<?> aClass) throws Exception {
        return (Build) new SimpleContainer().
                addInstance(Environment.class, env).
                addInstance(File.class, env.workingDirectory()).
                addInstance(Properties.class, env.properties()).
                addInstance(PrintStream.class, env.out()).
                create(aClass);
    }

    public Class<?> findBuildClass(final Option<File> buildFile) throws Exception {
        update();
        return buildFile.<Class<?>>map(buildFile1 -> {
            String name = relativePath(env.workingDirectory(), buildFile1);

            Sequence<File> libs = libs();
            final MemoryStore compiledBuild = MemoryStore.memoryStore();
            compile(env, libs,
                    fileSource(buildFile1, name),
                    compiledBuild);
            URLClassLoader libraries = new URLClassLoader(FileUrls.urls(libs).toArray(URL.class));
            ClassLoader loader = new ByteClassLoader(Maps.mapValues(compiledBuild.data(), bytes()), libraries);
            return loader.loadClass(className(name));

        }).getOrElse(AutoBuild.class);
    }

    private Sequence<File> libs() {
        return recursiveFiles(libDir).filter(hasSuffix("jar")).realise().append(jcompiloJar());
    }

    private File jcompiloJar() {
        try {
            Class<?> aClass = getClass();
            return jarFile(aClass);
        } catch (URISyntaxException e) {
            throw new JCompiloException("Can't find jcompilo.jar");
        }
    }

    public static File jarFile(Class<?> aClass) throws URISyntaxException {
        return new File(jarUrl(aClass));
    }

    public static URI jarUrl(Class<?> aClass) throws URISyntaxException {
        return aClass.getProtectionDomain().getCodeSource().getLocation().toURI();
    }

    public Option<File> buildFile() {
        return files(env.workingDirectory()).
                find(where(name(), endsWith("uild.java")));
    }

    private static Sources fileSource(File buildFile, String name) throws FileNotFoundException {
        return iterableSource(one(new Sources.Source(name, new Date(buildFile.lastModified()), new FileInputStream(buildFile), false)));
    }

    private static String className(String build) {
        return build.replace(".java", "").replace('.', '/');
    }
}