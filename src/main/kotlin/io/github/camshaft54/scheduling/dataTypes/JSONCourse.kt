package io.github.camshaft54.scheduling.dataTypes

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
open class JSONCourse(
    @SerialName("Name") val name: String,
    @SerialName("Type") val typeStr: String,
    @SerialName("ID") val id: String,
    @SerialName("Sections") val sectionCount: Int,
    @SerialName("Required") val required: Boolean,
    @SerialName("Size") val maxSize: Int
) {
    fun toRosteredSections(): Array<RosteredSection> {
        val sections = mutableListOf<RosteredSection>()
        repeat(sectionCount) {
            sections.add(RosteredSection(this, it))
        }
        return sections.toTypedArray()
    }
}