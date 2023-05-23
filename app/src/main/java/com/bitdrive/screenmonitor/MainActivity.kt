package com.bitdrive.screenmonitor

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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.bitdrive.screenmonitor.ui.theme.ScreenMonitorTheme
import java.util.Calendar

class MainActivity : ComponentActivity() {

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

        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
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

        val appOpsManager = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
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
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LazyColumn() {
                        appInfoAndStats.forEach { (appInfo, usageStats) ->
                            item {
                                Text(text = "${appInfo.loadLabel(packageManager)} - ${DateUtils.formatElapsedTime(usageStats.totalTimeInForeground / 1_000)}")
                            }
                        }
                    }
                }
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