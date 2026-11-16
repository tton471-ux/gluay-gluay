package com.gluaygluay.editor

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.gluaygluay.editor.audio.AudioAsset
import com.gluaygluay.editor.audio.VoiceOverRecorder
import com.gluaygluay.editor.audio.extractAudioFromVideo
import java.io.File
import java.util.UUID

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
                val audioAssets = remember { mutableStateListOf<AudioAsset>() }
                val audioStatus = remember { mutableStateOf("ยังไม่มีไฟล์เสียงในโปรเจกต์") }
                val isRecording = remember { mutableStateOf(false) }
                val recorder = remember { mutableStateOf<VoiceOverRecorder?>(null) }
                val audioPicker = rememberLauncherForActivityResult(
                    ActivityResultContracts.OpenDocument()
                ) { uri ->
                    if (uri != null) {
                        try {
                            contentResolver.takePersistableUriPermission(
                                uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                            )
                        } catch (_: SecurityException) {}
                        val name = uri.lastPathSegment ?: "Imported audio"
                        audioAssets.add(AudioAsset(UUID.randomUUID().toString(), uri.toString(), name, "music"))
                        audioStatus.value = "เพิ่มไฟล์เสียงจากเครื่องแล้ว"
                    }
                }
                val videoPicker = rememberLauncherForActivityResult(
                    ActivityResultContracts.OpenDocument()
                ) { uri ->
                    if (uri != null) {
                        try {
                            contentResolver.takePersistableUriPermission(
                                uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                            )
                        } catch (_: SecurityException) {}
                        selectedVideo.value = uri
                    }
                }
                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { granted ->
                    if (granted) startVoiceRecording(
                        recorder, isRecording, audioStatus, audioAssets
                    ) else audioStatus.value = "ไม่ได้รับอนุญาตให้ใช้ไมโครโฟน"
                }
                val player = remember { ExoPlayer.Builder(this@MainActivity).build() }

                DisposableEffect(selectedVideo.value) {
                    selectedVideo.value?.let {
                        player.setMediaItem(MediaItem.fromUri(it))
                        player.prepare()
                        player.playWhenReady = false
                    } ?: run {
                        player.stop()
                        player.clearMediaItems()
                    }
                    onDispose { }
                }
                DisposableEffect(Unit) {
                    onDispose {
                        if (isRecording.value) recorder.value?.cancel()
                        player.release()
                    }
                }

                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF0D1020)) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("🍌 GLUAY GLUAY", style = MaterialTheme.typography.headlineMedium)
                        Text("MOBILE EDITOR • VIDEO + AUDIO FOUNDATION", color = Color(0xFFB8C2E0))
                        Button(
                            onClick = { videoPicker.launch(arrayOf("video/*")) },
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

                        Text("AUDIO STUDIO", style = MaterialTheme.typography.titleLarge, color = Color(0xFFFFC247))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { audioPicker.launch(arrayOf("audio/*")) },
                                modifier = Modifier.weight(1f)
                            ) { Text("เพิ่มเพลง / เสียง") }
                            Button(
                                onClick = {
                                    val uri = selectedVideo.value
                                    if (uri == null) {
                                        audioStatus.value = "กรุณาเลือกวิดีโอก่อนแยกเสียง"
                                    } else {
                                        audioStatus.value = "กำลังแยกเสียงจากวิดีโอ..."
                                        Thread {
                                            try {
                                                val out = File(cacheDir, "audio/extracted_${System.currentTimeMillis()}.m4a")
                                                extractAudioFromVideo(this@MainActivity, uri, out)
                                                runOnUiThread {
                                                    audioAssets.add(AudioAsset(UUID.randomUUID().toString(), out.absolutePath, out.name, "extracted"))
                                                    audioStatus.value = "แยกเสียงจากวิดีโอสำเร็จ"
                                                }
                                            } catch (error: Exception) {
                                                runOnUiThread { audioStatus.value = "แยกเสียงไม่สำเร็จ: ${error.message}" }
                                            }
                                        }.start()
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) { Text("แยกเสียงวิดีโอ") }
                        }
                        Button(
                            onClick = {
                                if (isRecording.value) {
                                    try {
                                        val output = recorder.value?.stop()
                                        if (output != null) {
                                            audioAssets.add(AudioAsset(UUID.randomUUID().toString(), output.absolutePath, output.name, "voiceover"))
                                            audioStatus.value = "บันทึกเสียงพากย์เรียบร้อย"
                                        }
                                    } catch (error: Exception) {
                                        audioStatus.value = "บันทึกเสียงไม่สำเร็จ: ${error.message}"
                                    } finally {
                                        recorder.value = null
                                        isRecording.value = false
                                    }
                                } else {
                                    val granted = ContextCompat.checkSelfPermission(
                                        this@MainActivity, Manifest.permission.RECORD_AUDIO
                                    ) == PackageManager.PERMISSION_GRANTED
                                    if (granted) startVoiceRecording(recorder, isRecording, audioStatus, audioAssets)
                                    else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(if (isRecording.value) "หยุดอัดเสียงพากย์" else "● อัดเสียงพากย์จากไมโครโฟน") }

                        Text(audioStatus.value, color = Color(0xFFB8C2E0), style = MaterialTheme.typography.bodySmall)
                        Text("ไฟล์เสียงในโปรเจกต์: ${audioAssets.size}", color = Color.White)
                        audioAssets.takeLast(4).forEach { asset ->
                            Text("• ${asset.displayName}  [${asset.sourceType}]", color = Color(0xFF9EABD0), style = MaterialTheme.typography.bodySmall)
                        }
                        Text(
                            "ขั้นนี้เพิ่มไฟล์เสียงและบันทึกเสียงเข้ารายการโปรเจกต์ชั่วคราว • ยังไม่ผสมเสียงลง Timeline/Export",
                            color = Color(0xFF8995B8),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }

    private fun startVoiceRecording(
        recorderState: androidx.compose.runtime.MutableState<VoiceOverRecorder?>,
        recordingState: androidx.compose.runtime.MutableState<Boolean>,
        status: androidx.compose.runtime.MutableState<String>,
        assets: MutableList<AudioAsset>
    ) {
        try {
            val file = File(cacheDir, "audio/voiceover_${System.currentTimeMillis()}.m4a")
            val voiceRecorder = VoiceOverRecorder(file)
            voiceRecorder.start()
            recorderState.value = voiceRecorder
            recordingState.value = true
            status.value = "กำลังอัดเสียงพากย์..."
        } catch (error: Exception) {
            status.value = "เริ่มอัดเสียงไม่ได้: ${error.message}"
        }
    }
}
