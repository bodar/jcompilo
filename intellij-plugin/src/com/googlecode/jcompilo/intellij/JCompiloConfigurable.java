package com.googlecode.jcompilo.intellij;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;

import javax.swing.*;

class JCompiloConfigurable implements Configurable {
    @Nls
    @Override
    public String getDisplayName() {
        return JCompiloBackendCompiler.NAME;
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
