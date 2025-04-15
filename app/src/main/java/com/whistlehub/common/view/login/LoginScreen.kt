package com.whistlehub.common.view.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.whistlehub.R
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography
import com.whistlehub.common.viewmodel.LoginState
import com.whistlehub.common.viewmodel.LoginViewModel


@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit = {},
    onSignUpClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {},
    navController: NavHostController,
    viewModel: LoginViewModel = hiltViewModel()
) {
    // 키보드 자동으로 올라오게 하기
    val keyboardController = LocalSoftwareKeyboardController.current

    // 아이디/비밀번호 상태 관리
    var userId by remember { mutableStateOf("") }
    var userPassword by remember { mutableStateOf("") }
    var isUserIdFocused by remember { mutableStateOf(false) }
    var isPasswordFocused by remember { mutableStateOf(false) }

    // 로그인 결과에 따른 UI 처리 (예: 에러 메시지 출력)
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // ViewModel의 로그인 상태를 구독합니다.
    val loginState by viewModel.loginState.collectAsState()

    // 로그인 상태에 따른 이벤트 처리
    LaunchedEffect(loginState) {
        when (loginState) {
            is LoginState.Success -> {
                onLoginSuccess()
                viewModel.resetState()
            }
            is LoginState.Error -> {
                errorMessage = (loginState as LoginState.Error).message
            }
            else -> { /* Idle 또는 Loading 상태는 별도 처리 */ }
        }
    }

    val colors = CustomColors()
    val textFieldStyle = Typography.bodyMedium.copy(color = colors.CommonTextColor)
    val placeholderStyle = Typography.bodyMedium.copy(color = colors.CommonPlaceholderColor)
    val buttonTextStyle = Typography.titleMedium.copy(color = colors.CommonTextColor)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        // 배경 이미지
//        Image(
//            painter = painterResource(id = R.drawable.login_background),
//            contentDescription = null,
//            modifier = Modifier.fillMaxSize(),
//            contentScale = ContentScale.Crop
//        )
        // 배경
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.CommonBackgroundColor)
        )
        // 중앙 정렬을 위한 Box
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // 콘텐츠의 최대 너비를 제한하는 Box
            Box(
                modifier = Modifier
                    .widthIn(max = 800.dp)
                    .fillMaxHeight()
            ) {
                @Suppress("UnusedBoxWithConstraintsScope") // Lint 오류방지 코드
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val verticalPadding = when {
                        maxHeight < 500.dp -> 30.dp
                        maxHeight < 700.dp -> 60.dp
                        maxHeight < 800.dp -> 120.dp
                        else -> 150.dp
                    }
                    // 전체 화면을 채우는 Column
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 40.dp, vertical = verticalPadding),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 최상단: 로고
                        Image(
                            painter = painterResource(id = R.drawable.whistlehub_mainlogo),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .padding(bottom = 20.dp),
                        )
                        // 최하단: 폼 영역
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // 아이디 입력 필드
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .drawBehind {
                                        val strokeWidth = 1.dp.toPx()
                                        val y = size.height
                                        drawLine(
                                            color = if (isUserIdFocused) colors.CommonFocusColor else colors.Grey50,
                                            start = Offset(0f, y),
                                            end = Offset(size.width, y),
                                            strokeWidth = strokeWidth
                                        )
                                    }
                            ) {
                                BasicTextField(
                                    value = userId,
                                    onValueChange = { userId = it },
                                    textStyle = textFieldStyle,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.CenterStart)
                                        .onFocusChanged { focusState ->
                                            isUserIdFocused = focusState.isFocused
                                            if (focusState.isFocused) {
                                                keyboardController?.show()
                                            }
                                        },
                                    singleLine = true,
                                    interactionSource = remember { MutableInteractionSource() },
                                    decorationBox = { innerTextField ->
                                        Box {
                                            if (userId.isEmpty()) {
                                                Text(
                                                    text = "아이디를 입력하세요",
                                                    style = placeholderStyle
                                                )
                                            }
                                            innerTextField()
                                        }
                                    }
                                )
                            }
                            // 비밀번호 입력 필드
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .drawBehind {
                                        val strokeWidth = 1.dp.toPx()
                                        val y = size.height
                                        drawLine(
                                            color = if (isPasswordFocused) colors.CommonFocusColor else colors.Grey50,
                                            start = Offset(0f, y),
                                            end = Offset(size.width, y),
                                            strokeWidth = strokeWidth
                                        )
                                    }
                            ) {
                                BasicTextField(
                                    value = userPassword,
                                    onValueChange = { userPassword = it },
                                    textStyle = textFieldStyle,
                                    visualTransformation = PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.CenterStart)
                                        .onFocusChanged { focusState ->
                                            isPasswordFocused = focusState.isFocused
                                            if (focusState.isFocused) {
                                                keyboardController?.show()
                                            }
                                        },
                                    singleLine = true,
                                    interactionSource = remember { MutableInteractionSource() },
                                    decorationBox = { innerTextField ->
                                        Box {
                                            if (userPassword.isEmpty()) {
                                                Text(
                                                    text = "비밀번호를 입력하세요",
                                                    style = placeholderStyle
                                                )
                                            }
                                            innerTextField()
                                        }
                                    }
                                )
                            }
                            // 에러 메시지 표시
                            if (errorMessage != null) {
                                Text(
                                    text = errorMessage!!,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                            // 로그인 버튼
                            Button(
                                onClick = { viewModel.login(userId, userPassword) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.CommonButtonColor
                                ),
                                shape = RoundedCornerShape(8.dp),
                                enabled = loginState != LoginState.Loading // 로딩 시 비활성화
                            ) {
                                if (loginState == LoginState.Loading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        text = "로그인",
                                        style = buttonTextStyle,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                            }
                            // 회원가입/비밀번호 찾기 행
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(onClick = {
                                    onSignUpClick()
                                }
                                ) {
                                    Text(text = "회원가입", color = colors.Grey50)
                                }
                                Spacer(modifier = Modifier.width(20.dp))
                                Text(text = "|", color = colors.Grey50)
                                Spacer(modifier = Modifier.width(20.dp))
                                TextButton(onClick = {
                                    onForgotPasswordClick()
                                }) {
                                    Text(text = "비밀번호 초기화", color = colors.Grey50)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
