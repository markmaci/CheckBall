package com.example.checkball

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.example.checkball.ui.theme.CheckBallTheme

val lacquierRegular = FontFamily(
    Font(R.font.lacquerregular, FontWeight.Normal)
)

@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CheckBallTheme {
                SplashScreen()
            }
        }
    }

    @Composable
    fun SplashScreen() {
        val bounceAnim = remember { Animatable(0f) }
        var showAppName by remember { mutableStateOf(false) }
        val textAlpha by animateFloatAsState(targetValue = if (showAppName) 1f else 0f, animationSpec = tween(500))

        LaunchedEffect(Unit) {
            repeat(3) {
                bounceAnim.animateTo(
                    targetValue = -50f,
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                )
                bounceAnim.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                )
            }

            showAppName = true

            delay(2000)
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF2EFDE)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.basketball),
                    contentDescription = null,
                    modifier = Modifier
                        .offset(y = bounceAnim.value.dp)
                        .size(200.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))
                Row {
                    Text(
                        text = "Check",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontFamily =lacquierRegular,
                        modifier = Modifier.alpha(textAlpha)
                    )
                    Text(
                        text = "Ball",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF68238),
                        fontFamily =lacquierRegular,
                        modifier = Modifier.alpha(textAlpha)
                    )
                }
            }
        }
    }
}
