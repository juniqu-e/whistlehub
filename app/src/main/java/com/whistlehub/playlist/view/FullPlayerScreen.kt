package com.whistlehub.playlist.view

import android.annotation.SuppressLint
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FastRewind
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.ShuffleOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.whistlehub.R
import com.whistlehub.common.view.navigation.Screen
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Pretendard
import com.whistlehub.common.view.theme.Typography
import com.whistlehub.playlist.view.component.PlayerComment
import com.whistlehub.playlist.view.component.PlayerPlaylist
import com.whistlehub.playlist.viewmodel.PlayerViewState
import com.whistlehub.playlist.viewmodel.TrackPlayViewModel
import com.whistlehub.profile.view.components.ProfileTrackDetailSheet
import com.whistlehub.workstation.viewmodel.WorkStationViewModel
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullPlayerScreen(
    navController: NavHostController,
    paddingValues: PaddingValues,
    workStationViewModel: WorkStationViewModel,
    trackPlayViewModel: TrackPlayViewModel = hiltViewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val user by trackPlayViewModel.user.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showPlayerMenu by remember { mutableStateOf(false) }

    // 아래로 드래그를 감지하는 offset 변수
    var dragOffsetY by remember { mutableStateOf(0f) }
    // 좌우 드래그를 감지하는 변수
    var dragOffsetX by remember { mutableStateOf(0f) }
    // 애니메이션을 위한 offset 변수
    val animatedOffsetY by animateFloatAsState(targetValue = dragOffsetY, label = "")

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .offset { IntOffset(0, animatedOffsetY.toInt()) } // 부드러운 애니메이션 적용
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        if (dragOffsetY > 150) { // 일정 거리 이상 드래그 시 뒤로 가기
                            navController.popBackStack()
                        }
                        dragOffsetY = 0f // 초기화
                    },
                    onVerticalDrag = { _, dragAmount ->
                        dragOffsetY += dragAmount
                    }
                )
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        // 왼쪽으로 드래그하면 다음곡
                        if (dragOffsetX < -150) {
                            coroutineScope.launch {
                                trackPlayViewModel.nextTrack()
                            }
                        }
                        // 오른쪽으로 드래그하면 이전곡
                        else if (dragOffsetX > 150) {
                            coroutineScope.launch {
                                trackPlayViewModel.previousTrack()
                            }
                        }
                        dragOffsetX = 0f // 초기화
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        dragOffsetX += dragAmount
                    }
                )
            },
        topBar = { PlayerHeader(navController) },
        bottomBar = {
            Column(Modifier.padding(bottom = paddingValues.calculateBottomPadding())) {
                PlayerController(trackPlayViewModel)
            }
        },
    ) { innerPadding ->
        // 배경 이미지
        val currentTrack by trackPlayViewModel.currentTrack.collectAsState(initial = null)
        val playerViewState by trackPlayViewModel.playerViewState.collectAsState(initial = PlayerViewState.PLAYING)

        PlayerBackground(
            Modifier
                .fillMaxSize()
                .blur(10.dp)
                .clickable {
                    // 배경 클릭 시 트랙 재생/일시정지
                    if (currentTrack != null && playerViewState == PlayerViewState.PLAYING) {
                        if (trackPlayViewModel.isPlaying.value) {
                            trackPlayViewModel.pauseTrack()
                        } else {
                            trackPlayViewModel.resumeTrack()
                        }
                    }
                })
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            when (playerViewState) {
                PlayerViewState.PLAYING -> {
                    TrackInfomation(Modifier.weight(1f), navController)
                }

                PlayerViewState.PLAYLIST -> {
                    PlayerPlaylist(Modifier.weight(1f),
                        workStationViewModel = workStationViewModel,
                        navController = navController
                    )
                }

                PlayerViewState.COMMENT -> {
                    PlayerComment(Modifier.weight(1f))
                }
            }
            TrackInteraction(trackPlayViewModel, onClickMore = { showPlayerMenu = true })
        }
        if (showPlayerMenu) {
            ProfileTrackDetailSheet(
                track = currentTrack!!,
                isOwnProfile = user?.memberId == currentTrack?.artist?.memberId,
                sheetState = sheetState,
                onDismiss = { showPlayerMenu = false },
                workStationViewModel = workStationViewModel,
                navController = navController,
            )
        }
    }
}


@Composable
fun PlayerHeader(
    navController: NavController,
    trackPlayViewModel: TrackPlayViewModel = hiltViewModel()
) {
    Row(Modifier.fillMaxWidth()) {
        IconButton(
            {
                trackPlayViewModel.setPlayerViewState(PlayerViewState.PLAYING)
                navController.navigateUp()
            },
        ) {
            Icon(
                Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "뒤로가기",
                tint = CustomColors().CommonIconColor
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TrackInfomation(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    trackPlayViewModel: TrackPlayViewModel = hiltViewModel()
) {
    // 트랙 정보를 표시하는 UI
    val currentTrack by trackPlayViewModel.currentTrack.collectAsState(initial = null)
    Column(
        modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Bottom)
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = currentTrack?.title ?: "Track Title",
            style = Typography.headlineMedium,
            fontFamily = Pretendard,
            fontWeight = FontWeight.Bold,
            color = CustomColors().CommonTitleColor,
            textAlign = TextAlign.Center
        )
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = currentTrack?.artist?.nickname ?: "Artist Name",
            style = Typography.bodyLarge,
            color = CustomColors().CommonSubTextColor,
            textAlign = TextAlign.Center
        )
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = "Tags",
            style = Typography.titleMedium,
            color = CustomColors().CommonSubTextColor,
            textAlign = TextAlign.Center
        )
        if (currentTrack?.tags?.isNotEmpty() == true) {
            FlowRow(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
            ) {
                currentTrack?.tags?.forEach { tag ->
                    Text(
                        text = "#${tag.name}",
                        style = Typography.bodyMedium,
                        color = CustomColors().CommonSubTextColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.clickable {
                            navController.navigate(Screen.TagRanking.route + "/${tag.tagId}/${tag.name}")
                        }
                    )
                }
            }
        } else {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "태그가 없습니다.",
                style = Typography.bodyLarge,
                color = CustomColors().CommonSubTextColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun TrackInteraction(
    trackPlayViewModel: TrackPlayViewModel = hiltViewModel(),
    onClickMore: () -> Unit = {},
) {
    val coroutineScope = rememberCoroutineScope()
    val currentTrack by trackPlayViewModel.currentTrack.collectAsState(initial = null)
    val playerViewState by trackPlayViewModel.playerViewState.collectAsState(initial = PlayerViewState.PLAYING)

    Row(
        Modifier
            .padding(10.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 좋아요 버튼
        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton({
                coroutineScope.launch {
                    trackPlayViewModel.likeTrack(currentTrack?.trackId ?: 0)
                }
            }) {
                if (currentTrack?.isLiked == true) {
                    Icon(
                        Icons.Filled.Favorite,
                        contentDescription = "좋아요 취소",
                        tint = CustomColors().CommonIconColor
                    )
                } else {
                    Icon(
                        Icons.Filled.FavoriteBorder,
                        contentDescription = "좋아요",
                        tint = CustomColors().CommonIconColor
                    )
                }
            }
            Text(
                text = currentTrack?.likeCount.toString(),
                style = Typography.bodyLarge,
                color = CustomColors().CommonSubTextColor,
                textAlign = TextAlign.Center
            )
        }
        // 댓글 버튼
        IconButton({
            if (playerViewState != PlayerViewState.COMMENT) {
                trackPlayViewModel.setPlayerViewState(PlayerViewState.COMMENT)
            } else {
                trackPlayViewModel.setPlayerViewState(PlayerViewState.PLAYING)
            }
        }) {
            Icon(
                Icons.Rounded.Forum,
                contentDescription = "댓글",
                tint = CustomColors().CommonIconColor
            )
        }
        // Now Playing 버튼
        IconButton({
            if (playerViewState != PlayerViewState.PLAYLIST) {
                trackPlayViewModel.setPlayerViewState(PlayerViewState.PLAYLIST)
            } else {
                trackPlayViewModel.setPlayerViewState(PlayerViewState.PLAYING)
            }
        }) {
            Icon(
                Icons.AutoMirrored.Rounded.QueueMusic,
                contentDescription = "플레이리스트",
                tint = CustomColors().CommonIconColor
            )
        }
        // 더보기 버튼
        IconButton({
            onClickMore()
        }) {
            Icon(
                Icons.Rounded.MoreVert,
                contentDescription = "더보기",
                tint = CustomColors().CommonIconColor
            )
        }
    }
}

@Composable
fun PlayerBackground(
    modifier: Modifier = Modifier,
    trackPlayViewModel: TrackPlayViewModel = hiltViewModel()
) {
    // 트랙의 배경 이미지를 표시하는 UI
    val currentTrack by trackPlayViewModel.currentTrack.collectAsState(initial = null)
    if (currentTrack?.imageUrl != null) {
        AsyncImage(
            model = currentTrack!!.imageUrl,
            contentDescription = "Track Image",
            modifier = modifier,
            contentScale = ContentScale.Crop,
        )
    } else {
        // 기본 배경 이미지
        Image(
            painterResource(R.drawable.default_track),
            contentDescription = "Track Image",
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    }
    // 어두운 오버레이 박스
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CustomColors().CommonBackgroundColor.copy(alpha = 0.5f))
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerController(
    trackPlayViewModel: TrackPlayViewModel = hiltViewModel(),
) {
    // 트랙 정보를 가져오기 위해 ViewModel 사용
    // 트랙 재생/일시정지/정지 버튼 클릭 시 ViewModel을 통해 트랙 제어
    val coroutineScope = rememberCoroutineScope()
    val currentTrack by trackPlayViewModel.currentTrack.collectAsState(initial = null)
    val isPlaying by trackPlayViewModel.isPlaying.collectAsState(initial = false)
    val playerPosition by trackPlayViewModel.playerPosition.collectAsState()
    val trackDuration by trackPlayViewModel.trackDuration.collectAsState()
    val isLooping by trackPlayViewModel.isLooping.collectAsState()
    val isShuffle by trackPlayViewModel.isShuffle.collectAsState()

    // 부드러운 슬라이더 애니메이션을 위해 animateFloatAsState 사용
    val animatedPlayerPosition by animateFloatAsState(
        targetValue = playerPosition.toFloat(),
        animationSpec = tween(durationMillis = 100, easing = LinearEasing),
        label = "Player Position"
    )

    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Slider(
            value = animatedPlayerPosition,
            onValueChange = { newPosition ->
                coroutineScope.launch {
                    trackPlayViewModel.seekTo(newPosition.toLong())
                }
            },
            valueRange = 0f..trackDuration.toFloat(), modifier = Modifier.fillMaxWidth(),
            track = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp) // 트랙 두께 변경
                        .background(CustomColors().CommonButtonColor, RoundedCornerShape(4.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedPlayerPosition / trackDuration.toFloat())
                            .height(8.dp)
                            .background(CustomColors().CommonOutLineColor, RoundedCornerShape(4.dp))
                    )
                }
            }, colors = SliderDefaults.colors(
                thumbColor = Color.Transparent,
                activeTrackColor = CustomColors().CommonTextColor,
                inactiveTrackColor = CustomColors().CommonSubTextColor
            )
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = formatDuration(playerPosition), color = Color.White)
            Text(text = formatDuration(trackDuration), color = Color.White)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        )
        {
            IconButton({
                trackPlayViewModel.toggleLooping()
            }) {
                Icon(
                    imageVector = if (isLooping) Icons.Rounded.RepeatOne else Icons.Rounded.Repeat,
                    contentDescription = "Loop",
                    tint = CustomColors().CommonIconColor,
                    modifier = Modifier.size(30.dp)
                )
            }
            IconButton(onClick = {
                coroutineScope.launch {
                    trackPlayViewModel.previousTrack()
                }
            }) {
                Icon(
                    imageVector = Icons.Rounded.FastRewind,
                    contentDescription = "PlayBack",
                    tint = CustomColors().CommonIconColor,
                    modifier = Modifier.size(30.dp)
                )
            }
            IconButton(
                onClick = {
                    if (isPlaying) {
                        trackPlayViewModel.pauseTrack()
                    } else {
                        if (currentTrack == null && trackPlayViewModel.playerTrackList.value.isNotEmpty()) {
                            coroutineScope.launch {
                                // 트랙이 없을 경우 첫 번째 트랙 재생
                                trackPlayViewModel.playTrack(trackPlayViewModel.playerTrackList.value[0].trackId)
                            }
                        } else if (currentTrack != null) {
                            trackPlayViewModel.resumeTrack()
                        }
                    }
                }) {
                Icon(
                    imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = CustomColors().CommonIconColor,
                    modifier = Modifier.size(30.dp)
                )
            }
            IconButton(onClick = {
                coroutineScope.launch {
                    trackPlayViewModel.nextTrack()
                }
            }) {
                Icon(
                    imageVector = Icons.Rounded.FastForward,
                    contentDescription = "PlayForward",
                    tint = CustomColors().CommonIconColor,
                    modifier = Modifier.size(30.dp)
                )
            }
            IconButton({
                trackPlayViewModel.toggleShuffle()
            }) {
                Icon(
                    imageVector = if (isShuffle) Icons.Rounded.ShuffleOn else Icons.Rounded.Shuffle,
                    contentDescription = "Shuffle",
                    tint = CustomColors().CommonIconColor,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}

@SuppressLint("DefaultLocale")
fun formatDuration(durationMs: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) % 60
    return String.format("%02d:%02d", minutes, seconds)
}