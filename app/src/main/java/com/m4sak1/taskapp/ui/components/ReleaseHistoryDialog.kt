package com.m4sak1.taskapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m4sak1.taskapp.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

data class GithubRelease(
    val name: String,
    val tagName: String,
    val publishedAt: String,
    val body: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReleaseHistoryDialog(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    disableAnimations: Boolean
) {
    if (!visible && disableAnimations) return

    var releases by remember { mutableStateOf<List<GithubRelease>?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(visible) {
        if (visible) {
            isLoading = true
            errorMessage = null
            try {
                val fetchedReleases = fetchReleases()
                releases = fetchedReleases
            } catch (e: Exception) {
                errorMessage = "Failed to load release history."
            } finally {
                isLoading = false
            }
        }
    }

    CustomInfoDialog(
        visible = visible,
        onDismissRequest = onDismissRequest,
        title = "リリース履歴",
        confirmText = stringResource(R.string.close),
        disableAnimations = disableAnimations
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp, max = 400.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (releases.isNullOrEmpty()) {
                Text(
                    text = "リリースが見つかりませんでした。",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(releases!!) { release ->
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = release.name.ifEmpty { release.tagName },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = release.publishedAt.take(10), // YYYY-MM-DD
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = release.body,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Divider(
                                modifier = Modifier.padding(top = 16.dp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

private suspend fun fetchReleases(): List<GithubRelease> = withContext(Dispatchers.IO) {
    val url = URL("https://api.github.com/repos/masaki-09/m4sak1-task-app/releases")
    val connection = url.openConnection() as HttpURLConnection
    connection.requestMethod = "GET"
    connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
    connection.connectTimeout = 10000
    connection.readTimeout = 10000

    if (connection.responseCode == HttpURLConnection.HTTP_OK) {
        val response = connection.inputStream.bufferedReader().use { it.readText() }
        val jsonArray = JSONArray(response)
        val releases = mutableListOf<GithubRelease>()

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            releases.add(
                GithubRelease(
                    name = obj.optString("name", ""),
                    tagName = obj.optString("tag_name", ""),
                    publishedAt = obj.optString("published_at", ""),
                    body = obj.optString("body", "")
                )
            )
        }
        return@withContext releases
    } else {
        throw Exception("Failed to fetch. Code: ${connection.responseCode}")
    }
}
