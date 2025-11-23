/*
 * Copyright (c) 2023-2025 tang shimin
 *
 * This file is part of MuJing.
 *
 * MuJing is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MuJing is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MuJing. If not, see <https://www.gnu.org/licenses/>.
 */

package ui.dialog

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import data.GitHubRelease
import data.MujingRelease
import io.ktor.client.*
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.timeout
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import jdk.internal.agent.resources.agent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.apache.maven.artifact.versioning.ComparableVersion
import ui.window.windowBackgroundFlashingOnCloseFixHack

@OptIn(ExperimentalSerializationApi::class)
@Composable
fun UpdateDialog(
    version: String,
    close: () -> Unit,
    autoUpdate:Boolean,
    setAutoUpdate:(Boolean) -> Unit,
    latestVersion:String,
    releaseNote:String,
    ignore:(String) -> Unit,
) {
    DialogWindow(
        title = "检查更新",
        icon = painterResource("logo/logo.png"),
        onCloseRequest = { close() },
        resizable = true,
        state = rememberDialogState(
            position = WindowPosition(Alignment.Center),
            size = DpSize(600.dp, 550.dp)
        ),
    ) {
        windowBackgroundFlashingOnCloseFixHack()
        Surface(
            elevation = 5.dp,
            shape = RectangleShape,
        ) {
            var detecting by remember { mutableStateOf(true) }
            var downloadable by remember { mutableStateOf(latestVersion.isNotEmpty()) }
            var body by remember { mutableStateOf("") }
            var releaseTagName by remember { mutableStateOf("") }

            suspend fun detectingUpdates(version: String) {
                val client = HttpClient {
                    install(io.ktor.client.plugins.HttpTimeout) {
                        requestTimeoutMillis = 5000
                        connectTimeoutMillis = 5000
                        socketTimeoutMillis = 5000
                    }
                    install(UserAgent) {
                        val v = if(version.startsWith("v")) version.substring(1) else version
                        agent = "MuJing/$v (${System.getProperty("os.name")} ${System.getProperty("os.version")}; )"

                    }
                    expectSuccess = false // 不自动抛出异常
                }

                // 优先访问mujingx.com版本检测API
                    val mujingApiUrl = "https://mujingx.com/api/version/latest"
                val githubApiUrl = "https://api.github.com/repos/tangshimin/mujing/releases/latest"
                val githubHeaderName = "Accept"
                val githubHeaderValue = "application/vnd.github.v3+json"

                // 首先尝试mujingx.com API
                try {
                    val mujingResponse: HttpResponse = client.get(mujingApiUrl) {
                        timeout {
                            requestTimeoutMillis = 5000 // 5秒超时
                        }
                    }

                    detecting = false
                    when (mujingResponse.status) {
                        HttpStatusCode.OK -> {
                            val string = mujingResponse.bodyAsText()
                            val format = Json { ignoreUnknownKeys = true }
                            val releases = format.decodeFromString<MujingRelease>(string)
                            val releaseVersion = ComparableVersion(releases.tag_name)
                            val currentVersion = ComparableVersion(version)
                            body = if (releaseVersion > currentVersion) {
                                downloadable = true
                                releaseTagName = releases.tag_name
                                val contentBody = releases.body + ""
                                contentBody
                            } else {
                                downloadable = false
                                "没有可用更新"
                            }
                        }
                        HttpStatusCode.NotFound -> {
                            body = "网页没找到"
                        }
                        HttpStatusCode.InternalServerError -> {
                            body = "服务器错误"
                        }
                        else -> {
                            body = "未知错误"
                        }
                    }
                } catch (exception: Exception) {
                    // mujingx.com API失败，回退到GitHub API
                    println("mujingx.com API failed, falling back to GitHub API: ${exception.message}")

                    // 回退到GitHub API
                    try {
                        val githubResponse: HttpResponse = client.get(githubApiUrl) {
                            header(githubHeaderName, githubHeaderValue)
                            timeout {
                                requestTimeoutMillis = 5000 // 5秒超时
                            }
                        }

                        detecting = false
                        when (githubResponse.status) {
                            HttpStatusCode.OK -> {
                                val string = githubResponse.bodyAsText()
                                val format = Json { ignoreUnknownKeys = true }
                                val releases = format.decodeFromString<GitHubRelease>(string)
                                val releaseVersion = ComparableVersion(releases.tag_name)
                                val currentVersion = ComparableVersion(version)
                                body = if (releaseVersion > currentVersion) {
                                    downloadable = true
                                    releaseTagName = releases.tag_name
                                    var releaseContent = "最新版本：${releases.tag_name}\n"
                                    val contentBody = releases.body
                                    if (contentBody != null) {
                                        val end = contentBody.indexOf("---")
                                        if (end != -1) {
                                            releaseContent += contentBody.substring(0, end)
                                        }
                                    }
                                    releaseContent
                                } else {
                                    downloadable = false
                                    "没有可用更新"
                                }
                            }
                            HttpStatusCode.NotFound -> {
                                body = "网页没找到"
                            }
                            HttpStatusCode.InternalServerError -> {
                                body = "服务器错误"
                            }
                            else -> {
                                body = "未知错误"
                            }
                        }
                    } catch (githubException: Exception) {
                        detecting = false
                        body = "检查更新失败：${githubException.message}"
                    }
                }
            }

            val scope = rememberCoroutineScope()
            LaunchedEffect(Unit) {
                scope.launch(Dispatchers.IO){
                    if(latestVersion.isEmpty()){
                        delay(500)
                        detectingUpdates(version)
                    }

                }
            }

            Box{
                val stateVertical = rememberScrollState(0)
                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize().verticalScroll(stateVertical)
                ) {

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.width(160.dp)
                        ){
                            Text("自动检查更新")
                            Checkbox(
                                checked = autoUpdate,
                                onCheckedChange = { setAutoUpdate(it) }
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("当前版本：$version",
                            textAlign = TextAlign.Left,
                            modifier = Modifier.width(160.dp)
                        )
                    }

                    if (latestVersion.isEmpty() && detecting) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
                        ) {
                            Box(Modifier.width(50.dp).height(50.dp)) {
                                CircularProgressIndicator(Modifier.align(Alignment.Center))
                            }
                        }
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
                        ) {
                            Text("正在检查")
                        }
                    }

                    Column (
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top,
                        modifier = Modifier.fillMaxWidth()
                            .padding(start = 20.dp,top = 10.dp,end = 20.dp)
                    ){
                        if(latestVersion.isNotEmpty()){
                            Text(text = "最新版本：$latestVersion\n",
                                textAlign = TextAlign.Left,
                                modifier = Modifier.width(160.dp)
                            )
                            Text(
                                text = releaseNote,
                                textAlign = TextAlign.Left,
                            )
                        }else{
                            Text(text = "最新版本：$releaseTagName\n",
                                textAlign = TextAlign.Left,
                                modifier = Modifier.width(160.dp)
                            )
                            Text(
                                text = body,
                                textAlign = TextAlign.Left,
//                                modifier = Modifier.width(160.dp)
                            )
                        }
                    }

                }
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(bottom = 10.dp)
                ) {
                    OutlinedButton(onClick = { close() }) {
                        Text("关闭")
                    }
                    Spacer(Modifier.width(20.dp))
                    val uriHandler = LocalUriHandler.current
                    val latest = "https://github.com/tangshimin/mujing/releases"
                    OutlinedButton(
                        onClick = {
                            uriHandler.openUri(latest)
                            close()
                        },
                        enabled = downloadable
                    ) {
                        Text("下载最新版")
                    }
                    Spacer(Modifier.width(20.dp))
                    val ignoreEnable = latestVersion.isNotEmpty() || releaseTagName.isNotEmpty()
                    OutlinedButton(
                        enabled = ignoreEnable,
                        onClick = {
                        if(latestVersion.isNotEmpty()){
                            ignore(latestVersion)
                        }else{
                            ignore(releaseTagName)
                        }
                            close()
                        }) {
                        Text("忽略")
                    }
                }
                VerticalScrollbar(
                    modifier = Modifier.align(Alignment.CenterEnd)
                        .fillMaxHeight(),
                    adapter = rememberScrollbarAdapter(stateVertical)
                )
            }

        }
    }
}

/**
 * 自动检查更新
 * 优先级：先访问mujingx.com，如果失败则回退到GitHub.com
 */
@OptIn(ExperimentalSerializationApi::class)
suspend fun autoDetectingUpdates(version: String): Triple<Boolean, String, String> {
    val client = HttpClient {
        install(io.ktor.client.plugins.HttpTimeout) {
            requestTimeoutMillis = 5000
            connectTimeoutMillis = 5000
            socketTimeoutMillis = 5000
        }
        install(UserAgent) {
            val v = if(version.startsWith("v")) version.substring(1) else version
            agent = "MuJing/$v (${System.getProperty("os.name")} ${System.getProperty("os.version")}; )"

        }
        expectSuccess = false // 不自动抛出异常
    }

    // 优先访问mujingx.com版本检测API
    val mujingApiUrl = "https://mujingx.com/api/version/latest"
    val githubApiUrl = "https://api.github.com/repos/tangshimin/mujing/releases/latest"
    val githubHeaderName = "Accept"
    val githubHeaderValue = "application/vnd.github.v3+json"

    // 首先尝试mujingx.com API
    try {
        val mujingResponse: HttpResponse = client.get(mujingApiUrl) {
            timeout {
                requestTimeoutMillis = 5000 // 5秒超时
            }
        }

        if (mujingResponse.status == HttpStatusCode.OK) {
            val string = mujingResponse.bodyAsText()
            val format = Json { ignoreUnknownKeys = true }
            val releases = format.decodeFromString<MujingRelease>(string)
            val releaseVersion = ComparableVersion(releases.tag_name)
            val currentVersion = ComparableVersion(version)
            if (releaseVersion > currentVersion) {
                var note = ""
                val body = releases.body
                if (releases.body != null) note += body
                return Triple(true, releases.tag_name, note)
            }
        }
    } catch (exception: Exception) {
        // mujingx.com API失败，回退到GitHub API
        println("mujingx.com API failed, falling back to GitHub API: ${exception.message}")
    }

    // 回退到GitHub API
    try {
        val githubResponse: HttpResponse = client.get(githubApiUrl) {
            header(githubHeaderName, githubHeaderValue)
            timeout {
                requestTimeoutMillis = 5000 // 5秒超时
            }
        }

        if (githubResponse.status == HttpStatusCode.OK) {
            val string = githubResponse.bodyAsText()
            val format = Json { ignoreUnknownKeys = true }
            val releases = format.decodeFromString<GitHubRelease>(string)
            val releaseVersion = ComparableVersion(releases.tag_name)
            val currentVersion = ComparableVersion(version)
            if (releaseVersion > currentVersion) {
                var note = ""
                val body = releases.body
                if (body != null) {
                    val end = body.indexOf("---")
                    if (end != -1) {
                        note += body.substring(0, end)
                    }
                }
                return Triple(true, releases.tag_name, note)
            }
        }
    } catch (exception: Exception) {
        exception.printStackTrace()
        return Triple(false, "", "")
    }
    return Triple(false, "", "")
}
