package com.example.monodiaryapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.monodiaryapp.ui.theme.MonoDiaryAppTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Suppress("DEPRECATION", "CAST_NEVER_SUCCEEDS")
class DiaryDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val title = intent.getStringExtra("title") ?: ""
        val content = intent.getStringExtra("content") ?: ""
        val songTitle = intent.getStringExtra("songTitle") ?: ""
        val artist = intent.getStringExtra("artist") ?: ""
        val dateString = intent.getStringExtra("date") ?: ""

        val diary = Diary(title, content, songTitle, artist, LocalDate.parse(dateString))

        setContent {
            MonoDiaryAppTheme {
                DiaryDetailScreen(diary)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryDetailScreen(diary: Diary) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = diary.title,
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = diary.content,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 16.sp),
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "Song Title: ${diary.songTitle}",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "Date: ${diary.date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
            modifier = Modifier.fillMaxWidth()
        )

        // Add more UI elements if needed

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { /* Handle button click if needed */ },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Back")
        }
    }
}