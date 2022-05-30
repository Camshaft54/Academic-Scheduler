package io.github.camshaft54.scheduling.dataTypes

import io.github.camshaft54.scheduling.dataTypes.CourseType.Companion.toCourseType

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
        return "$id-${section+1}/$sectionCount"
    }
}