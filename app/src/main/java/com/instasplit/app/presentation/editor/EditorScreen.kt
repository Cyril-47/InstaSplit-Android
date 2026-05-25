package com.instasplit.app.presentation.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    onBack: () -> Unit,
    onPreview: () -> Unit,
    viewModel: EditorViewModel = hiltViewModel()
) {
    val cropState by viewModel.cropState.collectAsStateWithLifecycle()
    val isBitmapLoading by viewModel.isBitmapLoading.collectAsStateWithLifecycle()
    val bitmap = viewModel.bitmap

    var isScrollEnabled by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Layout", fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back to Home")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentPadding = PaddingValues(16.dp)
            ) {
                Button(
                    onClick = onPreview,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = bitmap != null && !isBitmapLoading
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Preview Slices", fontSize = 16.sp)
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isBitmapLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (bitmap != null) {
                val ar = cropState.aspectRatio
                val canvasAspect = (ar.widthRatio * cropState.slideCount).toFloat() / ar.heightRatio

                // 1. Pinned Viewport Area (Fixed height container)
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val maxW = maxWidth
                    val maxH = maxHeight

                    // Constrain layout dimensions maintaining aspect ratio bounds
                    val width = if (maxW / maxH > canvasAspect) maxH * canvasAspect else maxW
                    val height = if (maxW / maxH > canvasAspect) maxH else maxW / canvasAspect

                    CropCanvas(
                        bitmap = bitmap,
                        cropState = cropState,
                        onScaleChange = viewModel::onScaleChange,
                        onOffsetChange = viewModel::onOffsetChange,
                        onScrollLockChange = { /* Pinned, no scroll lock needed */ },
                        modifier = Modifier.size(width, height)
                    )
                }

                // 2. Scrollable Settings Panel Area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    SettingsPanel(
                        cropState = cropState,
                        onScaleChange = viewModel::onScaleChange,
                        onSlideCountChange = viewModel::onSlideCountChange,
                        onAspectRatioChange = viewModel::onAspectRatioChange,
                        onQualityChange = viewModel::onQualityChange,
                        onReset = viewModel::onReset,
                        onAutoFit = viewModel::onAutoFit,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No image loaded", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

