package com.googlecode.jcompilo.intellij;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;

import javax.swing.*;

class JCompiloConfigurable implements Configurable {
    private final JCompiloBackendCompiler backendCompiler;

    public JCompiloConfigurable(JCompiloBackendCompiler backendCompiler) {
        this.backendCompiler = backendCompiler;
    }

    @Nls
    @Override
    public String getDisplayName() {
        return backendCompiler.getPresentableName();
    }

    @Override
    public Icon getIcon() {
        return null;
    }

    @Override
    public String getHelpTopic() {
        return "";
    }

    @Override
    public JComponent createComponent() {
        return new JPanel();
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public void apply() throws ConfigurationException {
    }

    @Override
    public void reset() {
    }

    @Override
    public void disposeUIResources() {
    }
}
