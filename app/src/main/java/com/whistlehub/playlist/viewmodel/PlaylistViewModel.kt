package com.whistlehub.playlist.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.whistlehub.common.data.local.room.UserRepository
import com.whistlehub.common.data.remote.dto.request.PlaylistRequest
import com.whistlehub.common.data.remote.dto.response.ApiResponse
import com.whistlehub.common.data.remote.dto.response.PlaylistResponse
import com.whistlehub.common.data.repository.PlaylistService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import javax.inject.Inject

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    val playlistService: PlaylistService,
    val userRepository: UserRepository
) : ViewModel() {
    private val _playlists =
        MutableStateFlow<List<PlaylistResponse.GetMemberPlaylistsResponse>>(emptyList())
    val playlists: StateFlow<List<PlaylistResponse.GetMemberPlaylistsResponse>> get() = _playlists

    private val _playlistInfo = MutableStateFlow<PlaylistResponse.GetPlaylistResponse?>(null)
    val playlistInfo: StateFlow<PlaylistResponse.GetPlaylistResponse?> get() = _playlistInfo

    private val _playlistTrack =
        MutableStateFlow<List<PlaylistResponse.PlaylistTrackResponse>>(emptyList())
    val playlistTrack: StateFlow<List<PlaylistResponse.PlaylistTrackResponse>> get() = _playlistTrack

    // 로딩 상태 추적을 위한 상태 변수
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading


    suspend fun getPlaylists() {
        try {
            val user = userRepository.getUser() // 사용자 정보 가져오기

            // 사용자 정보가 없을 경우 기본값으로 1(테스트계정 ID) 사용
            if (user == null) {
                Log.d("warning", "User not found, using default ID 1")
            }
            val playlistResponse =
                playlistService.getMemberPlaylists(user?.memberId ?: 0, 0, 10) // 페이지는 0번부터

            withContext(Dispatchers.Main) {
                _playlists.emit(playlistResponse.payload ?: emptyList())
            }
        } catch (e: Exception) {
            Log.d("error", "Failed to get playlists: ${e.message}")
        }
    }

    suspend fun getLikeTracks() {
        try {
            val user = userRepository.getUser() // 사용자 정보 가져오기

            // 사용자 정보가 없을 경우 기본값으로 1(테스트계정 ID) 사용
            if (user == null) {
                Log.d("warning", "User not found, using default ID 1")
            }
            val playlistResponse =
                playlistService.getLikeTracks(user?.memberId ?: 0, 0, 50) // 페이지는 0번부터

            if (playlistResponse.code == "SU") {
                _playlistInfo.value = PlaylistResponse.GetPlaylistResponse(
                    memberId = user?.memberId ?: 0,
                    name = "Liked Track",
                    description = "좋아요를 누른 트랙 목록입니다.",
                    imageUrl = null
                )
                _playlistTrack.value = playlistResponse.payload?.map {
                    PlaylistResponse.PlaylistTrackResponse(
                        playlistTrackId = it.trackId,
                        playOrder = null,
                        trackInfo = PlaylistResponse.Track(
                            trackId = it.trackId,
                            nickname = it.nickname,
                            title = it.title,
                            duration = it.duration,
                            imageUrl = it.imageUrl
                        )
                    )
                } ?: emptyList()
            } else {
                Log.d("error", "Failed to get like tracks: ${playlistResponse.message}")
            }
        } catch (e: Exception) {
            Log.d("error", "Failed to get playlists: ${e.message}")
        }
    }

    suspend fun getPlaylistInfo(playlistId: Int) {
        try {
            val playlistInfoResponse = playlistService.getPlaylists(playlistId)
            if (playlistInfoResponse.code == "SU") {
                _playlistInfo.value = playlistInfoResponse.payload
            } else {
                Log.d("error", "Failed to get playlist info: ${playlistInfoResponse.message}")
            }
        } catch (e: Exception) {
            Log.d("error", "Failed to get playlist info: ${e.message}")
        }
    }

    suspend fun getPlaylistTrack(playlistId: Int) {
        try {
            val playlistTrackResponse = playlistService.getPlaylistTracks(playlistId)
            if (playlistTrackResponse.code == "SU") {
                _playlistTrack.value = playlistTrackResponse.payload ?: emptyList()
            } else {
                Log.d("error", "Failed to get playlist tracks: ${playlistTrackResponse.message}")
            }
        } catch (e: Exception) {
            Log.d("error", "Failed to get playlist tracks: ${e.message}")
        }
    }

    suspend fun addTrackToPlaylist(playlistId: Int, trackId: Int) {
        try {
            val addTrackResponse = playlistService.addTrackToPlaylist(
                PlaylistRequest.AddTrackToPlaylistRequest(
                    playlistId = playlistId,
                    trackId = trackId
                )
            )
            if (addTrackResponse.code == "SU") {
                Log.d(
                    "success",
                    "Track added to playlist successfully Track$trackId into playlist$playlistId"
                )
            } else {
                Log.d("error", "${addTrackResponse.message}")
            }
        } catch (e: Exception) {
            Log.d("error", "Failed to add track to playlist: ${e.message}")
        }
    }

    suspend fun createPlaylist(
        name: String = "New Playlist",
        description: String? = null,
        trackIds: List<Int>? = null,
        image: MultipartBody.Part? = null
    ) {
        try {
            val createPlaylistResponse = playlistService.createPlaylist(
                name = name,
                description = description,
                trackIds = trackIds,
                image = image
            )
            if (createPlaylistResponse.code == "SU") {
                Log.d(
                    "success",
                    "Playlist created successfully with ID ${createPlaylistResponse.payload}"
                )
                getPlaylists() // 플레이리스트 목록 갱신
            } else {
                Log.d("error", "${createPlaylistResponse.message}")
            }
        } catch (e: Exception) {
            Log.d("error", "Failed to create playlist: ${e.message}")
        }
    }

    suspend fun deletePlaylist(playlistId: Int) {
        try {
            val deletePlaylistResponse = playlistService.deletePlaylist(playlistId)
            if (deletePlaylistResponse.code == "SU") {
                Log.d("success", "Playlist deleted successfully with ID $playlistId")
                getPlaylists() // 플레이리스트 목록 갱신
            } else {
                Log.d("error", "${deletePlaylistResponse.message}")
            }
        } catch (e: Exception) {
            Log.d("error", "Failed to delete playlist: ${e.message}")
        }
    }

    suspend fun updatePlaylist(
        playlistId: Int,
        name: String,
        description: String,
        trackIds: List<Int> = emptyList(),
        image: MultipartBody.Part? = null
    ) {
        try {
            // 로딩 상태 시작
            _isLoading.value = true

            lateinit var updatePlaylistImage: ApiResponse<Unit>
            // 정보 수정
            val updatePlaylistResponse = playlistService.updatePlaylist(
                request = PlaylistRequest.UpdatePlaylistRequest(
                    playlistId = playlistId,
                    name = name,
                    description = description
                )
            )
            // 트랙 수정
            if (trackIds.isNotEmpty()) {
                playlistService.updatePlaylistTracks(
                    request = PlaylistRequest.UpdatePlaylistTrackRequest(
                        playlistId = playlistId,
                        tracks = trackIds
                    )
                )
            }
            // 이미지 수정
            if (image != null) {
                updatePlaylistImage = playlistService.uploadPlaylistImage(
                    playlistId = playlistId,
                    image = image
                )
                Log.d("success", "Image is not null")
            } else {
                Log.d("error", "Image is null")
            }
            if (updatePlaylistResponse.code == "SU") {
                Log.d("success", "Playlist updated successfully with ID $playlistId")
                getPlaylistInfo(playlistId)
                getPlaylistTrack(playlistId)
            } else {
                Log.d("error", "${updatePlaylistResponse.message}")
            }
        } catch (e: Exception) {
            Log.d("error", "Failed to update playlist: ${e.message}")
        } finally {
            // 로딩 상태 종료
            _isLoading.value = false
        }
    }
}