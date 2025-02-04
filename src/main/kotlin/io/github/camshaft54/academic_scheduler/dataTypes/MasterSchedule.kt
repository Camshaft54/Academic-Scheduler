package io.github.camshaft54.academic_scheduler.dataTypes

class MasterSchedule(
    val masterSchedule: Map<Period, List<RosteredSection>>, val scheduledStudents: List<ScheduledStudent>
) {
    /**
     * Iterates through the courses in the master schedule by period, prioritizing required courses, and
     * finds all requesting and available students for the current course
     */
    fun selectStudentsForEachCourse(
        round: Int,
        action: (section: RosteredSection, requestingStudents: List<ScheduledStudent>) -> List<ScheduledStudent>
    ) {
        for ((period, courses) in masterSchedule) {
            val reorderedCourses = courses.filter { it.required }.plus(courses.filter { !it.required })
            for (course in reorderedCourses) {
                if (round > 0 && course.required) continue
                val requestingStudents =
                    scheduledStudents.filter { student -> student.isRequestingCourse(course, round) }
                val availableStudents = requestingStudents.filterNot { student -> student.haveClassDuring(period) }
                val selectedStudents = action.invoke(course, availableStudents)
                course.students.addAll(selectedStudents)
                selectedStudents.forEach { it.addCourse(period, course) }
            }
        }
    }

    /**
     * Averages the selection success of all scheduled students
     */
    fun calculateSuccess(): Double {
        return scheduledStudents.map { it.calculateSelectionSuccess() }.average()
    }

    // Gets all the required courses that students were rejected from
    fun getAllRequiredRejects(): Map<ScheduledStudent, List<RosteredSection>> {
        return scheduledStudents.associateWith { it.getRequiredRejects(masterSchedule.values.flatten()) }
    }

    override fun toString(): String {
        return "$masterSchedule\n$scheduledStudents"
    }
}