package com.googlecode.jcompilo;

public interface Identifiers {
    String group();
    String artifact();
    String version();
    String versionedArtifact();

    String artifactUri();
    String releasePath();
}
