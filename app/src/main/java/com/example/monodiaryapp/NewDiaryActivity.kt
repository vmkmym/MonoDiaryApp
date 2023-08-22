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
import com.example.monodiaryapp.data.DiaryEntry
import com.example.monodiaryapp.ui.theme.MonoDiaryAppTheme
import com.example.monodiaryapp.viewmodel.DiaryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class NewDiaryActivity : ComponentActivity() {
    private lateinit var diaryDao: DiaryDao
    private lateinit var diaryViewModel: DiaryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MonoDiaryAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // ViewModel을 생성할 때 액티비티나 컴포넌트에서 context를 전달
                    diaryViewModel = ViewModelProvider(this)[DiaryViewModel::class.java]

                    val context = LocalContext.current
                    val database = DiaryDatabase.getDatabase(this)
                    val diaryDao = database.diaryDao()
                    val mediaLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.PickMultipleVisualMedia(),
                        onResult = { uris ->
                            for (uri in uris) {
                                // 해당 Uri에 영구적인 권한을 부여합니다.
                                val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                context.contentResolver.takePersistableUriPermission(uri, flag)
                            }
                            diaryViewModel.updateImageUris(uris)
                        }
                    )
                    EditScreen(
                        mediaLauncher = mediaLauncher,
                        context = context, // val 선언하고 빼기
                        diaryDao = diaryDao,
                        diaryViewModel = diaryViewModel
                    )
                }
            }
            val database = DiaryDatabase.getDatabase(this)
            diaryDao = database.diaryDao()
        }
    }
}


@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(
    mediaLauncher: ActivityResultLauncher<PickVisualMediaRequest>,
    context: Context,
    diaryDao: DiaryDao,
    diaryViewModel: DiaryViewModel
) {
    val scope = rememberCoroutineScope()
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    val lastModified = LocalDate.now()
    val formattedLastModified = lastModified.format(dateFormatter)
    val isModified = remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            MyCenteredTopAppBar2(
                title = "Today",
                navigationIcon = {
                    IconButton(onClick = {
                        diaryViewModel.updateTitle(diaryViewModel.titleState.value)
                        diaryViewModel.updateMainText(diaryViewModel.mainTextState.value)
                        diaryViewModel.updateImageUris(diaryViewModel.imageUris.value)
                        diaryViewModel.updateBgm(diaryViewModel.bgmState.value)

                        val newDiary = DiaryEntry(
                            title = diaryViewModel.titleState.value,
                            content = diaryViewModel.mainTextState.value,
                            image = diaryViewModel.imageUris.value,
                            bgm = diaryViewModel.bgmState.value,
                            date = diaryViewModel.dateState.value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        )

                        scope.launch(Dispatchers.IO) {
                            diaryDao.insertAll(newDiary)
                        }

                        val intent = Intent(context, HomeActivity::class.java)
                        intent.putExtra("title", diaryViewModel.titleState.value)
                        intent.putExtra("selectUris", diaryViewModel.imageUris.value.toTypedArray())
                        intent.putExtra("bgm", diaryViewModel.bgmState.value)
                        intent.putExtra("mainText", diaryViewModel.mainTextState.value)
                        intent.putExtra("date", formattedLastModified)
                        context.startActivity(intent)

                    }) {
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = "저장 버튼"
                        )
                    }
                },
                // 로직
                actionIcon = {
                    IconButton(onClick = {
                        diaryViewModel.selectedDiary.value?.let { selectedDiary ->
                            scope.launch(Dispatchers.IO) {
                                diaryDao.update(selectedDiary) // 선택한 다이어리 수정
                            }
                        }
                        val intent = Intent(context, HomeActivity::class.java)
                        context.startActivity(intent)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Done,
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
                    navigationIcon = {
                        IconButton(onClick = {
                            diaryViewModel.selectedDiary.value?.let { selectedDiary ->
                                scope.launch(Dispatchers.IO) {
                                    diaryDao.delete(selectedDiary) // 선택한 다이어리 삭제
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
                    item {
                        val editMyText1 by diaryViewModel.titleState.collectAsState()
                        TextField(
                            value = editMyText1,
                            onValueChange = { diaryViewModel.updateTitle(it) },
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
                            selectUris = diaryViewModel.imageUris.value,
                            context = context,
                            onImagesUpdated = { newImageUris ->
                                diaryViewModel.updateImageUris(newImageUris)
                            }
                        )
                    }
                    // bgm필드
                    item {
                        val editMyText2 by diaryViewModel.bgmState.collectAsState()
                        TextField(
                            value = editMyText2,
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
                            )
                        )
                    }
                    // 일기 내용 필드
                    item {
                        val editMyText3 by diaryViewModel.mainTextState.collectAsState()
                        TextField(
                            value = editMyText3,
                            onValueChange = {
                                diaryViewModel.updateMainText(it)
                                isModified.value = it.isNotEmpty()
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
                            text = diaryViewModel.dateState.value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
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
private fun MultiImageLoader(
    mediaLauncher: ActivityResultLauncher<PickVisualMediaRequest>,
    selectUris: List<Uri?>,
    context: Context,
    onImagesUpdated: (List<Uri>) -> Unit,
    diaryDao: DiaryDao,
    diaryViewModel: DiaryViewModel
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
    // MultiImageLoader 함수에서 selectUris가 비어있는 경우에 기본 이미지
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

//        val selectedDiary = diaryViewModel.selectedDiary.value
//        val uid = diaryViewModel.uidState.value
//
//        // 이미지 URI 변경 시에만 이미지를 업데이트
//        if (selectedDiary != null) {
//            val updatedDiary = selectedDiary.copy(
//                image = nonNullUris
//            )
//
//            viewModelScope.launch(Dispatchers.IO) {
//                diaryDao.update(updatedDiary)
//            }
//        } else {
//            // If no selected diary, create a new diary entry
//            val newDiary = DiaryEntry(
//                uid = uid,
//                title = diaryViewModel.titleState.value,
//                content = diaryViewModel.mainTextState.value,
//                image = nonNullUris,
//                bgm = diaryViewModel.bgmState.value,
//                date = diaryViewModel.dateState.value.toString()
//            )
//
//            viewModelScope.launch(Dispatchers.IO) {
//                diaryDao.insertAll(newDiary)
//            }
//        }

        onDispose { }
    }
}


