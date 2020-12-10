package com.isfett.inspections;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.intellij.codeInspection.*;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.util.SmartList;
import com.isfett.PHPAnalyzerSettingsProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;


public class MagicStringInspection extends LocalInspectionTool { //PhpInspection
    public boolean VISITOR_ARGUMENT = false;
    public boolean VISITOR_ARRAY = false;
    public boolean VISITOR_ASSIGN = true;
    public boolean VISITOR_CONDITION = true;
    public boolean VISITOR_DEFAULT_PARAMETER = true;
    public boolean VISITOR_OPERATION = true;
    public boolean VISITOR_PROPERTY = true;
    public boolean VISITOR_RETURN = true;
    public boolean VISITOR_SWITCH_CASE = true;
    public boolean VISITOR_TERNARY = false;

    public boolean PROCESSOR_IGNORE_ARRAY_KEY = false;
    public boolean PROCESSOR_IGNORE_DEFINE_FUNCTION = false;
    public boolean PROCESSOR_IGNORE_EMPTY_STRING = false;

    JPanel optionsPanel;
    Gson gson;
    private boolean isRunning = false;

    public MagicStringInspection() {
        this.gson = new Gson();
    }



    @Nullable
    @Override
    public JComponent createOptionsPanel() {

        optionsPanel = new JPanel();
        //optionsPanel.setLayout(new FlowLayout(FlowLayout.TRAILING));
        GridBagLayout layout = new GridBagLayout();
        optionsPanel.setLayout(layout);

        JLabel labelVisitors = new JLabel("Visitors");
        JLabel labelProcessors = new JLabel("Processors");
        Font headlineLabelFont=new Font(labelProcessors.getFont().getName(),Font.BOLD,labelProcessors.getFont().getSize());
        labelProcessors.setFont(headlineLabelFont);
        labelVisitors.setFont(headlineLabelFont);

        //optionsPanel.add(SeparatorFactory.createSeparator("Options", null), BorderLayout.NORTH);

        JPanel visitorsPanel = new JPanel();
        visitorsPanel.setLayout(new BoxLayout(visitorsPanel, BoxLayout.Y_AXIS));
        visitorsPanel.setAlignmentY(JPanel.TOP_ALIGNMENT);
        visitorsPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        //visitorsPanel.add(SeparatorFactory.createSeparator("Visitors", null), BorderLayout.NORTH);
        visitorsPanel.add(labelVisitors);
        /*
        JPanel processorPanel = new JPanel(new FlowLayout());
        processorPanel.add(SeparatorFactory.createSeparator("Processors", null), BorderLayout.NORTH);

        optionsPanel.add(processorPanel);
        */
        this.addCheckbox("Argument", VISITOR_ARGUMENT, visitorsPanel, (isSelected) -> VISITOR_ARGUMENT = isSelected);
        this.addCheckbox("Array", VISITOR_ARRAY, visitorsPanel, (isSelected) -> VISITOR_ARRAY = isSelected);
        this.addCheckbox("Assign", VISITOR_ASSIGN, visitorsPanel, (isSelected) -> VISITOR_ASSIGN = isSelected);
        this.addCheckbox("Condition", VISITOR_CONDITION, visitorsPanel, (isSelected) -> VISITOR_CONDITION = isSelected);
        this.addCheckbox("DefaultParameter", VISITOR_DEFAULT_PARAMETER, visitorsPanel, (isSelected) -> VISITOR_DEFAULT_PARAMETER = isSelected);
        this.addCheckbox("Operation", VISITOR_OPERATION, visitorsPanel, (isSelected) -> VISITOR_OPERATION = isSelected);
        this.addCheckbox("Property", VISITOR_PROPERTY, visitorsPanel, (isSelected) -> VISITOR_PROPERTY = isSelected);
        this.addCheckbox("Return", VISITOR_RETURN, visitorsPanel, (isSelected) -> VISITOR_RETURN = isSelected);
        this.addCheckbox("SwitchCase", VISITOR_SWITCH_CASE, visitorsPanel, (isSelected) -> VISITOR_SWITCH_CASE = isSelected);
        this.addCheckbox("Ternary", VISITOR_TERNARY, visitorsPanel, (isSelected) -> VISITOR_TERNARY = isSelected);
        //optionsPanel.add(visitorsPanel);
        GridBagConstraints visitorsPanelGBC = new GridBagConstraints();
        visitorsPanelGBC.anchor = GridBagConstraints.NORTHWEST;
        visitorsPanelGBC.gridx = 1;
        visitorsPanelGBC.gridy = 1;
        /*
        gbc.setRow(1);
        gbc.setColumn(1);
        gbc.setAnchor(GridBagConstraints.NORTH);
        */
        optionsPanel.add(visitorsPanel, visitorsPanelGBC);
        JPanel processorsPanel = new JPanel();
        processorsPanel.setLayout(new BoxLayout(processorsPanel, BoxLayout.Y_AXIS));
        processorsPanel.setAlignmentY(JPanel.TOP_ALIGNMENT);
        processorsPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        processorsPanel.add(labelProcessors);
        //processorsPanel.add(SeparatorFactory.createSeparator("Processors", null), BorderLayout.NORTH);

        this.addCheckbox("IgnoreArrayKey", PROCESSOR_IGNORE_ARRAY_KEY, processorsPanel, (isSelected) -> PROCESSOR_IGNORE_ARRAY_KEY = isSelected);
        this.addCheckbox("IgnoreDefineFunction", PROCESSOR_IGNORE_DEFINE_FUNCTION, processorsPanel, (isSelected) -> PROCESSOR_IGNORE_DEFINE_FUNCTION = isSelected);
        this.addCheckbox("IgnoreEmptyString", PROCESSOR_IGNORE_EMPTY_STRING, processorsPanel, (isSelected) -> PROCESSOR_IGNORE_EMPTY_STRING = isSelected);
        processorsPanel.setMinimumSize(new Dimension(visitorsPanel.getWidth(), visitorsPanel.getHeight()));
        GridBagConstraints optionsPanelGBC = new GridBagConstraints();
        optionsPanelGBC.anchor = GridBagConstraints.NORTHWEST;
        optionsPanelGBC.gridx = 2;
        optionsPanelGBC.gridy = 1;
        optionsPanel.add(processorsPanel, optionsPanelGBC);

        return optionsPanel;
    }

    private void addCheckbox(
            @NotNull final String label,
            final boolean defaultValue,
            JPanel panel,
            final Consumer<Boolean> updateConsumer
    ) {
        final AbstractButton createdCheckbox = new JCheckBox(label, defaultValue);
        createdCheckbox.addItemListener((itemEvent) -> updateConsumer.accept(createdCheckbox.isSelected()));
        createdCheckbox.setAlignmentX(0);
        createdCheckbox.setHorizontalAlignment(SwingConstants.LEFT);
        panel.add(createdCheckbox);
        panel.add(Box.createVerticalStrut(5));

    }



    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
        System.out.println("build Visitor 1");
        return PsiElementVisitor.EMPTY_VISITOR;
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly, @NotNull LocalInspectionToolSession session) {
        System.out.println("build Visitor 2");

        return new PsiElementVisitor() {
            @Override
            public void visitFile(PsiFile file) {
                if(Thread.currentThread().getStackTrace()[2].getClassName().equals("com.intellij.psi.impl.source.xml.XmlFileImpl")) {
                    return;
                }
                System.out.println("Visit File");
                addDescriptors(checkFile(file, holder.getManager(), isOnTheFly));
            }

            private void addDescriptors(final ProblemDescriptor[] descriptors) {
                if (descriptors != null) {
                    for (ProblemDescriptor descriptor : descriptors) {
                        holder.registerProblem(descriptor);
                    }
                }
            }
        };
    }

    private String buildVisitors(Project project)
    {
        String visitors = "--visitors=";
        ArrayList<String> visitorNameList = new ArrayList<>();

        if (VISITOR_ARGUMENT) {
            visitorNameList.add("Argument");
        }

        if (VISITOR_ARRAY) {
            visitorNameList.add("Array");
        }

        if (VISITOR_ASSIGN) {
            visitorNameList.add("Assign");
        }

        if (VISITOR_CONDITION) {
            visitorNameList.add("Condition");
        }

        if (VISITOR_DEFAULT_PARAMETER) {
            visitorNameList.add("DefaultParameter");
        }

        if (VISITOR_OPERATION) {
            visitorNameList.add("Operation");
        }

        if (VISITOR_PROPERTY) {
            visitorNameList.add("Property");
        }

        if (VISITOR_RETURN)  {
            visitorNameList.add("Return");
        }

        if (VISITOR_SWITCH_CASE) {
            visitorNameList.add("SwitchCase");
        }

        if (VISITOR_TERNARY) {
            visitorNameList.add("Ternary");
        }

        if (visitorNameList.isEmpty()) {
            final Notification notification = new Notification("IsfettPHPAnalyzer", "Isfett PHP-Analyzer Magic Number Detector",
                   "You should at least select one Visitor in Isfett  PHP-Analyzer MagicNumberDetetector", NotificationType.INFORMATION);
            notification.notify(project);
            return "";
        }

        StringBuilder visitorNameStringBuilder = new StringBuilder();
        for (String visitorName : visitorNameList)
        {
            visitorNameStringBuilder.append(visitorName);
            visitorNameStringBuilder.append(",");
        }
        String visitorNames = visitorNameStringBuilder.toString();
        visitorNames = visitorNames.substring(0, visitorNames.length() -1);

        return visitors+visitorNames;
    }


    private String buildProcessors()
    {
        String processors = "--processors=";
        ArrayList<String> processorNameList = new ArrayList<>();

        if (PROCESSOR_IGNORE_ARRAY_KEY) {
            processorNameList.add("IgnoreArrayKey");
        }

        if (PROCESSOR_IGNORE_DEFINE_FUNCTION) {
            processorNameList.add("IgnoreDefineFunction");
        }

        if (PROCESSOR_IGNORE_EMPTY_STRING) {
            processorNameList.add("IgnoreEmptyString");
        }

        if (processorNameList.isEmpty()) {
            return "";
        }

        StringBuilder processorNameStringBuilder = new StringBuilder();
        for (String processorName : processorNameList)
        {
            processorNameStringBuilder.append(processorName);
            processorNameStringBuilder.append(",");
        }
        String processorNames = processorNameStringBuilder.toString();
        processorNames = processorNames.substring(0, processorNames.length() -1);

        return processors+processorNames;
    }

    @Override
    public void inspectionStarted(@NotNull LocalInspectionToolSession session, boolean isOnTheFly) {
        if (isRunning) {
            return;
        }
        super.inspectionStarted(session, isOnTheFly);
        isRunning = true;
    }

    @Override
    public void inspectionFinished(@NotNull LocalInspectionToolSession session, @NotNull ProblemsHolder problemsHolder) {
        super.inspectionFinished(session, problemsHolder);
        isRunning = false;
    }

    @Nullable
    @Override
    public ProblemDescriptor[] checkFile(@NotNull PsiFile file, @NotNull InspectionManager manager, boolean isOnTheFly) {
        this.cleanup(manager.getProject());

        System.out.println(file.getVirtualFile().getPath());
        final List<ProblemDescriptor> descriptors = new SmartList<>();

        File tempFile = this.createTempFile();
        System.out.println(tempFile.getPath());
        PHPAnalyzerSettingsProvider provider = ServiceManager.getService(manager.getProject(), PHPAnalyzerSettingsProvider.class);
        if (null == provider.getPath()) {
            return ProblemDescriptor.EMPTY_ARRAY;
        }
        ArrayList<PhpAnalyzerMagicStringDetectorModel> violations = checkMagicStringAnalyzer(file, tempFile, provider, manager.getProject());
        if (null != violations && !violations.isEmpty()) {
            System.out.println("not Empty");
            for (PhpAnalyzerMagicStringDetectorModel violation: violations)  {

                descriptors.add(
                        new PhpAnalyzerProblemDescriptor(file, violation.toString(), LocalQuickFix.EMPTY_ARRAY, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, false, violation.getTextRange(), true, isOnTheFly)
                );
            }
        }

        tempFile.delete();

        return descriptors.toArray(ProblemDescriptor.EMPTY_ARRAY);
    }

    @Override
    public boolean runForWholeFile() {
        return true;
    }

    @NotNull
    @Override
    public String getShortName() {
        return "magic-string-detector";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Magic String Detector";
    }

    private File createTempFile()
    {
        File temp = null;
        try {
            temp = File.createTempFile("php-analyzer-"+getShortName(), ".json");
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return temp;
    }

    private ArrayList<PhpAnalyzerMagicStringDetectorModel> checkMagicStringAnalyzer(PsiFile file, File tempFile, PHPAnalyzerSettingsProvider provider, Project project)
    {
        String visitors = buildVisitors(project);
        if ("" == visitors) {
            return new ArrayList<>();
        }
        String command = provider.getBaseCommand(getShortName())+visitors+" "+buildProcessors()+" --include-files="+file.getVirtualFile().getName()+" --with-json="+tempFile.getPath()+" "+file.getVirtualFile().getParent().getPath();
        System.out.println(command);
        executeCommand(command);
        return getViolationsFromExport(file, tempFile);
    }

    private ArrayList<PhpAnalyzerMagicStringDetectorModel> getViolationsFromExport(PsiFile file, File tempFile)
    {
        String fileContents = "";
        try {
            fileContents = Files.readString(Paths.get(tempFile.getPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(fileContents);
        Type collectionType = new TypeToken<ArrayList<PhpAnalyzerMagicStringDetectorModel>>(){}.getType();
        ArrayList<PhpAnalyzerMagicStringDetectorModel> items  = gson.fromJson( fileContents , collectionType);
        return items;
    }

    private String executeCommand(String command) {
        System.out.println(command);
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
        System.out.println(output.toString());

        return output.toString();
    }
}

