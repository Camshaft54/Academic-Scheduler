package io.github.camshaft54.scheduling.dataTypes

class ScheduledStudent(student: Student) : Student(student.name, student.grade, student.requests) {
    val schedule: MutableMap<Period, RosteredSection> = mutableMapOf()

    fun addCourse(period: Period, course: RosteredSection) {
        schedule[period] = course
    }

    fun haveClassDuring(period: Period) = schedule.contains(period)
    override fun toString(): String {
        return "{$name: $schedule}"
    }

    fun calculateSelectionSuccess(): Double {
        return schedule.map { it.value.id }.intersect(requests.flatten().toSet()).size.toDouble() / requests.size
    }

    fun getRequiredRejects(courses: List<RosteredSection>): List<RosteredSection> {
        // Go through each ranking course and if the request contains a ranking of size == 1 and the first ranked course matches the class, keep it
        return courses.filter { course -> requests.any { request -> request.size == 1 && course.matchesId(request[0]) && course.required && !schedule.values.any { it.matchesId(course.id) } } }
    }

    fun isRequestingCourse(course: RosteredSection, round: Int): Boolean {
        val requestRanking =
            requests.firstOrNull { requestRanking -> requestRanking.size > round && course.matchesId(requestRanking[round]) }
        if (requestRanking != null) {
            if (course.required && schedule.values.none { it.matchesId(requestRanking[0]) }) {
                return true
            } else if (!course.required) {
                for (i in 0 until round) {
                    if (schedule.values.any { it.matchesId(requestRanking[i]) }) {
                        return false
                    }
                }
                return true
            }
        }
        return false
    }
}