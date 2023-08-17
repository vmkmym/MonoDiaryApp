@file:Suppress("NAME_SHADOWING")

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
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
                    val updatedSelectedImageUris= remember { mutableStateOf(mutableListOf<Uri>()) }
                    val songNameState = remember { mutableStateOf(TextFieldValue()) }
                    val lastModifiedState = remember { mutableStateOf(LocalDate.now()) }

                    HomeScreen2(
                        mediaLauncher = mediaLauncher,
                        isScreenClosed = isScreenClosed,
                        context = applicationContext,
                        titleState = titleState.value,
                        mainTextState = mainTextState.value,
                        updatedSelectedImageUris = updatedSelectedImageUris,
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
    updatedSelectedImageUris: MutableState<MutableList<Uri>>,
    songNameState: TextFieldValue,
    lastModifiedState: MutableState<LocalDate>,
    diaryDao: DiaryDao,
) {
    val selectedImageUris: MutableState<MutableList<Uri>> = remember { mutableStateOf(mutableListOf()) }

    Scaffold(
        topBar = {
            MyCenteredTopAppBar2(
                title = "Today",
                // 뒤로 가기 버튼을 누르면 db에 새 다이어리가 저장되고 화면 전환
                navigationIcon = {
                    IconButton(onClick = {
                        val newDiary = DiaryEntry(
                            title = titleState.text,
                            content = mainTextState.text,
                            image = updatedSelectedImageUris.toString(), // Get the first image's path
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
                // 수정 버튼을 누르면 해당 다이어리의 내용 업데이트
                actionIcon = {
                    IconButton(onClick = {
                        val updatedDiary = DiaryEntry(
                            title = titleState.text,
                            content = mainTextState.text,
                            image = updatedSelectedImageUris.toString(),
                            songTitle = songNameState.text,
                            date = lastModifiedState.value.toString()
                        )
                        diaryDao.update(updatedDiary)
                    }) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "수정"
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
                    // 해당 다이어리 삭제 후 화면 전환
                    navigationIcon = {
                        IconButton(onClick = {
                            val diaryToDelete = DiaryEntry() // 다이어리의 고유 ID
                            diaryDao.delete(diaryToDelete)
                            isScreenClosed.value = true
                        }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "삭제"
                            )
                        }
                    },
                    actionIcon1 = {
                        IconButton(onClick = {
                            val copiedText = mainTextState.text
                            val clipboard =
                                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Copied Text", copiedText)
                            clipboard.setPrimaryClip(clip)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "복사"
                            )
                        }
                    },
                    actionIcon2 = {
                        IconButton(onClick = {
                            // 나중에..
                        }) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = "갤러리로 이동"
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
                    updatedSelectedImageUris = updatedSelectedImageUris.value,
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
    updatedSelectedImageUris: MutableList<Uri>,
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
        val diaryList by db.diaryDao().getAll().collectAsState(initial = emptyList())

        LazyColumn {
            items(diaryList) {diaryEntry ->
                DiaryItemEntry(entry = diaryEntry)
            }
        }
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

@Composable
fun DiaryItemEntry(entry: DiaryEntry) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = entry.title.toString(),
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = entry.content.toString(),
            fontSize = 16.sp,
            color = Color.Gray,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
        // 추가적인 다이어리 항목 UI 요소 추가 가능
    }
}


@Preview(showBackground = true)
@Composable
fun HomeScreen2Preview() {
    val mediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { result ->
            // 뭘 넣어야 하지
        }
    )
    val isScreenClosedState = remember { mutableStateOf(false) } // remember를 사용하여 상태 정의

    MonoDiaryAppTheme {
        val isScreenClosed = remember { mutableStateOf(false) }

        val database = DiaryDatabase.getDatabase(LocalContext.current) // Initialize database
        val diaryDao = database.diaryDao() // Initialize DiaryDao

        val titleState = remember { mutableStateOf(TextFieldValue()) }
        val mainTextState = remember { mutableStateOf(TextFieldValue()) }
        val updatedSelectedImageUris = remember { mutableStateOf(mutableListOf<Uri>()) }
        val songNameState = remember { mutableStateOf(TextFieldValue()) }
        val lastModifiedState = remember { mutableStateOf(LocalDate.now()) }

        HomeScreen2(
            mediaLauncher = mediaLauncher,
            isScreenClosed = isScreenClosed,
            context = LocalContext.current,
            titleState = titleState.value,
            mainTextState = mainTextState.value,
            updatedSelectedImageUris = updatedSelectedImageUris,
            songNameState = songNameState.value,
            lastModifiedState = lastModifiedState,
            diaryDao = diaryDao,
        )
    }
}
