package com.siliconsage.miner.util

import android.content.Context
import com.siliconsage.miner.BuildConfig
import com.siliconsage.miner.viewmodel.GameViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * UpdateService v1.0 (Phase 14 extraction)
 * Handles version checking and in-app update installation flows.
 */
object UpdateService {

    /**
     * Check for new versions via UpdateManager
     */
    fun check(
        scope: CoroutineScope,
        context: Context?,
        showNotification: Boolean,
        onInfoReceived: (UpdateInfo?) -> Unit,
        onCurrent: () -> Unit
    ) {
        UpdateManager.checkUpdate(BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE) { info, success ->
            scope.launch(Dispatchers.Main) {
                onInfoReceived(info)
                
                if (info != null) {
                    if (showNotification && context != null) {
                        if (UpdateNotificationManager.shouldShowNotification(context)) {
                            UpdateNotificationManager.showUpdateNotification(
                                context,
                                info.version,
                                info.url
                            )
                            UpdateNotificationManager.markNotificationShown(context)
                        }
                    }
                } else if (success) {
                    onCurrent()
                }
            }
        }
    }

    /**
     * Trigger the download and install flow
     */
    fun startDownload(
        context: Context,
        info: UpdateInfo,
        scope: CoroutineScope,
        onProgress: (Float) -> Unit,
        onComplete: (Boolean) -> Unit
    ) {
        if (info.downloadUrl.isNotEmpty()) {
            UpdateManager.downloadUpdate(
                url = info.downloadUrl,
                context = context,
                onProgress = onProgress,
                onComplete = { file ->
                    scope.launch(Dispatchers.Main) {
                        if (file != null) {
                            UpdateManager.installUpdate(context, file)
                            onComplete(true)
                        } else {
                            onComplete(false)
                        }
                    }
                }
            )
        } else {
            UpdateManager.openReleasePage(context, info.url.ifEmpty { "https://github.com/Vatteck/SiliconSageAIMiner/releases" })
            onComplete(true)
        }
    }
}
