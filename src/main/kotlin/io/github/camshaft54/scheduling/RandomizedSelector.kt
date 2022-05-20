package io.github.camshaft54.scheduling

import io.github.camshaft54.scheduling.RandomizedSelector.Companion.chooseRandomElements
import java.util.*
import kotlin.math.min


class RandomizedSelector(private val students: List<Student>, private val classMap: Map<String, Class>) {

    fun generateRandomMasterSchedule(): MasterSchedule {
        val remainingClasses = classMap.values.toMutableList()
        val masterSchedule = mutableMapOf<Period, MutableList<RosteredCourse>>()
        for (period in Period.values()) {
            if (remainingClasses.isEmpty()) break
            masterSchedule[period] = if (!period.artOnly) {
                remainingClasses.chooseRandomNumberOfRandomElements(period.ordinal, false)
            } else {
                val artClasses = remainingClasses.filter { it.type == ClassType.ART }.toMutableList()
                if (artClasses.isEmpty()) continue
                artClasses.chooseRandomNumberOfRandomElements(period.ordinal, true)
            }.map {RosteredCourse(it)}.toMutableList()
        }
        while (remainingClasses.isNotEmpty()) {
            val minPeriod = masterSchedule.minByOrNull { (_, classes) -> classes.size }?.key ?: break
            masterSchedule[minPeriod]?.add(RosteredCourse(remainingClasses.removeAt(rand.nextInt(remainingClasses.size))))
        }
        return MasterSchedule(masterSchedule, students.map { ScheduledStudent(it) })
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
        return this.chooseRandomElements(courseCount).toMutableList()
    }

//    fun generateStudentSchedules(masterSchedule: Map<Period, List<Class>>): Map<String, MutableMap<Period, Class>> {
//        val studentSchedules: Map<String, MutableMap<Period, Class>> = students.associate { it.name to mutableMapOf() }
//        val waitlists: MutableMap<ClassType, MutableList<Student>> =
//            ClassType.values().associateWith { mutableListOf<Student>() }.toMutableMap()
//        val classRoster: MutableMap<Class, MutableList<Student>> = mutableMapOf()
//        var totalWaitlists = 0
//        for (period in Period.values()) {
//            for (course in masterSchedule[period] ?: emptyList()) {
//                val requestingStudents = students.filter { student -> course.id in student.requests }.toMutableList()
//                val selectedStudents: MutableList<Student>
//                if (course.type.maxCapacity > requestingStudents.size) {
//                    selectedStudents =
//                        waitlists[course.type]?.chooseRandomStudents(course.type.maxCapacity - requestingStudents.size)
//                            ?: mutableListOf()
//                    selectedStudents.addAll(requestingStudents)
//                } else {
//                    selectedStudents =
//                        requestingStudents.toMutableList().chooseRandomStudents(course.type.maxCapacity)
//                    val waitlistedStudents = requestingStudents.minus(selectedStudents.toSet())
//                    totalWaitlists += waitlistedStudents.size
//                    waitlists[course.type]?.addAll(waitlistedStudents)
//                }
//                selectedStudents.forEach { student -> studentSchedules[student.name]?.set(period, course) }
//                classRoster[course] = selectedStudents
//            }
//        }
//
//        val allOpenClasses = classRoster.filter { (course, students) ->
//            course.type.maxCapacity >= students.size
//        }
//        // If there are still open classes in category and students on the waitlist for that category continue
//        for (type in ClassType.values()) {
//            val openClasses = allOpenClasses.filter { it.key.type == type }
//            allOpenClasses.filter { it.key.type == type }.any { (course, students) ->
//                if (openClasses.isNotEmpty() && waitlists[type]!!.isNotEmpty()) {
//                    val selectedStudents = waitlists[type]!!.chooseRandomStudents(type.maxCapacity - students.size)
//                    val currClassPeriod = masterSchedule.firstNotNullOf { if (course in it.value) it.key else null }
//                    selectedStudents.forEach { student -> studentSchedules[student.name]?.set(currClassPeriod, course) }
//                    students.addAll(selectedStudents)
//                    waitlists[type]!!.removeAll(selectedStudents)
//                    return@any false
//                } else {
//                    return@any true
//                }
//            }
//        }
//        println("Class Roster: ${classRoster.mapValues { (_, students) -> students.size }}")
//        println("Waitlists: ${waitlists.mapValues { (_, students) -> students.size }}")
//        return studentSchedules
//    }

    fun generateStudentSchedules2(masterSchedule: MasterSchedule): MasterSchedule {
        masterSchedule.selectStudentsForEachCourse { period, course, availableStudents, waitlist ->
            val selectedStudents: List<ScheduledStudent>
            if (course.type.maxCapacity > availableStudents.size) {
                selectedStudents = availableStudents.plus(
                    waitlist.removeRandomStudents(course.type.maxCapacity - availableStudents.size)
                )
            } else {
                selectedStudents = availableStudents.chooseRandomElements(course.type.maxCapacity)
                waitlist.addStudents(availableStudents.minus(selectedStudents.toSet()))
            }
            selectedStudents
        }

        return masterSchedule
    }

    class MasterSchedule(
        val masterSchedule: Map<Period, List<RosteredCourse>>,
        val scheduledStudents: List<ScheduledStudent>
    ) {
        val waitlists = ClassType.values().associateWith { Waitlist(it) }
        fun selectStudentsForEachCourse(action: (period: Period, course: RosteredCourse, requestingStudents: List<ScheduledStudent>, waitlist: Waitlist) -> List<ScheduledStudent>) {
            for ((period, courses) in masterSchedule) {
                for (course in courses) {
                    val requestingStudents = scheduledStudents.filter { student -> course.id in student.requests }
                    val waitlist = waitlists[course.type]!!
                    val availableStudents = requestingStudents.toMutableList()
                    for (student in requestingStudents) {
                        if (student.haveClassDuring(period)) {
                            availableStudents.remove(student)
                            waitlist.addStudent(student)
                        }
                    }
                    course.students = action.invoke(period, course, availableStudents, waitlist).toMutableList()
                    course.students.forEach { it.addCourse(period, course) }
                }
            }
        }

        override fun toString(): String {
            return "$masterSchedule\n\n${waitlists.values}"
        }
    }

    companion object {
        private val rand = Random()

        fun <T : Any> List<T>.chooseRandomElements(count: Int): List<T> {
            println("Student list size: $size, count: $count")
            if (count <= 0) return emptyList()
            if (size <= count) return this
            val mutable = toMutableList()
            return (0 until count).map { mutable.removeAt(rand.nextInt(mutable.size)) }
        }
    }
}

class Waitlist(val classType: ClassType, val students: MutableList<ScheduledStudent> = mutableListOf()) {
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
        return "{$classType (instances=$waitlistInstances): $students}"
    }
}

class RosteredCourse(course: Class) : Class(course.name, course.type, course.id) {
    var students: MutableList<ScheduledStudent> = mutableListOf()
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
}