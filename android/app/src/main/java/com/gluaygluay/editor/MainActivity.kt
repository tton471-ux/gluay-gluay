package com.gluaygluay.editor

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(
                primary = Color(0xFFFFC247),
                background = Color(0xFF0D1020),
                surface = Color(0xFF171D33)
            )) {
                val selectedVideo = remember { mutableStateOf<Uri?>(null) }
                val picker = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.OpenDocument()
                ) { uri ->
                    if (uri != null) {
                        try {
                            contentResolver.takePersistableUriPermission(
                                uri,
                                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                            )
                        } catch (_: SecurityException) {
                            // Some providers grant temporary access only.
                        }
                        selectedVideo.value = uri
                    }
                }
                val player = remember {
                    ExoPlayer.Builder(this@MainActivity).build()
                }
                DisposableEffect(selectedVideo.value) {
                    val uri = selectedVideo.value
                    if (uri != null) {
                        player.setMediaItem(MediaItem.fromUri(uri))
                        player.prepare()
                        player.playWhenReady = false
                    } else {
                        player.stop()
                        player.clearMediaItems()
                    }
                    onDispose { }
                }
                DisposableEffect(Unit) {
                    onDispose { player.release() }
                }

                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF0D1020)) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text("🍌 GLUAY GLUAY", style = MaterialTheme.typography.headlineMedium)
                        Text("MOBILE EDITOR • PREVIEW TEST 01", color = Color(0xFFB8C2E0))
                        Button(
                            onClick = { picker.launch(arrayOf("video/*")) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("เลือกวิดีโอจากเครื่อง") }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .background(Color.Black, RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedVideo.value != null) {
                                AndroidView(
                                    factory = { context ->
                                        PlayerView(context).apply {
                                            this.player = player
                                            useController = true
                                            resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
                                        }
                                    },
                                    update = { it.player = player },
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text("นำเข้าวิดีโอเพื่อเริ่มดูตัวอย่าง", color = Color.White)
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                if (selectedVideo.value == null) "ยังไม่ได้เลือกวิดีโอ" else "พร้อมดูตัวอย่าง",
                                color = Color(0xFFB8C2E0)
                            )
                            Button(onClick = { player.seekTo(0); player.play() }, enabled = selectedVideo.value != null) {
                                Text("เล่นวิดีโอ")
                            }
                        }
                        Text(
                            "รุ่นเริ่มต้น: นำเข้าและดูตัวอย่างเท่านั้น • Timeline และ Export จะพัฒนาต่อ",
                            color = Color(0xFF8995B8),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
