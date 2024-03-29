package com.example.monodiaryapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import coil.request.ImageRequest
import com.example.monodiaryapp.data.DiaryDatabase
import com.example.monodiaryapp.data.DiaryEntry
import com.example.monodiaryapp.ui.theme.MonoDiaryAppTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HomeContent()
        }
    }
}

@Composable
fun HomeContent() {
    val context = LocalContext.current
    val database = DiaryDatabase.getDatabase(context)

    MonoDiaryAppTheme {
        HomeScreen(database)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(database: DiaryDatabase) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            MyCenteredTopAppBar(
                title = "Home",
                navigationIcon = {
                    IconButton(onClick = { /* doSomething() */ }) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = "메뉴"
                        )
                    }
                },
                actionIcon = {
                    IconButton(
                        onClick = {
                            val intent = Intent(context, NewDiaryActivity::class.java)
                            context.startActivity(intent)
                        }) {
                        Icon(
                            imageVector = Icons.Filled.AddCircle,
                            contentDescription = "다이어리 생성"
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
                DiaryList(context, database)
            }
        }
    )
}

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun DiaryList(context: Context, database: DiaryDatabase) {
    val diaryListFlow = database.diaryDao().getAll()
    val diaryListState by diaryListFlow.collectAsState(initial = emptyList())

    LazyColumn {
        items(diaryListState) { diaryEntry ->
            DiaryItem(diaryEntry, onItemClick = { clickedDiary: DiaryEntry ->
                val intent = Intent(context, EditDiaryActivity::class.java).apply {
                    putExtra("title", clickedDiary.title)
                    putExtra("bgm", clickedDiary.bgm)
                    putExtra("content", clickedDiary.content)
                    putExtra("date", clickedDiary.date)
                    putExtra("uid", clickedDiary.uid)
                }
                context.startActivity(intent)
            })
        }
    }
}

@Composable
fun DiaryItem(
    diary: DiaryEntry,
    onItemClick: (DiaryEntry) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick(diary) },
        shape = MaterialTheme.shapes.large

    ) {
        Row(
            modifier = Modifier
                .background(Color.White)
                .fillMaxWidth()
                .clickable { onItemClick(diary) },
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 이미지 미리보기 표시, 코일 라이브러리 사용, 타입 컨버터 db, entity
            ImagePreview(diary.image)

            Spacer(modifier = Modifier.width(10.dp))
            Column(
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = diary.title,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleSmall.copy(fontSize = 20.sp)
                )

                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = diary.content.firstLineOrMaxLength(50),
                    maxLines = 1,
                    fontWeight = FontWeight.Bold,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 16.sp)
                )

                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = diary.bgm,
                    maxLines = 1,
                    fontWeight = FontWeight.Bold,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
                )

                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "작성일: ${formatDateWithDayOfWeek(LocalDate.parse(diary.date))}",
                    maxLines = 1,
                    fontWeight = FontWeight.Bold,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyCenteredTopAppBar(
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

fun formatDateWithDayOfWeek(date: LocalDate): String {
    val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    val formattedDate = date.format(DateTimeFormatter.ofPattern("yy.MM.dd"))
    return "$formattedDate $dayOfWeek"
}

// 이미지 미리보기
@Composable
fun ImagePreview(imageList: List<Uri>) {
    val firstImage = imageList.firstOrNull() // 첫 번째 이미지만 표시
    val defaultImageResId = R.drawable.hhh

    if (firstImage != null) {
        val imagePainter = rememberAsyncImagePainter(
            ImageRequest.Builder(LocalContext.current).data(data = firstImage).apply {
                fallback(defaultImageResId)
            }.build()
        )

        Image(
            painter = imagePainter,
            contentDescription = "이미지 미리보기",
            modifier = Modifier
                .padding(16.dp)
                .size(120.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    } else {
        Image(
            painter = painterResource(id = defaultImageResId),
            contentDescription = "기본 이미지",
            modifier = Modifier
                .padding(16.dp)
                .size(120.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    }
}

// 일정 글자 수 이하의 본문만 표시 하는 firstLineOrMaxLength()
fun String.firstLineOrMaxLength(maxLength: Int): String {
    val lines = this.lines()
    return if (lines.isEmpty()) {
        ""
    } else {
        val firstLine = lines[0]
        if (firstLine.length > maxLength) {
            firstLine.substring(0, maxLength) + "..."
        } else {
            firstLine
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHomeContent() {
    MonoDiaryAppTheme {
        HomeContent()
    }
}