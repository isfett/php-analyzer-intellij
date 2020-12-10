package com.isfett;


import com.intellij.ide.DataManager;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.options.ex.Settings;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.profile.codeInspection.ui.header.InspectionToolsConfigurable;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.labels.LinkLabel;
import com.intellij.ui.components.labels.LinkListener;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.lang.StringUtils;

public class PHPAnalyzerSettingsPanel {
    private Project project;
    private JPanel main;
    private JTextField path;
    private JButton validateBtn;
    private JButton pathChooser;
    private JLabel validationMessage;
    private LinkLabel linkInspections;
    private JTextField excludes;
    private JTextField excludePaths;
    private JTextField excludeFiles;
    private JTextField suffixes;
    private LinkLabel linkDocumentation;

    private boolean modified = false;

    public PHPAnalyzerSettingsPanel(@NotNull Project project) {
        this.project = project;
    }

    public JComponent createPanel() {
        FileChooserDescriptor fileChooserDescriptor = new FileChooserDescriptor(true, false, false, false, false, false) {
            @Override
            public boolean isFileSelectable(VirtualFile file) {
                return super.isFileSelectable(file) && (file.getPath().endsWith("bin/php-analyzer") || (file.getExtension() != null && file.getExtension().equals("phar")));
            }
        };
        pathChooser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println(e);
                FileChooser.chooseFile(fileChooserDescriptor, project, null, new Consumer<VirtualFile>() {
                    @Override
                    public void consume(VirtualFile virtualFile) {
                        path.setText(virtualFile.getPath());
                        modified = true;
                        validateBtn.setEnabled(true);
                    }
                });
            }
        });
        validateBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String command = path.getText()+" --version";
                File file = new File(path.getText());
                validationMessage.setForeground(JBColor.RED);
                if (!file.isFile() || !file.exists()) {
                    validationMessage.setText("File not found!");
                    return;
                }
                if(!file.canExecute()) {
                    validationMessage.setText("File not executable!");
                    return;
                }
                String output = executeCommand(command);
                validationMessage.setText(output);
                if (output.contains("chris@isfett.com")) {
                    validationMessage.setForeground(JBColor.GREEN);
                }
                /*JOptionPane.showMessageDialog(null, "'"+version+"'", "debug", JOptionPane.ERROR_MESSAGE);*/
            }
        });
        if (path.getText().equals("")) {
            validateBtn.setEnabled(false);
        }
        path.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                toggleValidateBtn();
            }
            public void removeUpdate(DocumentEvent e) {
                toggleValidateBtn();
            }
            public void insertUpdate(DocumentEvent e) {
                toggleValidateBtn();
            }

            public void toggleValidateBtn() {
                if (path.getText().length()==0){
                    /*JOptionPane.showMessageDialog(null,
                            "Error: Please enter number bigger than 0", "Error Message",
                            JOptionPane.ERROR_MESSAGE);*/
                    validateBtn.setEnabled(false);
                } else {
                    validateBtn.setEnabled(true);
                }
            }
        });
        excludes.getDocument().addDocumentListener(this.getChangeListener());
        excludePaths.getDocument().addDocumentListener(this.getChangeListener());
        excludeFiles.getDocument().addDocumentListener(this.getChangeListener());
        suffixes.getDocument().addDocumentListener(this.getChangeListener());
        linkInspections.setListener(new LinkListener() {
            @Override
            public void linkSelected(LinkLabel aSource, Object aLinkData) {
                Settings settings = Settings.KEY.getData(DataManager.getInstance().getDataContext(main));
                if (settings != null) {

                    settings.select(settings.find(InspectionToolsConfigurable.class));
                    //settings.select(settings.find("editor.inspections"));
                }
            }
        }, null);
        URI uri = null;
        try {
            uri = new URI("https://www.github.com/isfett/php-analyzer");
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
        }
        URI finalUri = uri;
        linkDocumentation.setListener(new LinkListener() {
            @Override
            public void linkSelected(LinkLabel aSource, Object aLinkData) {
                if (Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().browse(finalUri);
                    } catch (IOException e) { /* TODO: error handling */ }
                } else { /* TODO: error handling */ }
            }
        }, null);
        //this.path.addBrowseFolderListener("test", "description", phpAnalyzerSettingsProvider.project, fileChooserDescriptor);
        return main;
    }

    public boolean isModified() {
        return modified;
    }

    public void apply() {
        PHPAnalyzerSettingsProvider settingsProvider = PHPAnalyzerSettingsProvider.getInstance(project);
        settingsProvider.setPath(StringUtils.deleteWhitespace(path.getText()));
        settingsProvider.setExcludes(StringUtils.deleteWhitespace(excludes.getText()));
        settingsProvider.setExcludePaths(StringUtils.deleteWhitespace(excludePaths.getText()));
        settingsProvider.setExcludeFiles(StringUtils.deleteWhitespace(excludeFiles.getText()));
        settingsProvider.setSuffixes(StringUtils.deleteWhitespace(suffixes.getText()));

        reset();
        //phpAnalyzerSettingsProvider.setApikey(apiKey.getText());
        //phpAnalyzerSettingsProvider.setSlug(bookSlug.getText());
    }

    public void reset() {
        PHPAnalyzerSettingsProvider settingsProvider = PHPAnalyzerSettingsProvider.getInstance(project);
        path.setText(settingsProvider.getPath());
        excludes.setText(settingsProvider.getExcludes());
        excludePaths.setText(settingsProvider.getExcludePaths());
        excludeFiles.setText(settingsProvider.getExcludeFiles());
        suffixes.setText(settingsProvider.getSuffixes());
        this.modified = false;
    }

    private String executeCommand(String command) {

        StringBuffer output = new StringBuffer();

        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = "";
            while ((line = reader.readLine())!= null) {
                output.append(line + "\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return output.toString();
    }

    private DocumentListener getChangeListener()
    {
        DocumentListener documentListener = new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                toggleChanged();
            }
            public void removeUpdate(DocumentEvent e) {
                toggleChanged();
            }
            public void insertUpdate(DocumentEvent e) {
                toggleChanged();
            }

            public void toggleChanged() {
                modified = true;
            }
        };

        return documentListener;
    }
}