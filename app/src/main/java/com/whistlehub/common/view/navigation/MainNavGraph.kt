package com.whistlehub.common.view.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.whistlehub.common.util.LogoutManager
import com.whistlehub.common.view.AppScaffold
import com.whistlehub.common.view.login.LoginScreen
import com.whistlehub.common.view.login.LoginSplashScreen
import com.whistlehub.common.view.signup.SelectTagsScreen
import com.whistlehub.common.view.signup.SignUpScreen
import com.whistlehub.common.viewmodel.SignUpViewModel
import com.whistlehub.search.viewmodel.SearchViewModel
import com.whistlehub.workstation.viewmodel.WorkStationViewModel
import com.whistlehub.common.view.passwordreset.PasswordResetScreen

/**
 * 앱의 전체 네비게이션 구조를 처리하는 메인 네비게이션 그래프
 */
@Composable
fun MainNavGraph(
    navController: NavHostController,
    logoutManager: LogoutManager,
    modifier: Modifier = Modifier
) {
    // LogoutHandler가 LogoutManager의 logoutEventFlow를 구독하도록 함
    LogoutHandler(
        navController = navController,
        logoutManager = logoutManager
    )

    NavHost(
        navController = navController,
        startDestination = "splash",
        modifier = modifier
    ) {
        composable("splash") {
            LoginSplashScreen(navController)
        }
        //유저 인증(로그인)
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    // 로그인 성공 시 메인 화면으로 이동
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onSignUpClick = {
                    // 회원가입 화면으로 이동
                    navController.navigate("signup") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onForgotPasswordClick = {
                    // 비밀번호 초기화 화면으로 이동
                    navController.navigate("passwordreset") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                navController = navController
            )
        }
        // 회원가입 정보 화면
        composable("signup") {
            SignUpScreen(
                onNext = {
                    // 태그 선택 화면으로 전환
                        userId, password, email, nickname, gender, birth ->
                    navController.navigate("selecttags/$userId/$password/$email/$nickname/$gender/$birth")
                },
                onLoginClick = {
                    // 로그인 화면으로 이동
                    navController.navigate("login") {
                        popUpTo("signup") { inclusive = true }
                    }
                },
                onForgotPasswordClick = {
                    // 비밀번호 초기화 화면으로 이동
                    navController.navigate("passwordreset") {
                        popUpTo("signup") { inclusive = true }
                    }
                },
            )
        }
        // 비밀번호 초기화 화면
        composable("passwordreset") {
            PasswordResetScreen(
                onLoginClick = {
                    // 로그인 화면으로 이동
                    navController.navigate("login") {
                        popUpTo("passwordreset") { inclusive = true }
                    }
                },
                onSignUpClick = {
                    // 회원가입 화면으로 이동
                    navController.navigate("signup") {
                        popUpTo("passwordreset") { inclusive = true }
                    }
                },
                navController = navController
            )
        }

        composable(
            route = "selecttags/{userId}/{password}/{email}/{nickname}/{gender}/{birth}",
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType },
                navArgument("password") { type = NavType.StringType },
                navArgument("email") { type = NavType.StringType },
                navArgument("nickname") { type = NavType.StringType },
                navArgument("gender") { type = NavType.StringType },
                navArgument("birth") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val password = backStackEntry.arguments?.getString("password") ?: ""
            val email = backStackEntry.arguments?.getString("email") ?: ""
            val nickname = backStackEntry.arguments?.getString("nickname") ?: ""
            val genderString = backStackEntry.arguments?.getString("gender") ?: "M"
            val birth = backStackEntry.arguments?.getString("birth") ?: ""
            // Composable 컨텍스트에서 미리 ViewModel 인스턴스를 생성합니다.
            val signUpViewModel: SignUpViewModel = hiltViewModel()

            SelectTagsScreen(
                userId = userId,
                password = password,
                email = email,
                nickname = nickname,
                gender = genderString.first(), // genderString는 "M" 또는 "F"
                birth = birth,
                onStartClick = { selectedTags ->
                    // onSuccess 콜백 회원가입 성공 시 메인 화면으로 이동
                    navController.navigate("main") {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                },
                onBackClick = {
                    // 뒤로가기 버튼 클릭 시 이전 화면으로 돌아감
                    navController.popBackStack()
                }
            )
        }
        //휘슬허브 메인 화면들
        composable("main") {
            MainScreenWithBottomNav(
                navController,
                logoutManager = logoutManager
            )
        }
    }
}

/**
 * 메인 앱 콘텐츠와 하단 네비게이션이 포함된 화면
 */
@Composable
fun MainScreenWithBottomNav(
    navController: NavHostController,
    logoutManager: LogoutManager
) {
    val newNavController = rememberNavController()
    val workStationViewModel: WorkStationViewModel = hiltViewModel()
    val searchViewModel: SearchViewModel = hiltViewModel()
    // 새로운 내부 네비게이션 컨트롤러 생성
    LaunchedEffect(key1 = logoutManager.logoutEventFlow) {
        logoutManager.logoutEventFlow.collect {
            // inner navController의 백스택을 초기화하고 로그인 화면으로 이동
            newNavController.navigate("login") {
                popUpTo(newNavController.graph.id) { inclusive = true }
            }
        }
    }
    AppScaffold(
        navController = newNavController,
        bottomBar = {
            BottomNavigationBar(navController = newNavController)
        },
    ) { paddingValues ->
        AppContentNavGraph(
            navController = newNavController,
            logoutManager = logoutManager,
            paddingValues = paddingValues,
            workStationViewModel = workStationViewModel,
            searchViewModel = searchViewModel,
        )
    }
}
