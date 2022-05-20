package io.github.camshaft54.scheduling

import java.util.*
import kotlin.math.min


class RandomizedSelector(private val students: List<Student>, private val classMap: Map<String, Class>) {
    private val rand = Random()

    fun generateRandomMasterSchedule(): Map<Period, List<Class>> {
        val remainingClasses = classMap.values.toMutableList()
        val masterSchedule = mutableMapOf<Period, MutableList<Class>>()
        for (period in Period.values()) {
            if (remainingClasses.isEmpty()) break
            masterSchedule[period] = if (!period.artOnly) {
                remainingClasses.chooseRandomNumberOfRandomElements(period.ordinal, false)
            } else {
                val artClasses = remainingClasses.filter { it.type == ClassType.ART }.toMutableList()
                if (artClasses.isEmpty()) continue
                artClasses.chooseRandomNumberOfRandomElements(period.ordinal, true)
            }
        }
        while (remainingClasses.isNotEmpty()) {
            val minPeriod = masterSchedule.minByOrNull { (_, classes) -> classes.size }?.key ?: break
            masterSchedule[minPeriod]?.add(remainingClasses.removeAt(rand.nextInt(remainingClasses.size)))
        }
        return masterSchedule
    }

    private fun MutableList<Class>.chooseRandomNumberOfRandomElements(
        periodNum: Int, artOnly: Boolean
    ): MutableList<Class> {
        val courseCount = if (artOnly) {
            if (this.size > 4) rand.nextInt(4, this.size) else this.size
        } else {
            val minCoursesPerPeriod = min(4.0, this.size.toDouble())
            val meanCourses = this.size.toDouble() / (7 - periodNum)
            val sdCourses = 1.5
            val maxCourses = this.size - (6 - periodNum) * minCoursesPerPeriod
            rand.nextGaussian(meanCourses, sdCourses).coerceIn(minCoursesPerPeriod, maxCourses).toInt()
        }
        return this.chooseRandomClasses(courseCount)
    }

    private fun MutableList<Class>.chooseRandomClasses(count: Int): MutableList<Class> {
        if (count <= 0) return mutableListOf()

        val result = mutableListOf<Class>()
        for (i in 0 until count) {
            result.add(this.removeAt(rand.nextInt(this.size)))
        }
        return result
    }

    private fun MutableList<Student>.chooseRandomStudents(count: Int): MutableList<Student> {
        println("Student list size: $size, count: $count")
        if (count <= 0) return mutableListOf()
        if (size <= count) return this

        val result = mutableListOf<Student>()
        for (i in 0 until count) {
            result.add(this.removeAt(rand.nextInt(this.size)))
        }
        return result
    }

    fun generateStudentSchedules(masterSchedule: Map<Period, List<Class>>): Map<String, MutableMap<Period, Class>> {
        val studentSchedules: Map<String, MutableMap<Period, Class>> = students.associate { it.name to mutableMapOf() }
        val waitlists: MutableMap<ClassType, MutableList<Student>> =
            ClassType.values().associateWith { mutableListOf<Student>() }.toMutableMap()
        val classRoster: MutableMap<Class, MutableList<Student>> = mutableMapOf()
        var totalWaitlists = 0
        for (period in Period.values()) {
            for (course in masterSchedule[period] ?: emptyList()) {
                val requestingStudents = students.filter { student -> course.id in student.requests }.toMutableList()
                val selectedStudents: MutableList<Student>
                if (course.type.maxCapacity > requestingStudents.size) {
                    selectedStudents =
                        waitlists[course.type]?.chooseRandomStudents(course.type.maxCapacity - requestingStudents.size)
                            ?: mutableListOf()
                    selectedStudents.addAll(requestingStudents)
                } else {
                    selectedStudents =
                        requestingStudents.toMutableList().chooseRandomStudents(course.type.maxCapacity)
                    val waitlistedStudents = requestingStudents.minus(selectedStudents.toSet())
                    totalWaitlists += waitlistedStudents.size
                    waitlists[course.type]?.addAll(waitlistedStudents)
                }
                selectedStudents.forEach { student -> studentSchedules[student.name]?.set(period, course) }
                classRoster[course] = selectedStudents
            }
        }

        val allOpenClasses = classRoster.filter { (course, students) ->
            course.type.maxCapacity >= students.size
        }
        // If there are still open classes in category and students on the waitlist for that category continue
        for (type in ClassType.values()) {
            val openClasses = allOpenClasses.filter { it.key.type == type }
            allOpenClasses.filter { it.key.type == type }.any { (course, students) ->
                if (openClasses.isNotEmpty() && waitlists[type]!!.isNotEmpty()) {
                    val selectedStudents = waitlists[type]!!.chooseRandomStudents(type.maxCapacity - students.size)
                    val currClassPeriod = masterSchedule.firstNotNullOf { if (course in it.value) it.key else null }
                    selectedStudents.forEach { student -> studentSchedules[student.name]?.set(currClassPeriod, course) }
                    students.addAll(selectedStudents)
                    waitlists[type]!!.removeAll(selectedStudents)
                    return@any false
                } else {
                    return@any true
                }
            }
        }
        println("Class Roster: ${classRoster.mapValues { (_, students) -> students.size }}")
        println("Waitlists: ${waitlists.mapValues { (_, students) -> students.size }}")
        return studentSchedules
    }

    fun generateStudentSchedules2(masterSchedule: MasterSchedule) {
        val scheduleStudents = students.map { ScheduledStudent(it) }
        val waitlists = ClassType.values().map { Waitlist(it) }
        // This value keeps track of all instances a student was waitlisted
        var totalWaitlistInstances = 0

        masterSchedule.forEachCourse { period, course ->
            val requestingStudents = students.filter { student -> course.id in student.requests }.toMutableList()
            val selectedStudents: MutableList<Student>
            if (course.type.maxCapacity > requestingStudents.size) {
                selectedStudents =
                    waitlists[course.type]?.chooseRandomStudents(course.type.maxCapacity - requestingStudents.size)
                        ?: mutableListOf()
                selectedStudents.addAll(requestingStudents)
            } else {
                selectedStudents =
                    requestingStudents.toMutableList().chooseRandomStudents(course.type.maxCapacity)
                val waitlistedStudents = requestingStudents.minus(selectedStudents.toSet())
                totalWaitlists += waitlistedStudents.size
                waitlists[course.type]?.addAll(waitlistedStudents)
            }
            selectedStudents.forEach { student -> studentSchedules[student.name]?.set(period, course) }
            classRoster[course] = selectedStudents
        }
    }

    class ScheduledStudent(student: Student) : Student(student.name, student.grade, student.requests) {
        val schedule: Map<Period, MutableList<Class>> = Period.values().associateWith { mutableListOf() }
    }

    data class MasterSchedule(val masterSchedule: Map<Period, List<RosteredCourse>>) {
        fun selectStudentsForEachCourse(action: (period: Period, course: RosteredCourse) -> List<ScheduledStudent>) {
            masterSchedule.forEach { (period, courses) ->
                courses.forEach { course ->
                    action.invoke(period, course)
                }
            }
        }
    }

    data class Waitlist(val classType: ClassType, val students: MutableList<Student> = mutableListOf())

    class RosteredCourse(course: Class) : Class(course.name, course.type, course.id)
}