package com.upscmentor.model.enums;

public enum DifficultyLevel {
    BEGINNER("Beginner - Just Starting UPSC Prep"),
    INTERMEDIATE("Intermediate - Covered Basic Syllabus"),
    ADVANCED("Advanced - Revision & Deep Practice"),
    EXPERT("Expert - Final Revision & Mock Tests");

    private final String displayName;

    DifficultyLevel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }
}