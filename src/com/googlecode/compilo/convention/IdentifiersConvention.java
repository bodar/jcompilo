package com.googlecode.compilo.convention;

import com.googlecode.compilo.Environment;
import com.googlecode.compilo.Identifiers;

import static java.lang.String.format;

public abstract class IdentifiersConvention implements Identifiers {
    protected final Environment env;

    protected IdentifiersConvention(Environment env) {
        this.env = env;
    }

    @Override
    public String version() {
        return env.properties().getProperty("build.number", "dev.build");
    }

    @Override
    public String versionedArtifact() {
        return format("%s-%s", artifact(), version());
    }

    @Override
    public String artifactUri() {
        return String.format("mvn:%s:%s:jar:%s", group(), artifact(), version());
    }

    @Override
    public String releasePath() {
        return String.format("%s/%s/%s/", group().replace('.', '/'), artifact(), version());
    }
}
