package com.example.monodiaryapp

import android.content.Context
import android.content.Intent
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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.monodiaryapp.ui.theme.MonoDiaryAppTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.example.monodiaryapp.data.DiaryDao
import com.example.monodiaryapp.data.DiaryDatabase

class HomeActivity : ComponentActivity() {
    private lateinit var diaryDao: DiaryDao // Declare DiaryDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = DiaryDatabase.getDatabase(this)
        diaryDao = database.diaryDao()

        setContent {
            MonoDiaryAppTheme {
                HomeScreen()
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
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 28.sp) // 폰트 크기를 더 키움
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
fun HomeScreen() {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            MyCenteredTopAppBar(
                title = "Home",
                navigationIcon = {
                    IconButton(onClick = { /* doSomething() */ }) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = "Localized description"
                        )
                    }
                },
                actionIcon = {
                    IconButton(
                        onClick = {
                            val intent = Intent(context, EditActivity::class.java)
                            context.startActivity(intent)
                        }) {
                        Icon(
                            imageVector = Icons.Filled.AddCircle,
                            contentDescription = "Localized description"
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
                DiaryList(context)
            }
        }
    )
}

@Composable
fun DiaryList(context: Context) {
    val diaryList = listOf(
        Diary("8월 17일 목요일", "일기 본문 첫 줄...", "", "", LocalDate.parse("2023-08-05")),
        Diary("8월 16일 수요일", "일기 본문 첫 줄...", "", "", LocalDate.parse("2023-08-04")),
        ).sortedByDescending { it.date }

    LazyColumn {
        items(diaryList) { diary ->
            DiaryItem(diary) { clickedDiary ->
                // 일기 화면으로 이동하는 코드 추가
                val intent = Intent(context, DiaryDetailActivity::class.java).apply {
                    putExtra("title", clickedDiary.title)
                    putExtra("content", clickedDiary.content)
                    putExtra("songTitle", clickedDiary.songTitle)
                    putExtra("artist", clickedDiary.artist)
                    putExtra("date", clickedDiary.date.toString())
                }
                context.startActivity(intent)
            }
        }
    }
}

@Composable
fun DiaryItem(diary: Diary, onItemClick: (Diary) -> Unit) {
    val context: Context = LocalContext.current // 클릭 핸들러 밖에서 context 추출
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = {
                onItemClick(diary)
                val intent = Intent(context, DiaryDetailActivity::class.java)
                intent.putExtra("title", diary.title)
                intent.putExtra("content", diary.content)
                intent.putExtra("songTitle", diary.songTitle)
                intent.putExtra("artist", diary.artist)
                intent.putExtra("date", diary.date.toString())
                context.startActivity(intent)
            }),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier
                .background(Color.White)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ImagePreview()

            Spacer(modifier = Modifier.width(10.dp))
            Column(
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = diary.title,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleSmall.copy(fontSize = 20.sp)
                )

                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = diary.content.firstLineOrMaxLength(50),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 16.sp)
                )

                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = "${diary.songTitle} - ${diary.artist}",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
                )

                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "작성일: ${formatDateWithDayOfWeek(diary.date)}",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
                )
            }
        }
    }
}

// 일정 글자 수 이하의 본문만 표시하는 firstLineOrMaxLength()
fun String.firstLineOrMaxLength(maxLength: Int): String {
    val lines = this.lines()
    return if (lines.isEmpty()) {
        ""
    } else {
        val firstLine = lines[0]
        if (firstLine.length > maxLength) {
            firstLine.substring(0, maxLength) + "..." // 일정 글자수까지만 표시하고 ... 추가
        } else {
            firstLine
        }
    }
}

// 일기 목록 리스트에 표시 될 항목
data class Diary(
    val title: String,
    val content: String,
    val songTitle: String,
    val artist: String,
    val date: LocalDate
)

// 날짜 포맷
fun formatDateWithDayOfWeek(date: LocalDate): String {
    val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    val formattedDate = date.format(DateTimeFormatter.ofPattern("yy.MM.dd"))
    return "$formattedDate $dayOfWeek"
}

// 이미지 동기화 하기 위한 함수
@Composable
fun ImagePreview() {
    Image(
        painter = painterResource(id = R.drawable.hhh), // 공통 이미지 리소스 사용
        contentDescription = null,
        modifier = Modifier
            .padding(16.dp)
            .size(120.dp)
            .clip(CircleShape),
        contentScale = ContentScale.Crop
    )
}
