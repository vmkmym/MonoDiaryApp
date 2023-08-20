package com.example.monodiaryapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.monodiaryapp.ui.theme.MonoDiaryAppTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MonoDiaryAppTheme {
                LaunchedEffect(true) {
                    delay(3000)
                    val intent = Intent(this@MainActivity, HomeActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }
                ShowInitialScreen()
            }
        }
    }
}


@Composable
fun ShowInitialScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .wrapContentSize(Alignment.Center)

    ) {
        Image(
            painter = painterResource(id = R.drawable.hhh),
            contentDescription = null,
            modifier = Modifier
                .size(200.dp)
                .padding(10.dp)
                .background(shape = CircleShape, color = Color.White)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = null,
            modifier = Modifier
                .size(120.dp)
                .padding(10.dp)
                .background(color = Color.White)
                .fillMaxSize()
                .align(Alignment.CenterHorizontally),
            contentScale = ContentScale.Crop
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MonoDiaryAppTheme {
        ShowInitialScreen()
    }
}