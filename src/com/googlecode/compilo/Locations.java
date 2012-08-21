package com.googlecode.compilo;

import java.io.File;

public interface Locations {
    File rootDir();
    File artifactsDir();
    File libDir();
    File srcDir();
    File testDir();

    File mainJar();
    File sourcesJar();
    File testJar();
    File testSourcesJar();

    File buildDir();
}
