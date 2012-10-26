package com.googlecode.compilo;

import com.googlecode.compilo.convention.AutoBuild;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Function2;
import com.googlecode.totallylazy.Maps;
import com.googlecode.totallylazy.Methods;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Runnables;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sources;
import com.googlecode.totallylazy.Strings;
import com.googlecode.totallylazy.predicates.LogicalPredicate;
import com.googlecode.yadic.SimpleContainer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import static com.googlecode.compilo.CompileProcessor.compile;
import static com.googlecode.compilo.Compiler.iterableSource;
import static com.googlecode.compilo.Environment.constructors.environment;
import static com.googlecode.compilo.Resource.functions.bytes;
import static com.googlecode.shavenmaven.Dependencies.load;
import static com.googlecode.totallylazy.Arrays.empty;
import static com.googlecode.totallylazy.Files.directory;
import static com.googlecode.totallylazy.Files.files;
import static com.googlecode.totallylazy.Files.hasSuffix;
import static com.googlecode.totallylazy.Files.name;
import static com.googlecode.totallylazy.Files.recursiveFiles;
import static com.googlecode.totallylazy.Files.relativePath;
import static com.googlecode.totallylazy.Lists.list;
import static com.googlecode.totallylazy.Methods.genericParameterTypes;
import static com.googlecode.totallylazy.Methods.methodName;
import static com.googlecode.totallylazy.Methods.modifier;
import static com.googlecode.totallylazy.Methods.returnType;
import static com.googlecode.totallylazy.Predicates.and;
import static com.googlecode.totallylazy.Predicates.classAssignableTo;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.not;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Runnables.printLine;
import static com.googlecode.totallylazy.Sequences.one;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Strings.endsWith;
import static com.googlecode.totallylazy.Strings.toLowerCase;
import static com.googlecode.yadic.generics.Types.matches;
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
            call(targets, createBuildClass(buildClass));

            report("SUCCESSFUL", start);
            return 0;
        } catch (Exception e) {
            report(format("FAILED: %s%n", e.getMessage()), start);
            return -1;
        }

    }

    private void printTargets(Class<?> buildClass) {
        env.out().printf("targets:%n");
        env.out().prefix("            ");
        sequence(buildClass.getMethods()).filter(targets()).map(methodName()).each(printLine(env.out(), "%s"));
    }

    private void call(List<String> targets, Build build) {
        sequence(targets.isEmpty() ? one("build") : targets).fold(build, new Function2<Build, String, Build>() {
            @Override
            public Build call(Build build, String target) throws Exception {
                Option<Method> method = sequence(build.getClass().getMethods()).
                        find(targets().and(where(methodName(), is(target.toLowerCase()))));
                if(method.isEmpty()) return build;
                return Methods.invoke(method.get(), build);
            }
        });
    }

    private LogicalPredicate<Method> targets() {
        return and(
                modifier(PUBLIC), not(modifier(STATIC)),
                where(returnType(), classAssignableTo(Build.class)),
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
        dependencies.mapConcurrently(new Function1<File, Boolean>() {
            @Override
            public Boolean call(File file) throws Exception {
                return load(file, env.out()).update(directory(libDir, file.getName().replace(".dependencies", "")));
            }
        }).realise();
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
        return buildFile.map(new Function1<File, Class<?>>() {
            @Override
            public Class<?> call(File buildFile) throws Exception {
                String name = relativePath(env.workingDirectory(), buildFile);

                Sequence<File> libs = libs();
                final MemoryStore compiledBuild = MemoryStore.memoryStore();
                compile(env, libs,
                        fileSource(buildFile, name),
                        compiledBuild);

                ClassLoader loader = new ByteClassLoader(Maps.mapValues(compiledBuild.data(), bytes()), FileUrls.urls(libs));
                return loader.loadClass(className(name));

            }
        }).getOrElse(AutoBuild.class);

    }


    private Sequence<File> libs() {
        return recursiveFiles(libDir).filter(hasSuffix("jar")).realise().add(compiloJar());
    }

    private File compiloJar() {
        try {
            Class<?> aClass = getClass();
            return jarFile(aClass);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Can't find compilo.jar");
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
        return iterableSource(one(new Sources.Source(name, new Date(buildFile.lastModified()), new FileInputStream(buildFile))));
    }

    private static String className(String build) {
        return build.replace(".java", "").replace('.', '/');
    }
}