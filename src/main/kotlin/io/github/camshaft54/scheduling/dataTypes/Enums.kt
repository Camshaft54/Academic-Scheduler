package io.github.camshaft54.scheduling.dataTypes

enum class CourseType(val displayName: String) {
    MATH("Math"),
    SCIENCE("Science"),
    ENGLISH("English"),
    HISTORY("History"),
    LANGUAGE("Language"),
    ART("Art"),
    INVALID("Invalid Class Type");

    companion object {
        fun String.toCourseType(): CourseType = CourseType.values().firstOrNull { it.displayName == this } ?: INVALID
    }
}

enum class Period(val artOnly: Boolean = false) {
    A, B, C, D, E(artOnly = true), F, G
}