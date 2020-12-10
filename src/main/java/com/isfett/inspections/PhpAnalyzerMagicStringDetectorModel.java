package com.isfett.inspections;

import com.intellij.openapi.util.TextRange;

class PhpAnalyzerMagicStringDetectorModel {
    private String type;
    private String visitor;
    private String value;
    private Integer startFilePos;
    private Integer endFilePos;

    @Override
    public String toString() {
        return String.format("Magic string \""+value+"\" found in type "+type+". Found by Visitor: "+visitor);
    }

    public TextRange getTextRange() {
        return new TextRange(startFilePos, endFilePos+1);
    }
}
