package io.github.camshaft54.academic_scheduler

import io.github.camshaft54.academic_scheduler.dataTypes.JSONCourse
import io.github.camshaft54.academic_scheduler.dataTypes.JSONStudent
import io.github.camshaft54.academic_scheduler.dataTypes.MasterSchedule
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.File


/**
 * Prints the best schedule after 10,000 iterations using schedule generating algorithm
 */
fun main() {
    val bestMasterSchedule = bestScheduleFromInput(File("input/students.json"), File("input/courses.json"), 10000)
    if (bestMasterSchedule != null) {
        val requiredRejects = bestMasterSchedule.getAllRequiredRejects().values.flatten().associateBy { it.id }.values
        println("--- Overview ---")
        println("Percent of student course requests satisfied on average: ${"%.2f".format(bestMasterSchedule.calculateSuccess() * 100)}%")
        println("Required courses that were not satisfied (${requiredRejects.size}): ${requiredRejects.joinToString { it.id }}")
        println("--- Master Schedule ---")
        for ((period, sections) in bestMasterSchedule.masterSchedule) {
            println("$period${if (period.artOnly) " (Art Only)" else ""}: ${sections.joinToString()}")
        }
        println("--- Student Schedules ---")
        for (student in bestMasterSchedule.scheduledStudents) {
            println(
                "${student.name} (Grade ${student.grade}): ${
                    student.schedule.map { (period, section) -> "$period: $section" }.joinToString()
                }"
            )
        }
    }
}

/**
 * Convenience method for finding best schedule from student and course file with specified number of iterations
 */
@OptIn(ExperimentalSerializationApi::class)
fun bestScheduleFromInput(studentsFile: File, coursesFile: File, iterations: Int): MasterSchedule? {
    val jsonStudents = Json.decodeFromStream<Array<JSONStudent>>(studentsFile.inputStream())
    val students = jsonStudents.map(JSONStudent::toStudent)
    val jsonCourses = Json.decodeFromStream<Array<JSONCourse>>(coursesFile.inputStream()).toList()
    val sections = jsonCourses.map { it.toRosteredSections() }.toTypedArray().flatten()
    students.forEach {
        if (!it.hasValidRequests(jsonCourses)) {
            println(it)
        }
    }
    val randomizedSelector = RandomizedSelector(students, sections)
    return randomizedSelector.findBestSchedule(iterations)
}