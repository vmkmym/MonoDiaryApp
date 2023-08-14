package com.example.monodiaryapp

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


class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                    val context = LocalContext.current
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
                DiaryList()
            }
        }
    )
}

@Composable
fun DiaryList() {
    val diaryList = listOf(
        Diary("일기 제목 1", "일기 본문 첫 줄...", "노래 제목 1", "아티스트 1", LocalDate.parse("2023-08-01")),
        Diary("일기 제목 2", "일기 본문 첫 줄...", "노래 제목 2", "아티스트 2", LocalDate.parse("2023-08-12")),
        Diary("일기 제목 3", "일기 본문 첫 줄...", "노래 제목 3", "아티스트 3", LocalDate.parse("2023-08-11")),
        Diary("일기 제목 4", "일기 본문 첫 줄...", "노래 제목 4", "아티스트 4", LocalDate.parse("2023-08-10")),
        Diary("일기 제목 5", "일기 본문 첫 줄...", "노래 제목 5", "아티스트 5", LocalDate.parse("2023-08-09")),
        Diary("일기 제목 6", "일기 본문 첫 줄...", "노래 제목 6", "아티스트 6", LocalDate.parse("2023-08-08")),
        Diary("일기 제목 7", "일기 본문 첫 줄...", "노래 제목 7", "아티스트 7", LocalDate.parse("2023-08-06")),
        Diary("일기 제목 7", "일기 본문 첫 줄...", "노래 제목 7", "아티스트 7", LocalDate.parse("2023-08-05")),
        Diary("일기 제목 7", "일기 본문 첫 줄...", "노래 제목 7", "아티스트 7", LocalDate.parse("2023-08-04")),
        ).sortedByDescending { it.date }

    LazyColumn {
        items(diaryList) { diary ->
            DiaryItem(diary)
        }
    }
}

@Composable
fun DiaryItem(diary: Diary) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { /* 일기 화면으로 이동하는 코드 추가 */ }),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier
                .background(Color.White)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 이미지는 아직 미구현이라 대체로 처리
            Image(
                painter = painterResource(id = R.drawable.hhh),
                contentDescription = null,
                modifier = Modifier
                    .padding(16.dp)
                    .size(120.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
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
fun formatDateWithDayOfWeek(date: Long): String {
    val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    val formattedDate = date.format(DateTimeFormatter.ofPattern("yy.MM.dd"))
    return "$formattedDate $dayOfWeek"
}


// 일기 드래그해서 삭제하기 (슬랙 링크 확인)