package com.whistlehub.workstation.view.component

import android.content.Context
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.whistlehub.common.data.remote.dto.request.WorkstationRequest
import com.whistlehub.common.util.rawWavList
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography
import com.whistlehub.workstation.data.InstrumentType
import com.whistlehub.workstation.data.Layer
import com.whistlehub.workstation.data.LayerButtonType
import com.whistlehub.workstation.data.rememberToastState
import com.whistlehub.workstation.view.component.record.RecordingPanel
import com.whistlehub.workstation.viewmodel.WorkStationViewModel
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AddLayerDialog(
    context: Context,
    onDismiss: () -> Unit,
    onLayerAdded: (Layer) -> Unit,
    viewModel: WorkStationViewModel,
    navController: NavController,
) {
    val customColor = CustomColors()
    val context = LocalContext.current
    val tracks by viewModel.tracks.collectAsState()
    val toastState = rememberToastState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        var selectedType by remember { mutableStateOf<LayerButtonType?>(null) }
        var selectedWavPath by remember { mutableStateOf<String?>(null) }
        Surface(
            modifier = Modifier
                .width(600.dp)
                .wrapContentHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            color = customColor.CommonSubBackgroundColor,
            tonalElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedContent(
                    targetState = selectedType,
                    transitionSpec = {
                        if (targetState == null || (initialState != null && (targetState?.ordinal
                                ?: -1) < initialState!!.ordinal)
                        ) {
                            // ← 뒤로 가는 전환
                            (slideInHorizontally { -it } + fadeIn()).togetherWith(
                                slideOutHorizontally { it } + fadeOut())
                        } else {
                            // → 앞으로 가는 전환
                            (slideInHorizontally { it } + fadeIn()).togetherWith(
                                slideOutHorizontally { -it } + fadeOut())
                        }
                    }
                ) { target ->
                    when (target) {
                        null -> {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.Center
                            ) {
                                val entries = LayerButtonType.entries
                                val isOdd = entries.size % 2 != 0
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    verticalArrangement = Arrangement.Center,
                                    content = {
                                        itemsIndexed(
                                            items = entries,
                                            span = { index, _ ->
                                                if (isOdd && index == entries.lastIndex) GridItemSpan(
                                                    2
                                                ) else GridItemSpan(1)
                                            }
                                        ) { _, type ->
                                            Button(
                                                onClick = { selectedType = type },
                                                modifier = Modifier
                                                    .padding(8.dp)
                                                    .height(140.dp)
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(20.dp))
                                                    .background(
                                                        brush = Brush.linearGradient(
                                                            colors = listOf(
                                                                type.hexColor.copy(alpha = 0.3f),
                                                                Color.White.copy(alpha = 0.06f)
                                                            )
                                                        )
                                                    )
                                                    .border(
                                                        width = 2.dp,
                                                        brush = Brush.linearGradient(
                                                            colors = listOf(
                                                                Color.White.copy(alpha = 0.7f),
                                                                type.hexColor.copy(alpha = 0.5f)
                                                            )
                                                        ),
                                                        shape = RoundedCornerShape(20.dp)
                                                    ),
                                                shape = RoundedCornerShape(20.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = customColor.CommonButtonColor.copy(
                                                        0.1f
                                                    ),
                                                    contentColor = customColor.CommonTextColor
                                                )
                                            ) {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.Center
                                                ) {
                                                    Icon(
                                                        imageVector = type.iconStyle,
                                                        contentDescription = type.label,
                                                        tint = customColor.CommonLabelColor,
                                                        modifier = Modifier.size(32.dp)
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = type.label,
                                                        style = Typography.bodyLarge,
                                                        color = customColor.CommonLabelColor,
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                        }

                        LayerButtonType.SEARCH -> {
                            LaunchedEffect(Unit) {
                                onDismiss()
                                navController.navigate("search")
                            }
                        }

                        LayerButtonType.RECORD -> {
                            RecordingPanel(
                                viewModel = viewModel,
                            )
                        }

                        LayerButtonType.RECOMMEND -> {
                            // 현재 레이어의 ID들
                            val existingLayerIds = tracks.map { it.typeId }
                            Log.d("Recommend", existingLayerIds.toString())
                            if (existingLayerIds.isEmpty()) {
                                // 레이어 없으면 아무것도 하지 않고 종료
                                viewModel.showToast(
                                    "1개 이상의 레이어가 있어야 추천이 가능합니다.",
                                    Icons.Default.Error,
                                    Color(0xFFF44336)
                                )
                                onDismiss()
                            } else {
                                val request =
                                    WorkstationRequest.ImportRecommendTrackRequest(existingLayerIds)
                                viewModel.addLayerFromRecommendTrack(request, context)
                                onDismiss()
                            }
                        }
                    }
                }
            }
        }
    }
}


fun getRawWavResMapForInstrument(context: Context, type: InstrumentType): Map<String, Int> {
    val res = context.resources
    val pkg = context.packageName

    return rawWavList.filter { it.startsWith(type.assetFolder) }
        .associateWith { res.getIdentifier(it, "raw", pkg) }.filterValues { it != 0 }
}


fun copyRawToInternal(context: Context, resId: Int, outFileName: String): File {
    val outFile = File(context.filesDir, outFileName)
    val input = context.resources.openRawResource(resId)
    val output = FileOutputStream(outFile)
    input.copyTo(output)
    input.close()
    output.close()
    return outFile
}