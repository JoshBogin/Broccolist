package com.broccolist

import androidx.lifecycle.ViewModel
import com.broccolist.models.Section
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppViewModel : ViewModel() {
    private val _sections = MutableStateFlow(listOf<Section>())
    val sections = _sections.asStateFlow()

    fun addSection(section: Section) {
        val currentSections = _sections.value.toMutableList()
        currentSections.add(section)
        _sections.value = currentSections
    }

    fun removeSection(section: Section) {
        val currentSections = _sections.value.toMutableList()

        for (sec in _sections.value) {
            if (sec.name == section.name) {
                currentSections.remove(sec)
                break
            }
        }

        _sections.value = currentSections
    }
}
