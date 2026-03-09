package com.upscmentor.model.enums;

public enum OptionalSubject {

    // Literature Subjects
    ASSAMESE_LIT("Assamese Literature"),
    BENGALI_LIT("Bengali Literature"),
    BODO_LIT("Bodo Literature"),
    DOGRI_LIT("Dogri Literature"),
    ENGLISH_LIT("English Literature"),
    GUJARATI_LIT("Gujarati Literature"),
    HINDI_LIT("Hindi Literature"),
    KANNADA_LIT("Kannada Literature"),
    KASHMIRI_LIT("Kashmiri Literature"),
    KONKANI_LIT("Konkani Literature"),
    MAITHILI_LIT("Maithili Literature"),
    MALAYALAM_LIT("Malayalam Literature"),
    MANIPURI_LIT("Manipuri Literature"),
    MARATHI_LIT("Marathi Literature"),
    NEPALI_LIT("Nepali Literature"),
    ODIA_LIT("Odia Literature"),
    PUNJABI_LIT("Punjabi Literature"),
    SANSKRIT_LIT("Sanskrit Literature"),
    SANTHALI_LIT("Santhali Literature"),
    SINDHI_LIT("Sindhi Literature"),
    TAMIL_LIT("Tamil Literature"),
    TELUGU_LIT("Telugu Literature"),
    URDU_LIT("Urdu Literature"),

    // Non-Literature Subjects
    AGRICULTURE("Agriculture"),
    ANIMAL_HUSBANDRY("Animal Husbandry & Veterinary Science"),
    ANTHROPOLOGY("Anthropology"),
    BOTANY("Botany"),
    CHEMISTRY("Chemistry"),
    CIVIL_ENGINEERING("Civil Engineering"),
    COMMERCE("Commerce & Accountancy"),
    ECONOMICS("Economics"),
    ELECTRICAL_ENGINEERING("Electrical Engineering"),
    GEOGRAPHY("Geography"),
    GEOLOGY("Geology"),
    HISTORY("History"),
    LAW("Law"),
    MANAGEMENT("Management"),
    MATHEMATICS("Mathematics"),
    MECHANICAL_ENGINEERING("Mechanical Engineering"),
    MEDICAL_SCIENCE("Medical Science"),
    PHILOSOPHY("Philosophy"),
    PHYSICS("Physics"),
    POLITICAL_SCIENCE("Political Science & International Relations"),
    PSYCHOLOGY("Psychology"),
    PUBLIC_ADMINISTRATION("Public Administration"),
    SOCIOLOGY("Sociology"),
    STATISTICS("Statistics"),
    ZOOLOGY("Zoology");

    private final String displayName;

    OptionalSubject(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }
}