package io.github.camshaft54.academic_scheduler.dataTypes

/**
 * Stores the possible class types as well as an invalid one. Currently, CourseType is not used by scheduler, but could be useful for later analytics
 */
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

/**
 * Stores the periods in the schedule and if they only allow art classes
 */
enum class Period(val artOnly: Boolean = false) {
    A, B, C, D, E(artOnly = true), F, G
}