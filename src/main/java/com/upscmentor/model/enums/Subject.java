package com.upscmentor.model.enums;

public enum Subject {

    // ========== PRELIMS (GS Paper I) ==========
    HISTORY_ANCIENT("Ancient History", "Prelims & Mains", "Indus Valley, Vedic Period, Maurya, Gupta empires"),
    HISTORY_MEDIEVAL("Medieval History", "Prelims & Mains", "Delhi Sultanate, Mughal Empire, Bhakti & Sufi movements"),
    HISTORY_MODERN("Modern History", "Prelims & Mains", "British Rule, Freedom Struggle, Post-independence"),
    HISTORY_ART_CULTURE("Art & Culture", "Prelims & Mains", "Indian art forms, architecture, literature, festivals"),

    GEOGRAPHY_PHYSICAL("Physical Geography", "Prelims & Mains", "Geomorphology, Climatology, Oceanography"),
    GEOGRAPHY_INDIAN("Indian Geography", "Prelims & Mains", "Rivers, Climate, Soil, Natural Resources"),
    GEOGRAPHY_WORLD("World Geography", "Prelims & Mains", "Continents, Global issues, Resource distribution"),

    POLITY("Indian Polity & Governance", "Prelims & Mains", "Constitution, Parliament, Judiciary, Federalism, Panchayati Raj"),
    ECONOMY("Indian Economy", "Prelims & Mains", "Planning, GDP, Banking, Agriculture, Industry, Budget"),
    ENVIRONMENT("Environment & Ecology", "Prelims & Mains", "Biodiversity, Climate Change, Conservation, Pollution"),
    SCIENCE_TECH("Science & Technology", "Prelims & Mains", "Space, Biotech, IT, Defence, Nuclear, Nano-tech"),
    CURRENT_AFFAIRS("Current Affairs", "Prelims & Mains", "National & International events, Government schemes"),

    // ========== PRELIMS (CSAT - Paper II) ==========
    CSAT("CSAT (Aptitude)", "Prelims", "Comprehension, Logical Reasoning, Analytical, Maths, Decision Making"),

    // ========== MAINS GS PAPERS ==========
    GS1("GS Paper I", "Mains", "History, Culture, Geography, Society"),
    GS2("GS Paper II", "Mains", "Governance, Constitution, Polity, Social Justice, International Relations"),
    GS3("GS Paper III", "Mains", "Economy, Technology, Environment, Disaster Management, Security"),
    GS4("GS Paper IV - Ethics", "Mains", "Ethics, Integrity, Aptitude, Case Studies, Emotional Intelligence"),

    // ========== ESSAY ==========
    ESSAY("Essay", "Mains", "Essay writing on philosophical, social, economic, political topics"),

    // ========== GENERAL ==========
    GENERAL("General Guidance", "All", "Strategy, Preparation Tips, Time Management, Motivation");

    private final String displayName;
    private final String examStage;
    private final String description;

    Subject(String displayName, String examStage, String description) {
        this.displayName = displayName;
        this.examStage = examStage;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getExamStage() { return examStage; }
    public String getDescription() { return description; }
}