package com.isfett;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class PHPAnalyzerSettings implements SearchableConfigurable{
    Project project;
    PHPAnalyzerSettingsPanel panel;
    PHPAnalyzerSettingsProvider settingsProvider;

    public PHPAnalyzerSettings(Project project) {
        this.project = project;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        panel = new PHPAnalyzerSettingsPanel(this.project);
        return panel.createPanel();
    }

    @Override
    public void reset() {
        panel.reset();
    }

    @Override
    public boolean isModified() {
        return panel.isModified();
    }

    @Override
    public void apply() throws ConfigurationException {
        panel.apply();
    }

    @Override
    public void disposeUIResources() {
        System.out.println("disposeUiResources?");
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Isfett PHP-Analyzer";
    }

    @NotNull
    @Override
    public String getId() {
        return "Isfett PHP-Analyzer";
    }
}
