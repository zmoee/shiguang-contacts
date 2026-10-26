package com.zmoee.shiguang

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.zmoee.shiguang.ui.ShiguangApp
import com.zmoee.shiguang.ui.theme.ShiguangTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT,
                Color.TRANSPARENT,
            ),
            navigationBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT,
                Color.argb(128, 27, 27, 27),
            ),
        )
        setContent {
            ShiguangTheme {
                ShiguangApp()
            }
        }
    }
}
