package io.github.camshaft54.scheduling

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.File
import io.github.camshaft54.scheduling.CourseType.Companion.toCourseType

/*
Choose a random number of classes for each period (potentially optimize this by preferring to have a variety of types of classes each period)
Go through each class starting with the A period classes, and add as many students as possible
Students who do not receive the class they selected of that type will be put on a waitlist for the type
The next class with free spots will be filled with students on this waitlist
Keep track of the number of waitlists
Repeat process until the best combo is reached
 */

@OptIn(ExperimentalSerializationApi::class)
fun main() {
    val jsonStudents = Json.decodeFromStream<Array<JSONStudent>>(File("input/students.json").inputStream())
    val students = jsonStudents.map(JSONStudent::toStudent)
    val courses = Json.decodeFromStream<Array<Course>>(File("input/courses.json").inputStream()).toList()
    students.forEach {
        if (!it.hasValidRequests(courses)) {
            println(it)
        }
    }
    val randomizedSelector = RandomizedSelector(students, courses)
    println(randomizedSelector.findBestSchedule(1))
//    val experimentalSchedule = RandomizedSelector.MasterSchedule(classMap.values.groupBy({it.type}, {RosteredCourse(it)}).mapKeys { (classType, _) -> Period.values()[classType.ordinal] }, students.map {ScheduledStudent(it)})
}

@Serializable
data class JSONStudent(
    @SerialName("Student Name")
    val name: String,
    @SerialName("Grade")
    val grade: Int,
    @SerialName("Class 1 Request")
    val class1: String,
    @SerialName("Class 2 Request")
    val class2: String,
    @SerialName("Class 3 Request")
    val class3: String,
    @SerialName("Class 4 Request")
    val class4: String,
    @SerialName("Class 5 Request")
    val class5: String,
    @SerialName("Class 6 Request")
    val class6: String,
    @SerialName("Class 7 Request")
    val class7: String,
) {
    fun toStudent(): Student {
        val requests = mutableListOf(class1, class2, class3, class4, class5, class6, class7)
        requests.removeAll(String::isBlank)
        return Student(name, grade, requests.map { it.uppercase().split(", ").toTypedArray() }.toTypedArray())
    }
}

@Serializable
open class Course(
    @SerialName("Course Name")
    val name: String,
    @SerialName("Type")
    val typeStr: String,
    @SerialName("ID")
    val id: String,
    @SerialName("Required")
    val required: Boolean
) {
    val type: CourseType = typeStr.toCourseType()

    constructor(name: String, type: CourseType, id: String, required: Boolean) : this(name, type.displayName, id, required)

    override fun toString(): String {
        return id
    }
}

open class Student(val name: String, val grade: Int, val requests: Array<Array<String>>) {
    fun hasValidRequests(courses: List<Course>): Boolean {
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

enum class CourseType(val displayName: String, val maxCapacity: Int) {
    MATH("Math", 20),
    SCIENCE("Science", 20),
    ENGLISH("English", 15),
    HISTORY("History", 15),
    LANGUAGE("Language", 10),
    ART("Art", 12),
    INVALID("Invalid Class Type", 0);

    companion object {
        fun String.toCourseType(): CourseType = CourseType.values().firstOrNull { it.displayName == this } ?: INVALID
    }
}

enum class Period(val artOnly: Boolean = false) {
    A,
    B,
    C,
    D,
    E(artOnly = true),
    F,
    G
}