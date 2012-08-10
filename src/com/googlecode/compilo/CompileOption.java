package com.googlecode.compilo;

public enum CompileOption {
    None(""),
    Debug("-g"),
    UncheckedWarnings("-Xlint:unchecked"),
    WarningAsErrors("-Werror"),
    Target6("-target 6"),
    Source6("-source 6");

    private final String value;

    CompileOption(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
