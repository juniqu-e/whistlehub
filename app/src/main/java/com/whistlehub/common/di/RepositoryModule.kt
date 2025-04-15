package com.whistlehub.common.di

import android.content.Context
import com.whistlehub.common.data.local.room.AppDatabase
import com.whistlehub.common.data.local.room.UserRepository
import com.whistlehub.common.data.remote.api.AuthApi
import com.whistlehub.common.data.remote.api.PlaylistApi
import com.whistlehub.common.data.remote.api.ProfileApi
import com.whistlehub.common.data.remote.api.RankingApi
import com.whistlehub.common.data.remote.api.TrackApi
import com.whistlehub.common.data.remote.api.WorkstationApi
import com.whistlehub.common.data.repository.AuthService
import com.whistlehub.common.data.repository.PlaylistService
import com.whistlehub.common.data.repository.ProfileService
import com.whistlehub.common.data.repository.RankingService
import com.whistlehub.common.data.repository.TrackService
import com.whistlehub.common.data.repository.WorkstationService
import com.whistlehub.common.util.LogoutManager
import com.whistlehub.common.util.TokenRefresh
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


/**
 RepositoryModule은 네트워크 API와 관련된 서비스(Repository) 객체들을 생성하여 DI 컨테이너에 등록하는 역할을 합니다.
 각 서비스 클래스는 특정 API 인터페이스를 사용하며, TokenRefresh를 통해 토큰 만료 시 갱신 로직이 적용됩니다.
 */

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    /**
     * provideTokenRefresh 함수는 TokenRefresh 인스턴스를 생성합니다.
     * - AuthApi: 인증 관련 API 요청(로그인, 토큰 갱신 등)을 수행하는 인터페이스입니다.
     * - tokenManager: 액세스 토큰과 리프레시 토큰을 저장 및 관리하는 클래스입니다.
     * TokenRefresh는 API 호출 시 토큰 만료("AF") 에러가 발생하면,
     * 리프레시 토큰을 사용하여 새로운 액세스 토큰을 받아 재시도하도록 ApiRepository의 executeApiCall을 오버라이드합니다.
     */
    @Provides
    @Singleton
    fun provideTokenRefresh(
        authApi: AuthApi,
        tokenManager: com.whistlehub.common.util.TokenManager,
        logoutManager: LogoutManager,
        userRepository: UserRepository
    ): TokenRefresh {
        return TokenRefresh(tokenManager, AuthService(authApi), logoutManager, userRepository)
    }


    @Provides
    @Singleton
    fun provideUserRepository(
        @ApplicationContext context: Context
    ): UserRepository {
        val database = AppDatabase.getInstance(context)
        return UserRepository(database.userDao())
    }

    @Provides
    @Singleton
    fun provideAuthService(
        authApi: AuthApi
    ): AuthService {
        return AuthService(authApi)
    }

    @Provides
    @Singleton
    fun provideProfileService(
        profileApi: ProfileApi,
        tokenRefresh: TokenRefresh
    ): ProfileService {
        return ProfileService(profileApi, tokenRefresh)
    }

    @Provides
    @Singleton
    fun providePlaylistService(
        playlistApi: PlaylistApi,
        tokenRefresh: TokenRefresh
    ): PlaylistService {
        return PlaylistService(playlistApi, tokenRefresh)
    }

    @Provides
    @Singleton
    fun provideTrackService(
        trackApi: TrackApi,
        tokenRefresh: TokenRefresh
    ): TrackService {
        return TrackService(trackApi, tokenRefresh)
    }

    @Provides
    @Singleton
    fun provideWorkstationService(
        workstationApi: WorkstationApi,
        tokenRefresh: TokenRefresh
    ): WorkstationService {
        return WorkstationService(workstationApi, tokenRefresh)
    }

    @Provides
    @Singleton
    fun provideRankingService(
        rankingApi: RankingApi,
        tokenRefresh: TokenRefresh
    ): RankingService {
        return RankingService(rankingApi, tokenRefresh)
    }
}