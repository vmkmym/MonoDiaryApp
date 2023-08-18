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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.example.monodiaryapp.data.DiaryDao
import com.example.monodiaryapp.data.DiaryDatabase
import com.example.monodiaryapp.data.DiaryEntry

class HomeActivity : ComponentActivity() {
    private lateinit var diaryDao: DiaryDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MonoDiaryAppTheme {
                val database = remember { DiaryDatabase.getDatabase(this) }
                diaryDao = database.diaryDao()
                HomeScreen(database)
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
                DiaryList(context, database)
            }
        }
    )
}

@Composable
fun DiaryList(context: Context, database: DiaryDatabase) {
    val diaryList by database.diaryDao().getAll().collectAsState(initial = emptyList())

    LazyColumn {
        items(diaryList) { diaryEntry ->
            DiaryItem(diaryEntry) {clickedDiary ->
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
fun DiaryItem(diary: DiaryEntry, onItemClick: (DiaryEntry) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick(diary) },
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
                    text = diary.title.toString(),
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleSmall.copy(fontSize = 20.sp)
                )

                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = diary.content!!.firstLineOrMaxLength(50),
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
                    text = if (diary.date != null) {
                        "작성일: ${formatDateWithDayOfWeek(LocalDate.parse(diary.date))}"
                    } else {
                        "작성일: -"
                    },
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
                )
            }
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
