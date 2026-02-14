package com.siliconsage.miner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.siliconsage.miner.ui.MainScreen
import com.siliconsage.miner.ui.TransitionScreen
import com.siliconsage.miner.ui.theme.SiliconSageAIMinerTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.siliconsage.miner.viewmodel.GameViewModel
import com.siliconsage.miner.viewmodel.GameViewModelFactory

class MainActivity : ComponentActivity() {
    
    private val viewModel: GameViewModel by viewModels {
        GameViewModelFactory((application as MinerApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.loadTechTreeFromAssets(application) // Load Tech Tree from JSON
        
        // Request Notification Permission for Android 13+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            val permissionRequest = registerForActivityResult(
                androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) {
                    // Good to go
                }
            }
            
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                permissionRequest.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        
        // Check for updates on startup and show notification if found
        viewModel.checkForUpdates(
            c = this,
            onResult = { _, found ->
                // Custom handling if needed, but GameViewModel now handles the notification
            },
            showNotification = true
        )
        
        // Init Engines
        com.siliconsage.miner.util.HapticManager.init(this)
        com.siliconsage.miner.util.SoundManager.init(this)
        com.siliconsage.miner.util.HeadlineManager.init(this)
        
        // Init notification channel for updates
        com.siliconsage.miner.util.UpdateNotificationManager.createNotificationChannel(this)
        
        // Apply DPI-aware UI scaling
        applyUIScaling()

        setContent {
            val themeColorHex by viewModel.themeColor.collectAsState()
            val themeColor = try { Color(android.graphics.Color.parseColor(themeColorHex)) } catch (e: Exception) { com.siliconsage.miner.ui.theme.NeonGreen }
            
            SiliconSageAIMinerTheme(
                primaryColorOverride = themeColor
            ) {
                val location by viewModel.currentLocation.collectAsState()
                val uiScale by viewModel.uiScale.collectAsState()
                
                // v3.4.10: Proper density-based scaling instead of pixel-zooming
                val currentDensity = LocalDensity.current
                val customDensity = Density(
                    density = currentDensity.density * uiScale.scaleFactor,
                    fontScale = currentDensity.fontScale * uiScale.scaleFactor
                )
                
                CompositionLocalProvider(LocalDensity provides customDensity) {
                    // A surface container using the 'background' color from the theme
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        if (location == "LAUNCH_PRELUDE" || location == "VOID_PRELUDE") {
                            TransitionScreen(viewModel = viewModel)
                        } else {
                            MainScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        com.siliconsage.miner.util.SoundManager.pauseAll()
        viewModel.onAppBackgrounded()
    }
    
    override fun onResume() {
        super.onResume()
        com.siliconsage.miner.util.SoundManager.resumeAll()
        viewModel.onAppForegrounded(this)
    }
    
    /**
     * Apply DPI-aware UI scaling based on device density and user preference
     */
    private fun applyUIScaling() {
        val prefs = getSharedPreferences("ui_preferences", MODE_PRIVATE)
        val userScaleOrdinal = prefs.getInt("ui_scale", -1)
        
        val densityDpi = resources.displayMetrics.densityDpi
        
        // Determine target density
        val targetDensity = if (userScaleOrdinal >= 0) {
            // User has set a preference - use it
            val userScale = com.siliconsage.miner.data.UIScale.fromOrdinal(userScaleOrdinal)
            densityDpi * userScale.scaleFactor
        } else {
            // Auto-scale based on device DPI
            when {
                densityDpi >= 640 -> densityDpi * 0.75f  // xxxhdpi -> compact
                densityDpi >= 480 -> densityDpi * 0.83f  // xxhdpi -> slightly compact
                densityDpi >= 320 -> densityDpi * 0.88f  // xhdpi -> slightly compact
                else -> densityDpi.toFloat()  // mdpi/hdpi unchanged
            }
        }
        
        // Apply scaling
        val scale = targetDensity / densityDpi
        resources.displayMetrics.density = resources.displayMetrics.density * scale
        resources.displayMetrics.scaledDensity = resources.displayMetrics.scaledDensity * scale
    }
}
