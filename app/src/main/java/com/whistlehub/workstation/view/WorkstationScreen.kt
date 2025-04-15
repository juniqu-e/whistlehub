package com.whistlehub.workstation.view

import android.app.Activity
import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.whistlehub.common.view.component.UploadProgressOverlay
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography
import com.whistlehub.workstation.data.Layer
import com.whistlehub.workstation.view.component.AddLayerDialog
import com.whistlehub.workstation.view.component.BeatAdjustmentPanel
import com.whistlehub.workstation.view.component.CustomToast
import com.whistlehub.workstation.view.component.UploadDialog
import com.whistlehub.workstation.view.component.UploadSheet
import com.whistlehub.workstation.view.component.WaveformWithProgressIndicator
import com.whistlehub.workstation.viewmodel.WorkStationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkStationScreen(
    navController: NavController,
    viewModel: WorkStationViewModel,
    paddingValues: PaddingValues
) {
    val context = LocalContext.current
    val activity = LocalActivity.current as? Activity
    val customColors = CustomColors()
    val tracks by viewModel.tracks.collectAsState()
    val verticalScrollState = rememberScrollState()
    val selectedLayerId = remember { mutableStateOf<Int?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    val toastState by viewModel.toastMessage.collectAsState()
    val isPlaying by viewModel.isPlaying
    val showUploadDialog by viewModel.showUploadDialog
    val showUploadSheet by viewModel.showUploadSheet
    val showAddLayerDialog by viewModel.showAddLayerDialog
    val isUploading by viewModel.isUploading
    val projectBpm by viewModel.projectBpm
    val tagPairs by viewModel.tagPairs.collectAsState()
    val progress by viewModel.progress
    val waveformPoints by viewModel.waveformPoints
    val bottomBarActions = viewModel.bottomBarActions.copy(
        onPlayedClicked = {
            viewModel.onPlayClicked(
                context = context
            ) { success ->
                if (!success) {
                    viewModel.showToast(
                        message = "마디가 설정되지 않는 레이어가 있습니다.",
                        icon = Icons.Default.Error,
                        color = Color(0xFFF44336)
                    )
                }
            }
        },
        onAddInstrument = {
            viewModel.toggleAddLayerDialog(true)
        },
//        onUploadTrackConfirm = { metadata ->
//            viewModel.onUpload(context, metadata) { success ->
//                toastState.value = if (success) {
//                    ToastData("믹스 저장 성공", Icons.Default.CheckCircle, Color(0xFF4CAF50))
//                } else {
//                    ToastData("믹스 저장 실패", Icons.Default.Error, Color(0xFFF44336))
//                }
//            }
//        }
    )

    LaunchedEffect(Unit) {
        viewModel.getTagList()
    }

    CustomToast(
        toastData = toastState,
        onDismiss = { viewModel.clearToast() },
        position = Alignment.Center
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LayerPanel(
            tracks = tracks,
            verticalScrollState = verticalScrollState,
            modifier = Modifier.weight(5f),
            onAddInstrument = {
                showDialog = true
            },
            onDeleteLayer = {
                viewModel.deleteLayer(it)
            },
            onResetLayer = {
                viewModel.resetLayer(it)
            },
            onBeatAdjustment = { layer ->
                selectedLayerId.value = layer.id
            },
        )

        Spacer(modifier = Modifier.height(16.dp))
        // BPM 다이얼
//        BPMIndicator(
//            modifier = Modifier
//                .size(180.dp)
//                .background(Color.Transparent),
//            initialValue = projectBpm.toInt(),
//            minValue = 90,
//            maxValue = 200,
//            primaryColor = Color.LightGray,
//            secondaryColor = Color.DarkGray,
//            onPositionChange = { newBpm ->
//                viewModel.setProjectBpm(newBpm.toFloat())
//            }
//        )
        Spacer(modifier = Modifier.height(16.dp))

        if (showAddLayerDialog) {
            AddLayerDialog(
                context = context,
                onDismiss = { viewModel.toggleAddLayerDialog(false) },
                onLayerAdded = { newLayer ->
                    viewModel.addLayer(newLayer)
                },
                viewModel = viewModel,
                navController = navController,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        WaveformWithProgressIndicator(
            modifier = Modifier
                .size(180.dp),
            progress = progress,
            isPlaying = isPlaying,
            waveformPoints = waveformPoints,
            primaryColor = Color.Magenta,
            secondaryColor = Color.Yellow.copy(0.7f)
        )
//        MixProgressBar(
//            progress = 0.5f,
//            modifier = Modifier.fillMaxWidth(),
//            backgroundColor = Color(0xFF1E1E1E),
//            progressColor = Color(0xFF8E2DE2),
//            height = 8.dp
//        )
        Spacer(modifier = Modifier.height(12.dp))

        Column(
            verticalArrangement = Arrangement.Center,
        ) {
            viewModel.bottomBarProvider.WorkStationBottomBar(
                actions = bottomBarActions,
                context = context,
                isPlaying = isPlaying,
                showUpload = showUploadDialog,
            )
        }
    }
    val selectedLayer = tracks.firstOrNull { it.id == selectedLayerId.value }
    selectedLayer?.let { layer ->
        ModalBottomSheet(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.displayCutout)
                .padding(horizontal = 16.dp),
            onDismissRequest = { selectedLayerId.value = null }
        ) {
            BeatAdjustmentPanel(
                layer = layer,
                onDismiss = { selectedLayerId.value = null },
                onGridClick = { index ->
                    viewModel.toggleBeat(layer.id, index)
                    Log.d("WhistleHubAudioEngine", layer.patternBlocks.toString())
                },
                onAutoRepeatApply = { start, interval ->
                    viewModel.applyPatternAutoRepeat(selectedLayer.id, start, interval)
                    Log.d("WhistleHubAudioEngine", layer.patternBlocks.toString())
                }
            )
        }
    }

    if (showUploadSheet) {
        UploadSheet(
            onDismiss = { viewModel.toggleUploadSheet(false) },
            onUploadClicked = { metadata ->
                viewModel.toggleUploadSheet(false)
                viewModel.onUpload(context, metadata)
            },
            tagList = tagPairs
        )
    }

    if (showUploadDialog) {
        UploadDialog(
            onDismiss = { viewModel.toggleUploadDialog(false) },
            onUploadClicked = { metadata ->
                viewModel.toggleUploadDialog(false)
                viewModel.onUpload(context, metadata)
            },
            tagList = tagPairs
        )
    }

    UploadProgressOverlay(isLoading = isUploading)
}

@Composable
fun LayerPanel(
    tracks: List<Layer>,
    onAddInstrument: () -> Unit,
    onDeleteLayer: (Layer) -> Unit,
    onResetLayer: (Layer) -> Unit,
    onBeatAdjustment: (Layer) -> Unit,
    verticalScrollState: ScrollState,
    modifier: Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(verticalScrollState)
            .padding(16.dp)
    ) {
        if (tracks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "악기를 추가하고 \n 트랙을 만들어보세요!",
                    color = Color.White.copy(alpha = 0.8f), // 연한 흰색
                    style = Typography.titleLarge,
                    textAlign = TextAlign.Center
                )
            }
        }
        tracks.forEach { layer ->
            LayerItem(
                layer = layer,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                onDelete = onDeleteLayer,
                onReset = onResetLayer,
                onBeatAdjustment = onBeatAdjustment,
            )
            Spacer(modifier = Modifier.heightIn(8.dp))
        }
        // + 버튼
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(60.dp)
//                .padding(8.dp)
//                .background(Color.DarkGray, RoundedCornerShape(6.dp))
//                .clickable { onAddInstrument() },
//            contentAlignment = Alignment.Center
//        ) {
//            Icon(
//                imageVector = Icons.Default.Add,
//                contentDescription = "Add Instrument",
//                tint = Color(0xFF4ECCA3),
//                modifier = Modifier.size(32.dp)
//            )
//        }
    }
}

@Composable
fun LayerItem(
    layer: Layer,
    modifier: Modifier,
    onDelete: (Layer) -> Unit,
    onReset: (Layer) -> Unit,
    onBeatAdjustment: (Layer) -> Unit,
) {
    val customColors = CustomColors()
    var menuExpanded by remember { mutableStateOf(false) }
    val glassColor = try {
        Color(android.graphics.Color.parseColor(layer.colorHex)).copy(alpha = 0.3f)
    } catch (e: Exception) {
        Color(0xFFBDBDBD).copy(alpha = 0.7f) // fallback color
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .shadow(10.dp, RoundedCornerShape(20.dp)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
//        Box(
//            modifier = Modifier
//                .background(
//                    glassColor,
//                    RoundedCornerShape(12.dp)
//                )
//                .border(
//                    2.dp,
//                    glassColor,
//                    RoundedCornerShape(12.dp)
//                )
//                .size(80.dp)
//                .padding(12.dp),
//            contentAlignment = Alignment.Center
//        ) {
//            Text(text = layer.name, color = Color.Black, fontSize = 14.sp)
//        }
//        Spacer(modifier = Modifier.width(4.dp))
        Row(
            modifier = Modifier
                .weight(1f)
                .height(200.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            glassColor,
                            Color.White.copy(alpha = 0.09f)
                        )
                    )
                )
                .border(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.7f),
                            glassColor,
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = layer.name,
                    style = Typography.titleMedium,
                    color = customColors.CommonTextColor
                )
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = layer.description,
                    style = Typography.bodySmall,
                    color = customColors.CommonSubTextColor
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = layer.category,
                    style = Typography.bodySmall,
                    color = customColors.CommonSubTextColor
                )

                Spacer(modifier = Modifier.height(6.dp))
            }

            Box {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More Options",
                    tint = customColors.CommonIconColor,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { menuExpanded = true }
                )

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("마디 조정") },
                        onClick = {
                            menuExpanded = false
                            onBeatAdjustment(layer)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("레이어 삭제") },
                        onClick = {
                            menuExpanded = false
                            onDelete(layer)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("마디 초기화") },
                        onClick = {
                            menuExpanded = false
                            onReset(layer)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SliderMinimalExample() {
    var sliderPosition by remember { mutableFloatStateOf(0f) }
    Column {
        Slider(
            value = sliderPosition,
            onValueChange = { sliderPosition = it },
            thumb = {
                Box(
                    modifier = Modifier
                        .size(24.dp)              // 원하는 thumb 크기
                        .clip(CircleShape)        // 원형으로 클리핑
                        .background(Color.Cyan)   // thumb 색상
                )
            },
            track = { sliderPositions ->
                // 전체 막대 높이
                val trackHeight = 6.dp
                // 활성 범위의 끝 지점(0f~1f 사이)
                val fraction = sliderPositions.value

                Box(
                    modifier = Modifier
                        .fillMaxWidth()             // 슬라이더 전체 폭
                        .height(trackHeight)
                        .clip(RoundedCornerShape(percent = 50)) // 양 끝이 둥근 막대
                        .background(Color.LightGray)             // 비활성 구간 색상
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)  // value 비율만큼만 채우기
                        .height(6.dp)
                        .clip(RoundedCornerShape(percent = 50))
                        .background(Color.Cyan)     // 활성 구간 색상
                )
            }
        )
        Text(text = sliderPosition.toString())
    }
}

@Composable
fun AudioProgressBar(modifier: Modifier, backgroundColor: Color, progressColor: Color, height: Dp) {
    Box(
        modifier = modifier
            .height(height)
            .background(backgroundColor, RoundedCornerShape(4.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .background(progressColor, RoundedCornerShape(4.dp))
        )
    }
}

@Composable
fun getTrackColor(layer: Layer): Color {
    layer.colorHex?.let {
        return Color(android.graphics.Color.parseColor(it))
    }

    return when (layer.category.uppercase()) {
        "DRUM" -> Color(0xFFFFEE58) // 연한 노랑
        "BASS" -> Color(0xFF9575CD) // 보라
        "OTHERS" -> Color(0xFF80CBC4) // 민트
        else -> Color(0xFFBDBDBD) // 기본 회색
    }
}


