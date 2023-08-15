package com.example.monodiaryapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.monodiaryapp.ui.theme.MonoDiaryAppTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class EditActivity : ComponentActivity() {
    private val mediaLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        uri?.let { selectedUri ->
            val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
            applicationContext.contentResolver.takePersistableUriPermission(selectedUri, flag)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MonoDiaryAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val isScreenClosed = remember { mutableStateOf(false) }
                    HomeScreen2(mediaLauncher, isScreenClosed, this)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen2(
    mediaLauncher: ActivityResultLauncher<PickVisualMediaRequest>,
    isScreenClosed: MutableState<Boolean>,
    context: Context
) {
    // 마지막 수정 상태 기억
    val lastModifiedState: MutableState<LocalDate> = remember { mutableStateOf((LocalDate.now())) }
    // 이미지 Uri 상태
    val selectedImageUris: MutableState<List<Uri>> = remember { mutableStateOf(emptyList()) }

    Scaffold(
        topBar = {
            MyCenteredTopAppBar2(
                title = "Today",
                navigationIcon = {
                    IconButton(onClick = {
                        val intent = Intent(context, HomeActivity::class.java)
                        context.startActivity(intent)
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Localized description"
                        )
                    }
                },
                actionIcon = {
                    IconButton(onClick = { /*
                        val intent = Intent(context, UserActivity::class.java)
                        context.startActivity(intent)
                    사용자 프로필 액티비티로 이동 */
                    }) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Localized description"
                        )
                    }
                }
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary)
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                MyBottomAppBar(
                    navigationIcon = {
                        IconButton(onClick = {
                            // 내용 저장하지 않고 화면 닫기
                            isScreenClosed.value = true
                        }) {
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
                                contentDescription = "내용복사"
                            )
                        }
                    },
                    actionIcon2 = {
                        IconButton(onClick = { /* 현재 내용 자동 저장 후 갤러리로 화면 전환 */ }) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = "갤러리로 이동"
                            )
                        }
                    },
                    actionIcon3 = {
                        IconButton(onClick = {
                            lastModifiedState.value = LocalDate.now()
                        }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Create,
                                contentDescription = "내용 수정"
                            )
                        }
                    }
                )
            }
        },
        // 레이지 컬럼으로 해야할 듯?
        content = { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                EditDiaryScreen(lastModifiedState.value, selectedImageUris.value, mediaLauncher, isScreenClosed, context)
            }
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDiaryScreen(
    lastModified: LocalDate,
    selectedImageUris: List<Uri>,
    launcher: ActivityResultLauncher<PickVisualMediaRequest>,
    isScreenClosed: MutableState<Boolean>,
    context: Context
) {
    var titleState by remember { mutableStateOf(TextFieldValue()) }
    var mainTextState by remember { mutableStateOf(TextFieldValue()) }
    var songNameState by remember { mutableStateOf(TextFieldValue()) }

    var updatedSelectedImageUris by remember { mutableStateOf(selectedImageUris) }

    if (isScreenClosed.value) {
        // isScreenClosed 값이 true일 경우 화면을 닫고 HomeActivity로 이동
        val intent = Intent(context, HomeActivity::class.java)
        context.startActivity(intent)
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // 일기 제목 텍스트 필드
        TextField(
            value = titleState,
            onValueChange = { titleState = it },
            label = { Text("일기 제목을 적어보세요!") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )
        // bgm 텍스트 필드
        TextField(
            value = songNameState,
            onValueChange = { songNameState = it },
            label = { Text("오늘의 bgm은?") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )
        // 이미지 가져오기
        ImageList(
            selectedImageUris = updatedSelectedImageUris,
            onImageSelected = { selectedUri ->
                updatedSelectedImageUris = updatedSelectedImageUris.toMutableList().apply {
                    add(selectedUri)
                }
            },
            launcher = launcher
        )

        TextField(
            value = mainTextState,
            onValueChange = { mainTextState = it },
            label = { Text("일기 내용을 적어보세요 :) ") },
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )

        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formattedLastModified = lastModified.format(dateFormatter)

        Text(
            text = lastModified.toString(),
            fontStyle = FontStyle.Italic,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(8.dp)
        )
    }
}


@Composable
fun MyBottomAppBar(
    navigationIcon: @Composable () -> Unit,
    actionIcon1: @Composable () -> Unit,
    actionIcon2: @Composable () -> Unit,
    actionIcon3: @Composable () -> Unit,
) {
    BottomAppBar(
        contentPadding = PaddingValues(horizontal = 10.dp),
        modifier = Modifier.background(MaterialTheme.colorScheme.primary)
        ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            navigationIcon()
            actionIcon1()
            actionIcon2()
            actionIcon3()
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


@Composable
fun ImageList(
    selectedImageUris: List<Uri>,
    onImageSelected: (Uri) -> Unit, // onImageSelected 함수 파라미터 추가
    launcher: ActivityResultLauncher<PickVisualMediaRequest>
) {
    val imageModifier = Modifier
        .clickable {
            launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
        }
        .padding(10.dp)
        .background(MaterialTheme.colorScheme.surface)
        .fillMaxWidth()

    LazyColumn {
        item {
            // 이미지 (기본값 이미지 또는 선택한)
            if (selectedImageUris.isNotEmpty()) {
                selectedImageUris.forEachIndexed { index, uri ->
                    Image(
                        painter = painterResource(id = R.drawable.hhh),
                        contentDescription = null,
                        modifier = imageModifier.clickable { onImageSelected(uri) },
                        contentScale = ContentScale.Crop
                    )
                }
            } else {
                // 기본 이미지 리소스 사용
                val imagePainter = painterResource(id = R.drawable.hhh)

                Image(
                    painter = imagePainter,
                    contentDescription = null,
                    modifier = imageModifier.clickable {
                        launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
                    },
                    contentScale = ContentScale.FillWidth
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreen2Preview() {
    val mediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { result ->
            // ...
        }
    )
    val isScreenClosedState = remember { mutableStateOf(false) } // remember를 사용하여 상태 정의

    MonoDiaryAppTheme {
        HomeScreen2(mediaLauncher, isScreenClosedState, LocalContext.current)
    }
}
