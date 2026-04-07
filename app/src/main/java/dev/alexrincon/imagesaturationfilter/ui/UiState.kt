package dev.alexrincon.imagesaturationfilter.ui

import android.graphics.Bitmap

sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    data class Success(val bitmap: Bitmap) : UiState()
    data class Error(val message: String) : UiState()
}
