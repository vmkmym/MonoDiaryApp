package com.example.monodiaryapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.monodiaryapp.ui.theme.MonoDiaryAppTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue

class EditActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MonoDiaryAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomeScreen2() // HomeScreen2 호출 추가
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyCenteredTopAppBar2(
    title: String,
    navigationIcon: @Composable () -> Unit,
    actionIcon: @Composable () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 28.sp)
            )
        },
        navigationIcon = navigationIcon,
        actions = {
            actionIcon()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen2() {
    val lastModifiedState = remember { mutableStateOf(formatDateWithDayOfWeek(System.currentTimeMillis())) }

    Scaffold(
        topBar = {
            MyCenteredTopAppBar2(
                title = "Today",
                navigationIcon = {
                    IconButton(onClick = { /* 홈액티비티가 나오게 화면 전환 */ }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Localized description"
                        )
                    }
                },
                actionIcon = {
                    IconButton(onClick = { /* 내용이 저장되는 버튼 */ }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Localized description"
                        )
                    }
                }
            )
        },
        bottomBar = { // BottomAppBar 추가
            MyBottomAppBar(
                navigationIcon = {
                    IconButton(onClick = { /* 현재 작성 중인 화면이 저장이 되지 않고 삭제됨 */ }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "삭제"
                        )
                    }
                },
                actionIcon1 = {
                    IconButton(onClick = { /* 본문 내용 복사하기 */ }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "복사"
                        )
                    }
                },
                actionIcon2 = {
                    IconButton(onClick = { /* 갤러리 접근 권한, 이미지 단일, 복수 가져오기 */ }) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "이미지 변경"
                        )
                    }
                },
                actionIcon3 = {
                    IconButton(onClick = { /* 현재 내용 자동 저장 후 갤러리로 화면 전환 */ }) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = "갤러리로 이동"
                        )
                    }
                },
                actionIcon4 = {
                    IconButton(onClick = {
                    /* Handle action icon click */
                        lastModifiedState.value = formatDateWithDayOfWeek(System.currentTimeMillis())
                    }) {
                        Icon(
                            imageVector = Icons.Default.Create,
                            contentDescription = "내용 수정"
                        )
                    }
                }
            )
        },
        content = { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                EditDiaryScreen(lastModifiedState.value)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDiaryScreen() {
    var titleState by remember { mutableStateOf(TextFieldValue()) }
    var mainTextState by remember { mutableStateOf(TextFieldValue()) }
    var songNameState by remember { mutableStateOf(TextFieldValue()) }
    var singerNameState by remember { mutableStateOf(TextFieldValue()) }

    var context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 일기 제목 텍스트 필드
        TextField(
            value = titleState,
            onValueChange = { titleState = it },
            label = { Text("제목을 입력하세요") },
            singleLine = true,
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.Transparent
            )
        )

        // 이미지 (기본값 이미지)
        Image(
            painter = painterResource(id = R.drawable.hhh),
            contentDescription = null,
            modifier = Modifier
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.surface)
                .size(200.dp)
                .align(Alignment.CenterHorizontally),
            contentScale = ContentScale.Crop
        )

        TextField(
            value = mainTextState,
            onValueChange = { mainTextState = it },
            label = { Text("내용을 입력하세요") },
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(8.dp),
            colors = TextFieldDefaults.textFieldColors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )

        // 노래 제목과 가수 텍스트 필드
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = songNameState,
                onValueChange = { songNameState = it },
                label = { Text("음악을 입력하세요") },
                singleLine = true,
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.Transparent
                )
            )
            TextField(
                value = singerNameState,
                onValueChange = { songNameState = it },
                label = { Text("가수를 입력하세요") },
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.Transparent
                )
            )
        }
        // 작성 날짜와 시간 칸
        Text(
            text = "$lastModified",
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBottomAppBar(
    navigationIcon: @Composable () -> Unit,
    actionIcon1: @Composable () -> Unit,
    actionIcon2: @Composable () -> Unit,
    actionIcon3: @Composable () -> Unit,
    actionIcon4: @Composable () -> Unit,
) {
    BottomAppBar(
        contentPadding = PaddingValues(horizontal = 12.dp),
        modifier = Modifier.background(MaterialTheme.colorScheme.primary)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            navigationIcon()
            actionIcon1()
            actionIcon2()
            actionIcon3()
            actionIcon4()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditPreview() {
    MonoDiaryAppTheme {
        HomeScreen2()
    }
}
