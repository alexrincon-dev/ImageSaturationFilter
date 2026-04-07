package dev.alexrincon.imagesaturationfilter.domain

import android.graphics.Bitmap
import dev.alexrincon.imagesaturationfilter.data.ImageProcessor
import dev.alexrincon.imagesaturationfilter.ui.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class ApplyFilterUseCase(
    private val processor: ImageProcessor
) {

    fun execute(
        input: Bitmap,
        value: Float
    ): Flow<UiState> = flow<UiState> {

        val result = processor.applyFilter(input, value)

        emit(UiState.Success(result))

    }.catch {
        emit(UiState.Error(it.message ?: "Error"))
    }.flowOn(Dispatchers.Default)
}
