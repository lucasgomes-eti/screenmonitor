package com.bitdrive.screenmonitor.main

import android.Manifest
import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.format.DateUtils
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.bitdrive.screenmonitor.services.TimerService
import com.bitdrive.screenmonitor.ui.composables.Dashboard
import com.bitdrive.screenmonitor.ui.theme.ScreenMonitorTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var usageStatsManager: UsageStatsManager

    @Inject
    lateinit var appOpsManager: AppOpsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val timerServiceIntent = Intent(this, TimerService::class.java)

        val requestPostNotificationPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    startForegroundService(timerServiceIntent)
                }
            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                startForegroundService(timerServiceIntent)
            } else {
                requestPostNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            startForegroundService(timerServiceIntent)
        }

        val appInfoAndStats = mutableStateMapOf<ApplicationInfo, UsageStats>()

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)

        fun queryUsageStats() {
            val stats =
                usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, calendar.timeInMillis, System.currentTimeMillis())
            stats.forEach { usageStats ->
                try {
                    val appInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        packageManager.getApplicationInfo(usageStats.packageName, PackageManager.ApplicationInfoFlags.of(0))
                    } else {
                        packageManager.getApplicationInfo(usageStats.packageName, 0)
                    }
                    if (!appInfo.isSystemPackage()) {
                        appInfoAndStats[appInfo] = usageStats
                    }
                } catch (_: NameNotFoundException) {}
            }
        }

        val requestAppUsagePermissionLauncher = registerForActivityResult(RequestUsageAccessPermission()) { queryUsageStats() }

        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOpsManager.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), packageName)
        } else {
            appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), packageName)
        }

        val isUsageAccessPermissionGranted = if (mode == AppOpsManager.MODE_DEFAULT) {
            checkCallingOrSelfPermission(Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED
        } else {
            mode == AppOpsManager.MODE_ALLOWED
        }

        if (isUsageAccessPermissionGranted) {
            queryUsageStats()
        } else {
            requestAppUsagePermissionLauncher.launch(Unit)
        }

        setContent {
            ScreenMonitorTheme {
                Dashboard(appInfoAndStats = appInfoAndStats)
            }
        }
    }
}

class RequestUsageAccessPermission : ActivityResultContract<Unit, Any>() {
    override fun createIntent(context: Context, input: Unit): Intent {
        return Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Any {
        return Any()
    }
}

fun ApplicationInfo.isSystemPackage(): Boolean {
    return flags and ApplicationInfo.FLAG_SYSTEM != 0
}