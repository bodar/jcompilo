package com.googlecode.compilo;

public enum CompileOption {
    None(""),
    Debug("-g"),
    UncheckedWarnings("-Xlint:unchecked"),
    WarningAsErrors("-Werror"),
    Target("-target"),
    Source("-source");

    private final String value;

    CompileOption(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
