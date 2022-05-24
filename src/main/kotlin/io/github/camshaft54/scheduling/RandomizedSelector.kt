package io.github.camshaft54.scheduling

import io.github.camshaft54.scheduling.RandomizedSelector.Companion.chooseRandomElements
import java.util.*
import kotlin.math.min


class RandomizedSelector(private val students: List<Student>, private val courses: List<Course>) {

    fun generateRandomMasterSchedule(): MasterSchedule {
        val remainingCourses = courses.map { RosteredCourse(it) }.toMutableList()
        val masterSchedule = mutableMapOf<Period, MutableList<RosteredCourse>>()
        for (period in Period.values()) {
            if (remainingCourses.isEmpty()) break
            masterSchedule[period] = if (!period.artOnly) {
                remainingCourses.removeRandomNumberOfRandomCourses(period.ordinal, false)
            } else {
                val artCourses = remainingCourses.filter { it.type == CourseType.ART }.toMutableList()
                if (artCourses.isEmpty()) continue
                artCourses.removeRandomNumberOfRandomCourses(period.ordinal, true)
            }.apply { remainingCourses.removeAll(this) }.map { RosteredCourse(it) }.toMutableList()
        }
        while (remainingCourses.isNotEmpty()) {
            val minPeriod = masterSchedule.minByOrNull { (_, courses) -> courses.size }?.key ?: break
            masterSchedule[minPeriod]?.add(RosteredCourse(remainingCourses.removeAt(rand.nextInt(remainingCourses.size))))
        }
        return MasterSchedule(masterSchedule, students.map { ScheduledStudent(it) })
    }

    fun generateStudentSchedules(masterSchedule: MasterSchedule): MasterSchedule {
       for (round in 0 until 3) {
            masterSchedule.selectStudentsForEachCourse(round) { course, availableStudents ->
                println("$course: ${availableStudents.size}")
                val selectedStudents: List<ScheduledStudent> = if (course.type.maxCapacity > availableStudents.size) {
                    availableStudents
                } else {
                    availableStudents.chooseRandomElements(course.type.maxCapacity)
                }
                println(selectedStudents.size)
                selectedStudents
            }
           println(masterSchedule)
        }
        return masterSchedule
    }

    class MasterSchedule(
        val masterSchedule: Map<Period, List<RosteredCourse>>, val scheduledStudents: List<ScheduledStudent>
    ) {
        fun selectStudentsForEachCourse(
            round: Int,
            action: (course: RosteredCourse, requestingStudents: List<ScheduledStudent>) -> List<ScheduledStudent>
        ) {
            for ((period, courses) in masterSchedule) {
                for (course in courses) {
                    if (round > 0 && course.required) continue
                    val requestingStudents = scheduledStudents.filter { student -> student.isRequestingCourse(course, round) }
                    val availableStudents = requestingStudents.toMutableList()
                    requestingStudents.forEach { student ->
                        if (student.haveClassDuring(period)) {
                            availableStudents.remove(student)
                        }
                    }
                    course.students = action.invoke(course, availableStudents).toMutableList()
                    course.students.forEach { it.addCourse(period, course) }
                    if (course.id == "ENG4")
                        println(masterSchedule)
                }
            }
        }

        fun calculateSuccess(): Double {
            // Waitlist success heuristic
            // return waitlists.values.map { it.waitlistInstances }.sum() + 10 * waitlists.values.map { it.students.size }.sum()
            // Student requested courses received / requests
            return scheduledStudents.map { it.calculateSelectionSuccess() }.average()
        }

        override fun toString(): String {
            return "Master Schedule (success=${calculateSuccess()}): $masterSchedule"
        }
    }

    companion object {
        private val rand = Random(0)

        fun <T : Any> List<T>.chooseRandomElements(count: Int): List<T> {
            if (count <= 0) return emptyList()
            if (size <= count) return this
            val mutable = toMutableList()
            return (0 until count).map { mutable.removeAt(rand.nextInt(mutable.size)) }
        }

        private fun MutableList<RosteredCourse>.removeRandomNumberOfRandomCourses(
            periodNum: Int, artOnly: Boolean
        ): MutableList<RosteredCourse> {
            val courseCount = if (artOnly) {
                if (this.size > 4) rand.nextInt(4, this.size)
                else this.size
            } else {
                val minCoursesPerPeriod = min(4.0, this.size.toDouble())
                val meanCourses = this.size.toDouble() / (7 - periodNum)
                val sdCourses = 1.5
                val maxCourses = this.size - (6 - periodNum) * minCoursesPerPeriod
                rand.nextGaussian(meanCourses, sdCourses).coerceIn(minCoursesPerPeriod, maxCourses).toInt()
            }
            return this.chooseRandomElements(courseCount).toMutableList()
        }
    }

    fun findBestSchedule(iterations: Int): MasterSchedule? {
        var bestSchedule: MasterSchedule? = null
        var bestSuccess = 0.0
        for (i in 0 until iterations) {
            val newSchedule = generateStudentSchedules(generateRandomMasterSchedule())
            val newSuccess = newSchedule.calculateSuccess()
            println(newSuccess)
            if (newSchedule.calculateSuccess() > bestSuccess) {
                bestSchedule = newSchedule
                bestSuccess = newSuccess
            }
        }
        return bestSchedule
    }
}

class Waitlist(val courseType: CourseType, val students: MutableList<ScheduledStudent> = mutableListOf()) {
    var waitlistInstances: Int = 0
    fun removeRandomStudents(count: Int): List<ScheduledStudent> {
        val chosenStudents = students.chooseRandomElements(count)
        students.removeAll(chosenStudents)
        return chosenStudents
    }

    fun addStudents(newStudents: List<ScheduledStudent>) {
        students.addAll(newStudents)
        waitlistInstances += newStudents.size
    }

    fun addStudent(newStudent: ScheduledStudent) {
        students.add(newStudent)
        waitlistInstances++
    }

    override fun toString(): String {
        return "{$courseType (instances=$waitlistInstances): $students}"
    }
}

class RosteredCourse(course: Course) : Course(course.name, course.type, course.id, course.required) {
    var students: MutableList<ScheduledStudent> = mutableListOf()
    override fun toString(): String {
        return "$id(${students.size}/${type.maxCapacity})"
    }
}

class ScheduledStudent(student: Student) : Student(student.name, student.grade, student.requests) {
    private val schedule: MutableMap<Period, RosteredCourse> = mutableMapOf()

    fun addCourse(period: Period, course: RosteredCourse) {
        schedule[period] = course
    }

    fun haveClassDuring(period: Period) = schedule.contains(period)
    override fun toString(): String {
        return "{$name: $schedule}"
    }

    fun calculateSelectionSuccess(): Double {
        return schedule.map { it.value.id }.intersect(requests.flatten().toSet()).size.toDouble() / requests.size
    }

    fun isRequestingCourse(course: RosteredCourse, round: Int): Boolean {
        val requestRanking = requests.firstOrNull { requestRanking -> requestRanking.size > round && requestRanking[round] == course.id}
        println("${requestRanking.contentDeepToString()} $course")
        if (requestRanking != null) {
            if (course.required) {
                return true
            } else {
                for (i in 0 until round) {
                    if (requestRanking[i] in schedule.values.map {it.id}) {
                        println("${requestRanking[i]} in ${schedule.values.map {it.id}}")
                        return false
                    }
                }
                return true
            }
        } else {
            return false
        }
    }
}