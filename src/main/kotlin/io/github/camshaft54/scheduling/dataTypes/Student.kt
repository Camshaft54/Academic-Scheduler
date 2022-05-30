package io.github.camshaft54.scheduling.dataTypes

open class Student(val name: String, val grade: Int, val requests: Array<Array<String>>) {
    fun hasValidRequests(courses: List<JSONCourse>): Boolean {
        val courseNames = courses.map { it.id }
        return requests.all { courseRanking ->
            courseRanking.all {
                it in courseNames
            } && ((courseRanking.size == 3 && !courses.first { it.id == courseRanking[0] }.required) || (courseRanking.size <= 1))
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Student

        if (name != other.name) return false
        if (grade != other.grade) return false
        if (!requests.contentEquals(other.requests)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + grade
        result = 31 * result + requests.contentHashCode()
        return result
    }

    override fun toString(): String {
        return "$name: {${requests.contentDeepToString()}}"
    }
}