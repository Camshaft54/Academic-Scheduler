package io.github.camshaft54.academic_scheduler.dataTypes

/**
 * Represents students with course requests that can be validated
 */
open class Student(val name: String, val grade: Int, val requests: Array<Array<String>>) {
    fun hasValidRequests(courses: List<JSONCourse>): Boolean {
        val courseNames = courses.map { it.id }
        return requests.all { courseRanking ->
            courseRanking.all {
                it in courseNames
            } && ((courseRanking.size == 3 && !courses.first { it.id == courseRanking[0] }.required) || (courseRanking.size <= 1))
        }
    }

    override fun toString(): String {
        return "$name: {${requests.contentDeepToString()}}"
    }
}