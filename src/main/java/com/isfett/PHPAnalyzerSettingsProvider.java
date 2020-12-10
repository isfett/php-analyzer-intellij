package com.isfett;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
        name = "PHPAnalyzerSettings",
        storages = {
                @Storage("isfett-phpanalyzer-plugin.xml")
        }
)
public class PHPAnalyzerSettingsProvider implements PersistentStateComponent<PHPAnalyzerSettingsProvider> {
    public String path;
    public String excludes;
    public String excludePaths;
    public String excludeFiles;
    public String suffixes = "php";

    public static PHPAnalyzerSettingsProvider getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, PHPAnalyzerSettingsProvider.class);
    }

    @Nullable
    @Override
    public PHPAnalyzerSettingsProvider getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull PHPAnalyzerSettingsProvider state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getExcludes() {
        return excludes;
    }

    public void setExcludes(String excludes) {
        this.excludes = excludes;
    }

    public String getExcludePaths() {
        return excludePaths;
    }

    public void setExcludePaths(String excludePaths) {
        this.excludePaths = excludePaths;
    }

    public String getExcludeFiles() {
        return excludeFiles;
    }

    public void setExcludeFiles(String excludeFiles) {
        this.excludeFiles = excludeFiles;
    }

    public String getSuffixes() {
        return suffixes;
    }

    public void setSuffixes(String suffixes) {
        this.suffixes = suffixes;
    }

    public String getBaseCommand(String commandName)
    {
        return path+" "+commandName+getSuffixesForCommand()+getExcludeFilesForCommand()+getExcludePathsForCommand()+" ";
    }

    public String getSuffixesForCommand()
    {
        return " --suffixes="+suffixes;
    }

    public String getExcludeFilesForCommand()
    {
        if (excludeFiles.length() > 0) {
            return " --exclude-files="+excludeFiles;
        }

        return "";
    }

    public String getExcludePathsForCommand()
    {
        if (excludePaths.length() > 0) {
            return " --exclude-paths="+excludePaths;
        }

        return "";
    }

    public String getExcludesForCommand()
    {
        if (excludePaths.length() > 0) {
            return " --excludes="+excludePaths;
        }

        return "";
    }
}
