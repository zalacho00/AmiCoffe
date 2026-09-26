package com.example.amicoffe_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.amicoffe_app.ui.screens.menu.AmiCoffeMainScreen
import com.example.amicoffe_app.ui.theme.AmiCoffeappTheme
import com.example.amicoffe_app.ui.theme.AmiScreenBg

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AmiCoffeappTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = AmiScreenBg
                ) {
                    AmiCoffeMainScreen()
                }
            }
        }
    }
}
