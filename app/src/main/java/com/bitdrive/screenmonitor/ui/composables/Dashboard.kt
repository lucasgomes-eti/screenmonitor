package com.bitdrive.screenmonitor.ui.composables

import android.app.usage.UsageStats
import android.content.pm.ApplicationInfo
import android.text.format.DateUtils
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bitdrive.screenmonitor.ui.theme.ScreenMonitorTheme
import com.bitdrive.screenmonitor.viewmodels.DashboardViewModel

@Composable
fun Dashboard(
    viewModel: DashboardViewModel = viewModel(),
    appInfoAndStats: Map<ApplicationInfo, UsageStats>
) {

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn() {
            appInfoAndStats.forEach { (appInfo, usageStats) ->
                item {
                    Text(text = "${appInfo.loadLabel(LocalContext.current.packageManager)} - ${DateUtils.formatElapsedTime(usageStats.totalTimeInForeground / 1_000)}")
                }
            }
        }
    }
}

@Preview
@Composable
private fun DashboardPreview() {
    ScreenMonitorTheme {
        Dashboard(appInfoAndStats = mapOf())
    }
}