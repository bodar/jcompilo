package com.googlecode.compilo.convention;

import com.googlecode.compilo.Identifiers;

import java.util.Properties;

import static java.lang.String.format;

public abstract class IdentifiersConvention implements Identifiers {
    protected final Properties properties;

    protected IdentifiersConvention(Properties properties) {
        this.properties = properties;
    }

    @Override
    public String version() {
        return properties.getProperty("build.number", "dev.build");
    }

    @Override
    public String versionedArtifact() {
        return format("%s-%s", artifact(), version());
    }
}
