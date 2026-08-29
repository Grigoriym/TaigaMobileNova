package com.grappim.taigamobile.utils.ui

import androidx.lifecycle.SavedStateHandle

/**
 * Persists individual UI-state fields to [savedStateHandle] so they survive process death.
 * [restore] seeds a ViewModel's initial state; [save] is called from that field's setter
 * alongside the usual `_state.update { }`. Only supports the types [SavedStateHandle] itself
 * natively stores (String, Int, Boolean, etc.) — not arbitrary objects.
 */
class RestorableState(private val savedStateHandle: SavedStateHandle) {
    fun <T> restore(key: String, default: T): T = savedStateHandle[key] ?: default

    fun <T> save(key: String, value: T) {
        savedStateHandle[key] = value
    }
}
