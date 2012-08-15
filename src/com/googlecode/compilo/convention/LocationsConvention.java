package com.googlecode.compilo.convention;

import com.googlecode.compilo.Locations;

import java.io.File;
import java.util.Properties;

import static com.googlecode.totallylazy.Files.directory;
import static com.googlecode.totallylazy.Files.file;
import static java.lang.String.format;

public abstract class LocationsConvention extends IdentifiersConvention implements Locations {
    protected final File root;

    protected LocationsConvention(File root, Properties properties) {
        super(properties);
        this.root = root;
    }

    public File rootDir() { return root; }
    public File artifactsDir() { return directory(rootDir(), "build/artifacts"); }
    public File srcDir() { return directory(rootDir(), "src"); }
    public File testDir() { return directory(rootDir(), "test"); }
    public File libDir() { return directory(rootDir(), "lib"); }

    public File mainJar() { return file(artifactsDir(), format("%s.jar", versionedArtifact())); }
    public File sourcesJar() { return file(artifactsDir(), format("%s-sources.jar", versionedArtifact())); }
    public File testJar() { return file(artifactsDir(), format("%s-tests.jar", versionedArtifact())); }
    public File testSourcesJar() { return file(artifactsDir(), format("%s-test-sources.jar", versionedArtifact())); }
}
