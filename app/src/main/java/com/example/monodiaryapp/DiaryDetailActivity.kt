package com.example.monodiaryapp

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
import androidx.compose.material.icons.filled.AccountCircle
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
import com.example.monodiaryapp.data.DiaryDao
import com.example.monodiaryapp.data.DiaryDatabase
import com.example.monodiaryapp.data.DiaryEntry
import com.example.monodiaryapp.ui.theme.MonoDiaryAppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DiaryDetailActivity : ComponentActivity() {
    private lateinit var diaryDao: DiaryDao
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MonoDiaryAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current
                    val database = DiaryDatabase.getDatabase(this)
                    val diaryDao = database.diaryDao()
                    var selectUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
                    val mediaLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.PickMultipleVisualMedia(),
                        onResult = { uris ->
                            selectUris = uris
                            val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
                            for (uri in selectUris) {
                                context.contentResolver.takePersistableUriPermission(uri, flag)
                            }
                        }
                    )

                    val title = intent.getStringExtra("title") ?: ""
                    val mainText = intent.getStringExtra("content") ?: ""
                    val bgm = intent.getStringExtra("bgm") ?: ""
                    val date = intent.getStringExtra("date") ?: ""

                    ShowDiaryDetailScreen(
                        mediaLauncher = mediaLauncher,
                        context = context,
                        selectUris = selectUris,
                        diaryDao = diaryDao,
                        titleState = title,
                        mainTextState = mainText,
                        bgmState = bgm,
                        dateState = date
                    )
                }
            }
//            val database = DiaryDatabase.getDatabase(this)
//            diaryDao = database.diaryDao()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowDiaryDetailScreen(
    mediaLauncher: ActivityResultLauncher<PickVisualMediaRequest>,
    context: Context,
    selectUris: List<Uri>,
    diaryDao: DiaryDao,
    titleState: String,
    mainTextState: String,
    bgmState: String,
    dateState: String,
) {
    val scope = rememberCoroutineScope()
    var titleState by remember { mutableStateOf("") }
    var mainTextState by remember { mutableStateOf("") }
    var bgmState by remember { mutableStateOf("") }
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val lastModified = LocalDate.now()
    val formattedLastModified = lastModified.format(dateFormatter)
    val dateState = formattedLastModified

    Scaffold(
        topBar = {
            TopAppBarWithIcons(
                title = "Today",
                navigationIcon = {
                    IconButton(onClick = {
                        val newDiary = DiaryEntry(
                            title = titleState,
                            content = mainTextState,
                            image = selectUris,
                            bgm = bgmState,
                            date = formattedLastModified,
                        )
                        scope.launch(Dispatchers.IO) {
                            diaryDao.insertAll(newDiary)
                        }
                        val intent = Intent(context, HomeActivity::class.java)
                        context.startActivity(intent)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = "저장 버튼"
                        )
                    }
                },
                actionIcon = {
                    IconButton(onClick = {
                        val updatedDiary = DiaryEntry(
                            title = titleState,
                            content = mainTextState,
                            image = selectUris,
                            bgm = bgmState,
                            date = formattedLastModified,
                        )
                        scope.launch(Dispatchers.IO) {
                            diaryDao.update(updatedDiary)
                        }
                        val intent = Intent(context, HomeActivity::class.java)
                        context.startActivity(intent)
                    }) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "수정 버튼"
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
                            val diaryToDelete = DiaryEntry(
                                title = titleState,
                                content = mainTextState,
                                image = selectUris,
                                bgm = bgmState,
                                date = formattedLastModified
                            )
                            scope.launch(Dispatchers.IO) { diaryDao.delete(diaryToDelete) }
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
                            val copiedText = mainTextState
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
                        TextField(
                            value = titleState,
                            onValueChange = { titleState = it },
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
                            selectUris = selectUris,
                            context = context
                        )
                    }
                    // bgm필드
                    item {
                        TextField(
                            value = bgmState,
                            onValueChange = { bgmState = it },
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
                    }
                    item {
                        Text(
                            text = formattedLastModified.toString(),
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
    context: Context
) {
    if (selectUris.isNotEmpty()) {

        for (uri in selectUris) {

            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(
                    ImageDecoder.createSource(
                        context.contentResolver,
                        uri!!
                    )
                )
            } else {
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri!!)
            }
            Image(
                bitmap = bitmap.asImageBitmap(), contentDescription = "",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        mediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
            )
        }

    } else {
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
}
