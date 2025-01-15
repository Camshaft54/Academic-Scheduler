package io.github.camshaft54.academic_scheduler.dataTypes

import io.github.camshaft54.academic_scheduler.dataTypes.CourseType.Companion.toCourseType

/**
 * Represents a single section of course. Has almost exact same data as a JSONCourse, just with a section number.
 */
open class RosteredSection(jsonCourse: JSONCourse, val section: Int) {
    @Suppress("unused")
    val name: String = jsonCourse.name
    val type: CourseType = jsonCourse.typeStr.toCourseType()
    val id: String = jsonCourse.id
    val sectionCount: Int = jsonCourse.sectionCount
    val required: Boolean = jsonCourse.required
    val maxSize: Int = jsonCourse.maxSize
    var students: MutableList<ScheduledStudent> = mutableListOf()

    fun matchesId(otherId: String): Boolean {
        return this.id == otherId
    }

    override fun toString(): String {
        return "$id-${section + 1}/$sectionCount"
    }
}