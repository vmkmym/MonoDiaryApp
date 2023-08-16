package com.example.monodiaryapp

import android.content.ClipData
import android.content.ClipboardManager
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
import com.example.monodiaryapp.data.DiaryDao
import com.example.monodiaryapp.data.DiaryDatabase
import com.example.monodiaryapp.data.DiaryEntry
import com.example.monodiaryapp.ui.theme.MonoDiaryAppTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class EditActivity : ComponentActivity() {
    private val mediaLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
            uri?.let { selectedUri ->
                val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
                applicationContext.contentResolver.takePersistableUriPermission(selectedUri, flag)
            }
        }

    private lateinit var diaryDao: DiaryDao // Declare DiaryDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MonoDiaryAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val isScreenClosed = remember { mutableStateOf(false) }

                    val database = DiaryDatabase.getDatabase(this) // Initialize database
                    val diaryDao = database.diaryDao() // Initialize DiaryDao

                    val titleState = remember { mutableStateOf(TextFieldValue()) }
                    val mainTextState = remember { mutableStateOf(TextFieldValue()) }
                    val updatedSelectedImageUris = remember { mutableStateOf(emptyList<Uri>()) }
                    val songNameState = remember { mutableStateOf(TextFieldValue()) }
                    val lastModifiedState = remember { mutableStateOf(LocalDate.now()) }

                    HomeScreen2(
                        mediaLauncher = mediaLauncher,
                        isScreenClosed = isScreenClosed,
                        context = applicationContext,
                        titleState = titleState.value,
                        mainTextState = mainTextState.value,
                        updatedSelectedImageUris = updatedSelectedImageUris.value,
                        songNameState = songNameState.value,
                        lastModifiedState = lastModifiedState,
                        diaryDao = diaryDao,
                    )
                }
            }
            val database = DiaryDatabase.getDatabase(this)
            diaryDao = database.diaryDao() // Initialize DiaryDao
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen2(
    mediaLauncher: ActivityResultLauncher<PickVisualMediaRequest>,
    isScreenClosed: MutableState<Boolean>,
    context: Context,
    titleState: TextFieldValue,
    mainTextState: TextFieldValue,
    updatedSelectedImageUris: List<Uri>,
    songNameState: TextFieldValue,
    lastModifiedState: MutableState<LocalDate>,
    diaryDao: DiaryDao,
) {
    val selectedImageUris: MutableState<List<Uri>> = remember { mutableStateOf(emptyList()) }
    val mainTextState by remember { mutableStateOf(TextFieldValue()) }

    Scaffold(
        topBar = {
            MyCenteredTopAppBar2(
                title = "Today",
                // 뒤로 가기 (내용저장, 리스트랑 연결)
                navigationIcon = {
                    IconButton(onClick = {

                        val newDiary = DiaryEntry(
                            title = titleState.text,
                            content = mainTextState.text,
                            image = updatedSelectedImageUris.firstOrNull()
                                ?.toString(), // Get the first image's path
                            songTitle = songNameState.text,
                            date = lastModifiedState.value.toString()
                        )
                        diaryDao.insertAll(newDiary)

                        val intent = Intent(context, HomeActivity::class.java)
                        context.startActivity(intent)
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "저장 버튼"
                        )
                    }
                },
                // 사용자 프로필로 이동
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
                    // 내용 저장하지 않고 화면 닫기
                    navigationIcon = {
                        IconButton(onClick = {
                            isScreenClosed.value = true
                        }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "삭제"
                            )
                        }
                    },
                    // 본문 내용 복사하기 (복사 완료 카드가 안 예쁨 -> 수정하기)
                    actionIcon1 = {
                        IconButton(onClick = {
                            // mainTextState의 내용을 복사
                            val copiedText = mainTextState.text
                            // 복사된 내용을 ClipboardManager를 사용하여 클립보드에 복사
                            val clipboard =
                                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Copied Text", copiedText)
                            clipboard.setPrimaryClip(clip)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "내용복사"
                            )
                        }
                    },
                    // 이 버튼을 누르면 변동 사항 저장된 후 갤러리로 이동
                    actionIcon2 = {
                        IconButton(onClick = {
                            // 갤러리를 만들고 하기?
                        }) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = "갤러리로 이동"
                            )
                        }
                    },
                    // 내용 수정 버튼을 눌러야 수정을 할 수 있음 (또 누를 필요는 없음)
                    actionIcon3 = {
                        IconButton(onClick = {
                            lastModifiedState.value = LocalDate.now()
                            // 이 버튼을 누르면 내용을 수정할 수 있음
                            // 처음
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
        content = { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                EditDiaryScreen(
                    lastModified = lastModifiedState.value,
                    selectedImageUris = selectedImageUris.value,
                    launcher = mediaLauncher,
                    isScreenClosed = isScreenClosed,
                    context = context,
                    titleStat = titleState,
                    mainTextState = mainTextState,
                    updatedSelectedImageUris = updatedSelectedImageUris.firstOrNull()?.toString(), // Pass the selectedImageUris state
                    songNameState = songNameState,
                )
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
    context: Context,
    titleStat: TextFieldValue,
    mainTextState: TextFieldValue,
    songNameState: TextFieldValue,

) {
    var titleState by remember { mutableStateOf(TextFieldValue()) }
    var mainTextState by remember { mutableStateOf(TextFieldValue()) }
    var songNameState by remember { mutableStateOf(TextFieldValue()) }
    var updatedSelectedImageUris by remember { mutableStateOf(selectedImageUris) }

    val db = remember {
        DiaryDatabase.getDatabase(context)
    }
    val scope = rememberCoroutineScope()

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

        val usersList by db.diaryDao().getAll().collectAsState(initial = emptyList())

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
        val isScreenClosed = remember { mutableStateOf(false) }

        val database = DiaryDatabase.getDatabase(LocalContext.current) // Initialize database
        val diaryDao = database.diaryDao() // Initialize DiaryDao

        val titleState = remember { mutableStateOf(TextFieldValue()) }
        val mainTextState = remember { mutableStateOf(TextFieldValue()) }
        val updatedSelectedImageUris = remember { mutableStateOf(emptyList<Uri>()) }
        val songNameState = remember { mutableStateOf(TextFieldValue()) }
        val lastModifiedState = remember { mutableStateOf(LocalDate.now()) }

        HomeScreen2(
            mediaLauncher = mediaLauncher,
            isScreenClosed = isScreenClosed,
            context = LocalContext.current,
            titleState = titleState.value,
            mainTextState = mainTextState.value,
            updatedSelectedImageUris = updatedSelectedImageUris.value,
            songNameState = songNameState.value,
            lastModifiedState = lastModifiedState,
            diaryDao = diaryDao,
        )
    }
}
