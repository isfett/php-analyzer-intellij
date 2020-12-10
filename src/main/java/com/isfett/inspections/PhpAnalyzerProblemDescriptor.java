package com.isfett.inspections;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptorBase;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class PhpAnalyzerProblemDescriptor extends ProblemDescriptorBase {
    public PhpAnalyzerProblemDescriptor(@NotNull PsiFile file, @NotNull String descriptionTemplate, LocalQuickFix[] fixes, @NotNull ProblemHighlightType highlightType, boolean isAfterEndOfLine, @Nullable TextRange rangeInElement, boolean showTooltip, boolean onTheFly) {
        super(file, file, descriptionTemplate, fixes, highlightType, isAfterEndOfLine, rangeInElement, showTooltip, onTheFly);
    }

    @Override
    public int getLineNumber() {
        return 0;
    }
}
