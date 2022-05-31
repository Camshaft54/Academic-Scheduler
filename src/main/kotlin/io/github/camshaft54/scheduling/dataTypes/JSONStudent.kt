package io.github.camshaft54.scheduling.dataTypes

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data type for taking in json students using kotlinx.serialization
 */
@Serializable
data class JSONStudent(
    @SerialName("Student Name") val name: String,
    @SerialName("Grade") val grade: Int,
    @SerialName("Class 1 Request") val class1: String,
    @SerialName("Class 2 Request") val class2: String,
    @SerialName("Class 3 Request") val class3: String,
    @SerialName("Class 4 Request") val class4: String,
    @SerialName("Class 5 Request") val class5: String,
    @SerialName("Class 6 Request") val class6: String,
    @SerialName("Class 7 Request") val class7: String,
) {
    fun toStudent(): Student {
        val requests = mutableListOf(class1, class2, class3, class4, class5, class6, class7)
        requests.removeAll(String::isBlank)
        return Student(name, grade, requests.map { it.uppercase().split(", ").toTypedArray() }.toTypedArray())
    }
}