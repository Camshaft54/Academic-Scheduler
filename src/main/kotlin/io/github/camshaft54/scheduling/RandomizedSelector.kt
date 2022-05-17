package io.github.camshaft54.scheduling

import java.util.*


class RandomizedSelector(students: List<Student>, private val classMap: Map<String, Class>) {
    private val studentSchedules: Map<String, MutableMap<Period, Class>> =
        students.associate { it.name to mutableMapOf() }
    private val rand = Random()

    fun generateRandomMasterSchedule(): List<List<Class>> {
        val remainingClasses = classMap.values.toMutableList()
        val masterSchedule = mutableListOf<MutableList<Class>>()
        val minCoursesPerPeriod = 4.0
        for (i in 0..6) {
            if (remainingClasses.isEmpty())
                break
            val meanCourses = remainingClasses.size.toDouble() / (7 - i)
            val sdCourses = 1.5
            val maxCourses = remainingClasses.size - (6 - i) * minCoursesPerPeriod
//            println("mean: $meanCourses max: $maxCourses remaining: ${remainingClasses.size}")
            val courseCount = rand.nextGaussian(meanCourses, sdCourses).coerceIn(minCoursesPerPeriod, maxCourses).toInt()
            masterSchedule.add(remainingClasses.chooseRandomElements(courseCount))
        }
        while (remainingClasses.isNotEmpty()) {
            masterSchedule.sortBy { classes -> classes.size }
            masterSchedule.first().add(remainingClasses.removeAt(rand.nextInt(remainingClasses.size)))
        }
        return masterSchedule
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