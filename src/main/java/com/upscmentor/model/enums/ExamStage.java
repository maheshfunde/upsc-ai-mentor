package com.upscmentor.model.enums;

public enum ExamStage {
    PRELIMS("Preliminary Examination"),
    MAINS("Main Examination"),
    INTERVIEW("Personality Test / Interview");

    private final String displayName;

    ExamStage(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }
}