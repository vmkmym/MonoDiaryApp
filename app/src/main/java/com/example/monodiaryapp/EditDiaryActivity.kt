package com.example.monodiaryapp

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
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
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.monodiaryapp.data.DiaryDao
import com.example.monodiaryapp.data.DiaryDatabase
import com.example.monodiaryapp.data.DiaryEntry
import com.example.monodiaryapp.ui.theme.MonoDiaryAppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class EditDiaryActivity : ComponentActivity() {
    private lateinit var diaryDao: DiaryDao
    private lateinit var diaryDetailViewModel: DiaryDetailViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MonoDiaryAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current
                    val database = remember { DiaryDatabase.getDatabase(context) }
                    val diaryDao = database.diaryDao()

                    val title = intent.getStringExtra("title") ?: ""
                    val mainText = intent.getStringExtra("content") ?: ""
                    val bgm = intent.getStringExtra("bgm") ?: ""

                    diaryDetailViewModel = ViewModelProvider(this)[DiaryDetailViewModel::class.java]
                    diaryDetailViewModel.initialize(title, mainText, bgm)

                    val mediaLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.PickMultipleVisualMedia(),
                        onResult = { uris ->
                            for (uri in uris) {
                                // 해당 Uri에 영구적인 권한을 부여합니다.
                                val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                context.contentResolver.takePersistableUriPermission(uri, flag)
                            }
                            diaryDetailViewModel.updateImageUris(uris)
                        }
                    )
                    ShowDiaryDetailScreen(
                        mediaLauncher = mediaLauncher,
                        context = context,
                        diaryDao = diaryDao,
                        diaryDetailViewModel = diaryDetailViewModel
                    )
                }
            }
            val database = DiaryDatabase.getDatabase(this)
            diaryDao = database.diaryDao()
        }
    }
}

class DiaryDetailViewModel: ViewModel() {
    private val _titleState = MutableStateFlow("")
    val titleState: StateFlow<String> = _titleState

    private val _mainTextState = MutableStateFlow("")
    val mainTextState: StateFlow<String> = _mainTextState

    private val _bgmState = MutableStateFlow("")
    val bgmState: StateFlow<String> = _bgmState

    private val _imageUris = MutableStateFlow<List<Uri>>(emptyList())
    val imageUris: StateFlow<List<Uri>> = _imageUris

    private val _date = MutableStateFlow(LocalDate.now())
    val date: StateFlow<LocalDate> = _date

    private val _selectedDiary = MutableStateFlow<DiaryEntry?>(null)
    val selectedDiary: StateFlow<DiaryEntry?> = _selectedDiary

    fun initialize(initialTitle: String, initialMainText: String, initialBgm: String) {
        _titleState.value = initialTitle
        _mainTextState.value = initialMainText
        _bgmState.value = initialBgm
    }
    fun updateTitle(newTitle: String) {
        _titleState.value = newTitle
    }

    fun updateMainText(newMainText: String) {
        _mainTextState.value = newMainText
    }

    fun updateBgm(newBgm: String) {
        _bgmState.value = newBgm
    }

    fun updateImageUris(newImageUris: List<Uri>) {
        _imageUris.value = newImageUris
    }
    fun updateDate(newDate: LocalDate) {
        _date.value = newDate
    }

    fun selectDiary(diaryEntry: DiaryEntry) {
        _selectedDiary.value = diaryEntry
    }

    fun clearSelectedDiary() {
        _selectedDiary.value = null
    }
}


@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowDiaryDetailScreen(
    mediaLauncher: ActivityResultLauncher<PickVisualMediaRequest>,
    context: Context,
    diaryDao: DiaryDao,
    diaryDetailViewModel: DiaryDetailViewModel
) {
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBarWithIcons(
                title = "Today",
                navigationIcon = {
                    IconButton(onClick = {
                        diaryDetailViewModel.selectedDiary.value?.let { selectedDiary ->
                            scope.launch(Dispatchers.IO) {
                                diaryDao.update(selectedDiary)
                            }
                        }
                        val intent = Intent(context, HomeActivity::class.java)
                        intent.putExtra("diaryUid", diaryDetailViewModel.selectedDiary.value?.uid)
                        context.startActivity(intent)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = "저장"
                        )
                    }
                },
                actionIcon = {
                    IconButton(onClick = {
                        diaryDetailViewModel.selectedDiary.value?.let { selectedDiary ->
                            scope.launch(Dispatchers.IO) {
                                diaryDao.update(selectedDiary)
                            }
                        }
                        val intent = Intent(context, HomeActivity::class.java)
                        intent.putExtra("diaryUid", diaryDetailViewModel.selectedDiary.value?.uid)
                        context.startActivity(intent)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
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
                BottomAppBarContent(
                    navigationIcon = {
                        IconButton(onClick = {
                            diaryDetailViewModel.selectedDiary.value?.let { selectedDiary ->
                                scope.launch(Dispatchers.IO) {
                                    diaryDao.delete(selectedDiary)
                                }
                            }
                            val intent = Intent(context, HomeActivity::class.java)
                            context.startActivity(intent)
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.delete),
                                modifier = Modifier.size(24.dp),
                                contentDescription = "삭제"
                            )
                        }
                    },
                    actionIcon1 = {
                        IconButton(onClick = {
                            val copiedText = diaryDetailViewModel.mainTextState.value
                            val clipboard =
                                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Copied Text", copiedText)
                            clipboard.setPrimaryClip(clip)
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.copy),
                                modifier = Modifier.size(24.dp),
                                contentDescription = "복사"
                            )
                        }
                    },
                    actionIcon2 = {
                        IconButton(onClick = {
                            val intent = Intent(context, GalleryActivity::class.java)
                            context.startActivity(intent)
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.photoicon),
                                modifier = Modifier.size(24.dp),
                                contentDescription = "갤러리로 이동"
                            )
                        }
                    }
                )
            }
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                LazyColumn {
                    item {
                        val text1 by diaryDetailViewModel.titleState.collectAsState()
                        TextField(
                            value = text1,
                            onValueChange = { diaryDetailViewModel.updateTitle(it) },
                            label = { Text("일기 제목을 적어보세요!") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface),
                            colors = TextFieldDefaults.textFieldColors(
                                containerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    }
                    // 이미지 필드
                    item {
                        MultiImageLoader(
                            mediaLauncher = mediaLauncher,
                            selectUris = diaryDetailViewModel.imageUris.value,
                            context = context,
                            onImagesUpdated = { newImageUris ->
                                diaryDetailViewModel.updateImageUris(newImageUris)
                            }
                        )
                    }

                    // bgm필드
                    item {
                        val text2 by diaryDetailViewModel.titleState.collectAsState()
                        TextField(
                            value = text2,
                            onValueChange = { diaryDetailViewModel.updateBgm(it) },
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
                    }
                    // 일기 내용 필드
                    item {
                        val text3 by diaryDetailViewModel.titleState.collectAsState()
                        TextField(
                            value = text3,
                            onValueChange = { diaryDetailViewModel.updateMainText(it)
                            },
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
                    }
                    item {
                        Text(
                            text = diaryDetailViewModel.date.value.format(DateTimeFormatter.ofPattern("yyyy.MM.dd")),
                            fontStyle = FontStyle.Italic,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(8.dp)
                        )
                    }
                }
            }
        }
    )
}


@Composable
fun BottomAppBarContent(
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
fun TopAppBarWithIcons(
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
private fun MultiImageLoader(
    mediaLauncher: ActivityResultLauncher<PickVisualMediaRequest>,
    selectUris: List<Uri?>,
    context: Context,
    onImagesUpdated: (List<Uri>) -> Unit
) {
    val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        for (uri in selectUris) {
            uri?.let {
                context.contentResolver.takePersistableUriPermission(uri, flag)
            }

            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                uri?.let {
                    ImageDecoder.decodeBitmap(
                        ImageDecoder.createSource(
                            context.contentResolver,
                            uri
                        )
                    )
                }
            } else {
                uri?.let {
                    MediaStore.Images.Media.getBitmap(
                        context.contentResolver,
                        uri
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.3f)
                    .aspectRatio(1f)
            ) {
                bitmap?.asImageBitmap()?.let {
                    Image(
                        bitmap = it,
                        contentDescription = "선택한 이미지",
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                mediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }
                    )
                }
            }
        }
    }
    if (selectUris.isEmpty()) {
        Image(
            painter = painterResource(id = R.drawable.hhh),
            contentDescription = "기본 이미지",
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    mediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
        )
    }
    // 이미지 업데이트 후 onImagesUpdated 호출
    DisposableEffect(selectUris) {
        val nonNullUris = selectUris.filterNotNull()
        onImagesUpdated(nonNullUris)
        onDispose { }
    }
}

