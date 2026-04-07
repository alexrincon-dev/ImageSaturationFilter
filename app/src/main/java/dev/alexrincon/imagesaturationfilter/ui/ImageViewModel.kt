package dev.alexrincon.imagesaturationfilter.ui

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.alexrincon.imagesaturationfilter.data.ImageRepository
import dev.alexrincon.imagesaturationfilter.domain.ApplyFilterUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ImageViewModel @Inject constructor(
    private val applyFilter: ApplyFilterUseCase,
    private val repository: ImageRepository
) : ViewModel() {

    private val _bitmap = MutableStateFlow<Bitmap?>(null)
    val bitmap: StateFlow<Bitmap?> = _bitmap.asStateFlow()

    private val _slider = MutableStateFlow(1f)
    val slider = _slider.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val snackbarMessage = _snackbarMessage.asSharedFlow()

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val uiState: StateFlow<UiState> =
        combine(
            _slider.debounce(150).distinctUntilChanged(),
            _bitmap
        ) { sliderValue, bmp ->
            sliderValue to bmp
        }.flatMapLatest { (sliderValue, bitmap) ->
            if (bitmap == null) {
                flowOf(UiState.Idle)
            } else {
                applyFilter.execute(bitmap, sliderValue)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Idle
        )

    fun onSliderChange(value: Float) {
        _slider.value = value
    }

    fun onPhotoSelected(uri: Uri?) {
        if (uri == null) {
            _bitmap.value = null
            return
        }
        viewModelScope.launch {
            try {
                val decoded = withContext(Dispatchers.IO) {
                    repository.loadBitmapFromUri(uri)
                }
                if (decoded != null) {
                    _bitmap.value = decoded
                } else {
                    _snackbarMessage.emit("Could not load image")
                }
            } catch (e: Exception) {
                _snackbarMessage.emit(e.message ?: "Could not load image")
            }
        }
    }
}
