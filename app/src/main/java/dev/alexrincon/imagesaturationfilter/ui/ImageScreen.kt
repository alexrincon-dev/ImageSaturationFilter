package dev.alexrincon.imagesaturationfilter.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.alexrincon.imagesaturationfilter.core.ui.theme.ImageSaturationFilterTheme
import dev.alexrincon.imagesaturationfilter.ui.components.EditableImageSection
import dev.alexrincon.imagesaturationfilter.ui.components.EmptyImageSection

@Composable
fun ImageScreen(
    modifier: Modifier = Modifier,
    viewModel: ImageViewModel = hiltViewModel()
) {
    val slider by viewModel.slider.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val bitmap by viewModel.bitmap.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val imagePickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri ->
            uri?.let { viewModel.onPhotoSelected(it) }
        }

    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    ImageScreenContent(
        modifier = modifier,
        hasImage = bitmap != null,
        slider = slider,
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onSliderChange = viewModel::onSliderChange,
        onSelectImageClick = { imagePickerLauncher.launch("image/*") },
        onImageRemoved = { viewModel.onPhotoSelected(null) }
    )
}

@Composable
fun ImageScreenContent(
    modifier: Modifier = Modifier,
    hasImage: Boolean,
    slider: Float,
    uiState: UiState,
    snackbarHostState: SnackbarHostState,
    onSliderChange: (Float) -> Unit,
    onSelectImageClick: () -> Unit,
    onImageRemoved: () -> Unit,
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .height(400.dp)
                    .fillMaxWidth()
            ) {
                if (!hasImage) {
                    EmptyImageSection(
                        modifier = Modifier.align(Alignment.Center),
                        onSelectImageClick = onSelectImageClick
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize()) {
                        when (uiState) {
                            UiState.Idle, UiState.Loading -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }

                            is UiState.Success -> {
                                EditableImageSection(
                                    bitmap = uiState.bitmap,
                                    onImageRemoved = onImageRemoved
                                )
                            }

                            is UiState.Error -> {
                                Text(uiState.message)
                            }
                        }

                    }
                }
            }

            if (hasImage) {
                Spacer(modifier = Modifier.height(16.dp))

                Text("Saturation: ${slider.format(2)}")

                Slider(
                    value = slider,
                    onValueChange = onSliderChange,
                    valueRange = 0f..2f
                )
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun ImageScreenPreview() {
    ImageSaturationFilterTheme {
        ImageScreenContent(
            hasImage = false,
            slider = 1f,
            uiState = UiState.Idle,
            snackbarHostState = remember { SnackbarHostState() },
            onSliderChange = {},
            onSelectImageClick = {},
            onImageRemoved = {}
        )
    }
}

fun Float.format(digits: Int) = "%.${digits}f".format(this)
