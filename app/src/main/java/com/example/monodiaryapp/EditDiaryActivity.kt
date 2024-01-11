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
import androidx.lifecycle.ViewModelProvider
import com.example.monodiaryapp.data.DiaryDao
import com.example.monodiaryapp.data.DiaryDatabase
import com.example.monodiaryapp.ui.theme.MonoDiaryAppTheme
import com.example.monodiaryapp.viewmodel.DiaryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

class EditDiaryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EditDiaryContent(intent, this)
        }
    }
}

@Composable
fun EditDiaryContent(startingIntent: Intent, activity: ComponentActivity) {
    MonoDiaryAppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val context = LocalContext.current
            val database = remember { DiaryDatabase.getDatabase(context) }
            val diaryDao = database.diaryDao()

            val title = startingIntent.getStringExtra("title") ?: ""
            val mainText = startingIntent.getStringExtra("content") ?: ""
            val bgm = startingIntent.getStringExtra("bgm") ?: ""
            val uid = startingIntent.getLongExtra("uid", 0)

            val diaryViewModel = remember {
                ViewModelProvider(activity)[DiaryViewModel::class.java]
            }

            diaryViewModel.initialize(title, mainText, bgm, uid)

            val mediaLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.PickMultipleVisualMedia(),
                onResult = { uris ->
                    for (uri in uris) {
                        // 해당 Uri에 영구적인 권한 부여
                        val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        context.contentResolver.takePersistableUriPermission(uri, flag)
                    }
                    diaryViewModel.updateImageUris(uris)
                }
            )
            ShowDiaryDetailScreen(
                mediaLauncher = mediaLauncher,
                diaryDao = diaryDao,
                diaryViewModel = diaryViewModel
            )
        }
    }
}


@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowDiaryDetailScreen(
    mediaLauncher: ActivityResultLauncher<PickVisualMediaRequest>,
    diaryDao: DiaryDao,
    diaryViewModel: DiaryViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isEditing by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBarWithIcons(
                title = "Today",
                navigationIcon = {
                    IconButton(onClick = {
                        // selectedDiary 는 DiaryEntry 객체를 가르킨다.
                        if (isEditing) {
                            scope.launch(Dispatchers.IO) {
                                val diary =
                                    diaryDao.loadAllByIds(diaryViewModel.uidState.value)
                                diary?.let { selectedDiary ->
                                    selectedDiary.title = diaryViewModel.titleState.value
                                    selectedDiary.content = diaryViewModel.mainTextState.value
                                    selectedDiary.bgm = diaryViewModel.bgmState.value
                                    selectedDiary.image = diaryViewModel.imageUris.value.map { Uri.parse(
                                        it.toString()
                                    ) }
                                    diaryDao.update(selectedDiary)
                                }
                                val intent = Intent(context, HomeActivity::class.java)
                                context.startActivity(intent)
                            }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = "저장"
                        )
                    }
                },
                actionIcon = {
                    IconButton(onClick = {
                        isEditing = !isEditing
                        diaryViewModel.selectedDiary.value?.let {
                            scope.launch(Dispatchers.IO) {
                                val diary =
                                    diaryDao.loadAllByIds(diaryViewModel.uidState.value)
                                diary?.let { selectedDiary ->
                                    selectedDiary.title = diaryViewModel.titleState.value
                                    selectedDiary.content = diaryViewModel.mainTextState.value
                                    selectedDiary.bgm = diaryViewModel.bgmState.value
                                    selectedDiary.image = diaryViewModel.imageUris.value.map { Uri.parse(
                                        it.toString()
                                    ) }
                                    diaryDao.update(selectedDiary)
                                }
                            }
                        }
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
                            scope.launch(Dispatchers.IO) {
                                val diary =
                                    diaryDao.loadAllByIds(diaryViewModel.uidState.value)
                                diary?.let {
                                    diaryDao.delete(diary)
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
                            val copiedText = diaryViewModel.mainTextState.value
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
                    // 제목 필드
                    item {
                        val text1 by diaryViewModel.titleState.collectAsState()
                        TextField(
                            value = text1,
                            onValueChange = { diaryViewModel.updateTitle(it) },
                            label = { Text("일기 제목을 적어보세요!") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface),
                            colors = TextFieldDefaults.textFieldColors(
                                containerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            enabled = isEditing
                        )
                    }
                    // 이미지 필드
                    item {
                        val imageUris by diaryViewModel.imageUris.collectAsState()
                        MultiImageLoader(
                            mediaLauncher = mediaLauncher,
                            selectUris = imageUris,
                            onImagesUpdated = { newImageUris ->
                                diaryViewModel.updateImageUris(newImageUris)
                            }
                        )
                    }
                    // bgm필드
                    item {
                        val text2 by diaryViewModel.bgmState.collectAsState()
                        TextField(
                            value = text2,
                            onValueChange = { diaryViewModel.updateBgm(it) },
                            label = { Text("오늘의 bgm은?") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface),
                            colors = TextFieldDefaults.textFieldColors(
                                containerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            enabled = isEditing
                        )
                    }
                    // 내용 필드
                    item {
                        val text3 by diaryViewModel.mainTextState.collectAsState()
                        TextField(
                            value = text3,
                            onValueChange = {
                                diaryViewModel.updateMainText(it)
                            },
                            label = { Text("일기 내용을 적어보세요 :) ") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface),
                            colors = TextFieldDefaults.textFieldColors(
                                containerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            enabled = isEditing
                        )
                    }
                    // 날짜 필드
                    item {
                        Text(
                            text = diaryViewModel.dateState.value.format(
                                DateTimeFormatter.ofPattern(
                                    "yyyy.MM.dd"
                                )
                            ),
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

@Suppress("DEPRECATION")
@Composable
private fun MultiImageLoader(
    mediaLauncher: ActivityResultLauncher<PickVisualMediaRequest>,
    selectUris: List<Uri?>,
    onImagesUpdated: (List<Uri>) -> Unit
) {
    val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
    val context = LocalContext.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        for (uri in selectUris) {
            uri?.let {
                context.contentResolver.takePersistableUriPermission(uri, flag)
            }

            val bitmap = uri?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(
                        ImageDecoder.createSource(
                            context.contentResolver,
                            uri
                        )
                    )
                } else {
                    MediaStore.Images.Media.getBitmap(
                        context.contentResolver,
                        uri
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                bitmap?.asImageBitmap()?.let {
                    Image(
                        bitmap = it,
                        contentDescription = "선택한 이미지",
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                mediaLauncher.launch(PickVisualMediaRequest())
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
                    mediaLauncher.launch(PickVisualMediaRequest())
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


//@Composable
//private fun MultiImageLoader(
//    mediaLauncher: ActivityResultLauncher<PickVisualMediaRequest>,
//    selectUris: List<Uri?>,
//    onImagesUpdated: (List<Uri>) -> Unit
//) {
//    val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
//    val context = LocalContext.current
//    Row(
//        modifier = Modifier.fillMaxWidth(),
//        horizontalArrangement = Arrangement.SpaceBetween
//    ) {
//        for (uri in selectUris) {
//            uri?.let {
//                context.contentResolver.takePersistableUriPermission(uri, flag)
//            }
//
//            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//                uri?.let {
//                    ImageDecoder.decodeBitmap(
//                        ImageDecoder.createSource(
//                            context.contentResolver,
//                            uri
//                        )
//                    )
//                }
//            } else {
//                uri?.let {
//                    MediaStore.Images.Media.getBitmap(
//                        context.contentResolver,
//                        uri
//                    )
//                }
//            }
//
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//            ) {
//                bitmap?.asImageBitmap()?.let {
//                    Image(
//                        bitmap = it,
//                        contentDescription = "선택한 이미지",
//                        contentScale = ContentScale.FillWidth,
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .clickable {
//                                mediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
//                            }
//                    )
//                }
//            }
//        }
//    }
//    if (selectUris.isEmpty()) {
//        Image(
//            painter = painterResource(id = R.drawable.hhh),
//            contentDescription = "기본 이미지",
//            contentScale = ContentScale.FillWidth,
//            modifier = Modifier
//                .fillMaxWidth()
//                .clickable {
//                    mediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
//                }
//        )
//    }
//    // 이미지 업데이트 후 onImagesUpdated 호출
//    DisposableEffect(selectUris) {
//        val nonNullUris = selectUris.filterNotNull()
//        onImagesUpdated(nonNullUris)
//        onDispose { }
//    }
//}

// 코일 라이브러리 사용해서 이미지 해결하기
