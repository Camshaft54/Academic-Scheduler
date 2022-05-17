package io.github.camshaft54.scheduling

import java.util.*
import kotlin.math.min


class RandomizedSelector(students: List<Student>, private val classMap: Map<String, Class>) {
    private val studentSchedules: Map<String, MutableMap<Period, Class>> =
        students.associate { it.name to mutableMapOf() }
    private val rand = Random()

    fun generateRandomMasterSchedule(): Map<Period, List<Class>> {
        val remainingClasses = classMap.values.toMutableList()
        val masterSchedule = mutableMapOf<Period, MutableList<Class>>()
        for (period in Period.values()) {
            if (remainingClasses.isEmpty()) break
            masterSchedule[period] =
                if (!period.artOnly) {
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
        periodNum: Int,
        artOnly: Boolean
    ): MutableList<Class> {
        val courseCount =
            if (artOnly) {
                if (this.size > 4) rand.nextInt(4, this.size) else this.size
            } else {
                val minCoursesPerPeriod = min(4.0, this.size.toDouble())
                val meanCourses = this.size.toDouble() / (7 - periodNum)
                val sdCourses = 1.5
                val maxCourses = this.size - (6 - periodNum) * minCoursesPerPeriod
                rand.nextGaussian(meanCourses, sdCourses).coerceIn(minCoursesPerPeriod, maxCourses).toInt()
            }
        return this.chooseRandomElements(courseCount)
    }

    private fun MutableList<Class>.chooseRandomElements(count: Int): MutableList<Class> {
        if (count <= 0) return mutableListOf()

        val result = mutableListOf<Class>()
        for (i in 0 until count) {
            result.add(this.removeAt(rand.nextInt(this.size)))
        }
        return result
    }
}