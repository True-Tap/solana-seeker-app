package com.truetap.solana.seeker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.truetap.solana.seeker.ui.navigation.SolanaSeekerNavGraph
import com.truetap.solana.seeker.ui.theme.SolanaseekerappTheme
import com.truetap.solana.seeker.ui.theme.TrueTapBackground
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SolanaseekerappTheme {
                // Full beige background for the entire app
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(TrueTapBackground)
                ) {
                    SolanaSeekerApp()
                }
            }
        }
    }
}

@Composable
fun SolanaSeekerApp(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    SolanaSeekerNavGraph(
        navController = navController,
        modifier = modifier
    )
}