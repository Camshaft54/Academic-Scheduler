package io.github.camshaft54.scheduling

import io.github.camshaft54.scheduling.dataTypes.*
import java.util.*
import kotlin.math.min


class RandomizedSelector(private val students: List<Student>, private val sections: List<RosteredSection>) {
    fun findBestSchedule(iterations: Int): MasterSchedule? {
        var bestSchedule: MasterSchedule? = null
        var bestMinRequiredRejects = Integer.MAX_VALUE
        var bestSuccess = 0.0
        for (i in 0 until iterations) {
            val newSchedule = generateStudentSchedules(generateRandomMasterSchedule())
            val newMinRequiredRejects = newSchedule.getAllRequiredRejects().size
            val newSuccess = newSchedule.calculateSuccess()
            if (newMinRequiredRejects < bestMinRequiredRejects && bestSuccess < newSuccess) {
                bestSchedule = newSchedule
                bestMinRequiredRejects = newMinRequiredRejects
                bestSuccess = newSuccess
            }
        }
        return bestSchedule
    }

    private fun generateRandomMasterSchedule(): MasterSchedule {
        val remainingCourses = sections.toMutableList()
        val masterSchedule = mutableMapOf<Period, MutableList<RosteredSection>>()
        // Randomly select random number of courses for each period
        for (period in Period.values()) {
            if (remainingCourses.isEmpty()) break
            masterSchedule[period] = if (!period.artOnly) {
                remainingCourses.removeRandomNumberOfRandomCourses(period.ordinal, false)
            } else {
                val artCourses = remainingCourses.filter { section -> section.type == CourseType.ART }.toMutableList()
                if (artCourses.isEmpty()) continue
                artCourses.removeRandomNumberOfRandomCourses(period.ordinal, true)
            }.apply { remainingCourses.removeAll(this) }
        }
        // Go through remaining unselected courses and add them to the period with the minimum number of classes
        while (remainingCourses.isNotEmpty()) {
            val minPeriod = masterSchedule.minByOrNull { (_, courses) -> courses.size }?.key ?: break
            masterSchedule[minPeriod]?.add(remainingCourses.removeAt(rand.nextInt(remainingCourses.size)))
        }
        return MasterSchedule(masterSchedule, students.map { ScheduledStudent(it) })
    }

    private fun generateStudentSchedules(masterSchedule: MasterSchedule): MasterSchedule {
        for (round in 0 until 3) {
            masterSchedule.selectStudentsForEachCourse(round) { section, availableStudents ->
                if (section.maxSize - section.students.size >= availableStudents.size) {
                    availableStudents
                } else if (section.maxSize - section.students.size > 0) {
                    availableStudents.chooseRandomElements(section.maxSize - section.students.size)
                } else {
                    emptyList()
                }
            }
        }
        return masterSchedule
    }

    companion object {
        private val rand = Random()

        fun <T : Any> List<T>.chooseRandomElements(count: Int): List<T> {
            if (count <= 0) return emptyList()
            if (size <= count) return this
            val mutable = toMutableList()
            return (0 until count).map { mutable.removeAt(rand.nextInt(mutable.size)) }
        }

        private fun MutableList<RosteredSection>.removeRandomNumberOfRandomCourses(
            periodNum: Int, artOnly: Boolean
        ): MutableList<RosteredSection> {
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
}

