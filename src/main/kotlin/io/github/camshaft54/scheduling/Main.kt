package io.github.camshaft54.scheduling

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.File

/*
Choose a random number of classes for each period (potentially optimize this by prefering to have a variety of types of classes each period)
Go through each class starting with the A period classes, and add as many students as possible
Students who do not recieve the class they selected of that type will be put on a waitlist for the type
The next class with free spots will be filled with students on this waitlist
Keep track of the number of waitlists
Repeat process until the best combo is reached
 */

@OptIn(ExperimentalSerializationApi::class)
fun main() {
    val jsonStudents = Json.decodeFromStream<Array<JSONStudent>>(File("input/students.json").inputStream())
    val students = jsonStudents.map(JSONStudent::toStudent)
    val classArray = Json.decodeFromStream<Array<Class>>(File("input/classes.json").inputStream())
    val classMap = classArray.associateBy { it.id }
//    val requestMap = classMap.map { (id, _) ->
//        id to students.count { id in it.requests }
//    }
//    println("Course Requests:\n $requestMap")
    val randomizedSerializable = RandomizedSelector(students, classMap)
    println(randomizedSerializable.generateRandomMasterSchedule())
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
        requests.removeIf(String::isBlank)
        return Student(name, grade, requests.toTypedArray())
    }
}

@Serializable
data class Class(
    @SerialName("Course Name")
    val name: String,
    @SerialName("Type")
    val type: String,
    @SerialName("ID")
    val id: String
) {
    override fun toString(): String {
        return name
    }
}

data class Student(val name: String, val grade: Int, val requests: Array<String>) {
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
}

enum class ClassTypes() {
    MATH(),
    SCIENCE(),
    ENGLISH(),
    HISTORY(),
    LANGUAGE(),
    ART()
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