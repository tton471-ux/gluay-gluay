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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.gluaygluay.editor.audio.AudioAsset
import com.gluaygluay.editor.audio.VoiceOverRecorder
import com.gluaygluay.editor.audio.extractAudioFromVideo
import com.gluaygluay.editor.audio.readMediaDurationMs
import com.gluaygluay.editor.data.ProjectAutosaver
import com.gluaygluay.editor.data.ProjectEntity
import com.gluaygluay.editor.data.ProjectRepository
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

private val AppBackground = Color(0xFF0D1020)
private val AppSurface = Color(0xFF171D33)
private val AppAccent = Color(0xFFFFC247)
private val AppMuted = Color(0xFFB8C2E0)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(
                primary = AppAccent,
                background = AppBackground,
                surface = AppSurface
            )) {
                val repository = remember { ProjectRepository(applicationContext) }
                val scope = rememberCoroutineScope()
                val autosaver = remember(repository, scope) {
                    ProjectAutosaver(repository, scope, debounceMs = 700L)
                }
                val projects by repository.observeProjects().collectAsState(initial = emptyList())
                var currentProject by remember { mutableStateOf<ProjectEntity?>(null) }
                var selectedVideo by remember { mutableStateOf<Uri?>(null) }
                var selectedVideoDurationMs by remember { mutableStateOf(0L) }
                val audioAssets = remember { mutableStateListOf<AudioAsset>() }
                var audioStatus by remember { mutableStateOf("ยังไม่มีไฟล์เสียงในโปรเจกต์") }
                var isRecording by remember { mutableStateOf(false) }
                var recorder by remember { mutableStateOf<VoiceOverRecorder?>(null) }
                val player = remember { ExoPlayer.Builder(this@MainActivity).build() }

                fun persistCurrentProject() {
                    val project = currentProject ?: return
                    val json = buildProjectDocument(project, selectedVideo, selectedVideoDurationMs, audioAssets)
                    autosaver.schedule(
                        projectId = project.id,
                        name = project.name,
                        schemaVersion = 2,
                        documentJson = { json }
                    )
                }

                fun openProject(projectId: String) {
                    scope.launch {
                        val project = repository.openProject(projectId)
                        if (project != null) {
                            currentProject = project
                            restoreProjectMedia(project.documentJson) { video, videoDurationMs, assets ->
                                selectedVideo = video
                                selectedVideoDurationMs = videoDurationMs
                                audioAssets.clear()
                                audioAssets.addAll(assets)
                            }
                            audioStatus = "เปิดโปรเจกต์แล้ว"
                        }
                    }
                }

                fun createProject() {
                    scope.launch {
                        val project = repository.createProject()
                        currentProject = project
                        selectedVideo = null
                        selectedVideoDurationMs = 0L
                        audioAssets.clear()
                        audioStatus = "สร้างโปรเจกต์ใหม่แล้ว"
                    }
                }

                val audioPicker = rememberLauncherForActivityResult(
                    ActivityResultContracts.OpenDocument()
                ) { uri ->
                    if (uri != null) {
                        try {
                            contentResolver.takePersistableUriPermission(
                                uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                            )
                        } catch (_: SecurityException) {}
                        val name = queryDisplayName(uri) ?: "Imported audio"
                        val duration = readMediaDurationMs(this@MainActivity, uri.toString())
                        audioAssets.add(AudioAsset(UUID.randomUUID().toString(), uri.toString(), name, "music", duration))
                        audioStatus = "เพิ่มไฟล์เสียงจากเครื่องแล้ว"
                        persistCurrentProject()
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
                        selectedVideo = uri
                        selectedVideoDurationMs = readMediaDurationMs(this@MainActivity, uri.toString())
                        persistCurrentProject()
                    }
                }
                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { granted ->
                    if (granted) {
                        try {
                            val file = File(filesDir, "projects/${currentProject?.id ?: "unsaved"}/audio/voiceover_${System.currentTimeMillis()}.m4a")
                            val voiceRecorder = VoiceOverRecorder(file)
                            voiceRecorder.start()
                            recorder = voiceRecorder
                            isRecording = true
                            audioStatus = "กำลังอัดเสียงพากย์..."
                        } catch (error: Exception) {
                            audioStatus = "เริ่มอัดเสียงไม่ได้: ${error.message}"
                        }
                    } else audioStatus = "ไม่ได้รับอนุญาตให้ใช้ไมโครโฟน"
                }

                LaunchedEffect(selectedVideo) {
                    selectedVideo?.let {
                        player.setMediaItem(MediaItem.fromUri(it))
                        player.prepare()
                        player.playWhenReady = false
                    } ?: run {
                        player.stop()
                        player.clearMediaItems()
                    }
                }

                DisposableEffect(player) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_STOP) {
                            player.pause()
                            val projectToSave = currentProject
                            if (projectToSave != null) {
                                val snapshot = buildProjectDocument(
                                    projectToSave, selectedVideo, selectedVideoDurationMs, audioAssets
                                )
                                scope.launch {
                                    try {
                                        autosaver.flush(projectToSave.id, projectToSave.name, 2, snapshot)
                                    } catch (_: Exception) {
                                        // Keep shutdown resilient; the explicit Save action reports errors in-app.
                                    }
                                }
                            }
                        }
                    }
                    this@MainActivity.lifecycle.addObserver(observer)
                    onDispose {
                        this@MainActivity.lifecycle.removeObserver(observer)
                        autosaver.cancel()
                        if (isRecording) recorder?.cancel()
                        player.release()
                    }
                }

                Surface(modifier = Modifier.fillMaxSize(), color = AppBackground) {
                    val activeProject = currentProject
                    if (activeProject == null) {
                        HomeStudio(
                            projects = projects,
                            onCreateProject = ::createProject,
                            onOpenProject = ::openProject,
                            onDeleteProject = { id -> scope.launch { repository.deleteProject(id) } }
                        )
                    } else {
                        EditorStudio(
                            project = activeProject,
                            selectedVideo = selectedVideo,
                            player = player,
                            audioAssets = audioAssets,
                            audioStatus = audioStatus,
                            isRecording = isRecording,
                            onBack = {
                                persistCurrentProject()
                                currentProject = null
                                selectedVideo = null
                                selectedVideoDurationMs = 0L
                                audioAssets.clear()
                            },
                            onPickVideo = { videoPicker.launch(arrayOf("video/*")) },
                            onPickAudio = { audioPicker.launch(arrayOf("audio/*")) },
                            onExtractAudio = {
                                val uri = selectedVideo
                                if (uri == null) {
                                    audioStatus = "กรุณาเลือกวิดีโอก่อนแยกเสียง"
                                } else {
                                    audioStatus = "กำลังแยกเสียงจากวิดีโอ..."
                                    Thread {
                                        try {
                                            val out = File(filesDir, "projects/${activeProject.id}/audio/extracted_${System.currentTimeMillis()}.m4a")
                                            extractAudioFromVideo(this@MainActivity, uri, out)
                                            runOnUiThread {
                                                audioAssets.add(AudioAsset(UUID.randomUUID().toString(), out.absolutePath, out.name, "extracted", readMediaDurationMs(this@MainActivity, out.absolutePath)))
                                                audioStatus = "แยกเสียงจากวิดีโอสำเร็จ"
                                                persistCurrentProject()
                                            }
                                        } catch (error: Exception) {
                                            runOnUiThread { audioStatus = "แยกเสียงไม่สำเร็จ: ${error.message}" }
                                        }
                                    }.start()
                                }
                            },
                            onToggleRecording = {
                                if (isRecording) {
                                    try {
                                        val output = recorder?.stop()
                                        if (output != null) {
                                            audioAssets.add(AudioAsset(UUID.randomUUID().toString(), output.absolutePath, output.name, "voiceover", readMediaDurationMs(this@MainActivity, output.absolutePath)))
                                            audioStatus = "บันทึกเสียงพากย์เรียบร้อย"
                                            persistCurrentProject()
                                        }
                                    } catch (error: Exception) {
                                        audioStatus = "บันทึกเสียงไม่สำเร็จ: ${error.message}"
                                    } finally {
                                        recorder = null
                                        isRecording = false
                                    }
                                } else {
                                    val granted = ContextCompat.checkSelfPermission(
                                        this@MainActivity, Manifest.permission.RECORD_AUDIO
                                    ) == PackageManager.PERMISSION_GRANTED
                                    if (granted) {
                                        try {
                                            val file = File(cacheDir, "audio/voiceover_${System.currentTimeMillis()}.m4a")
                                            recorder = VoiceOverRecorder(file).also { it.start() }
                                            isRecording = true
                                            audioStatus = "กำลังอัดเสียงพากย์..."
                                        } catch (error: Exception) {
                                            audioStatus = "เริ่มอัดเสียงไม่ได้: ${error.message}"
                                        }
                                    } else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            },
                            onSaveNow = {
                                scope.launch {
                                    val json = buildProjectDocument(activeProject, selectedVideo, selectedVideoDurationMs, audioAssets)
                                    autosaver.flush(activeProject.id, activeProject.name, 2, json)
                                    audioStatus = "บันทึกโปรเจกต์แล้ว"
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    private fun queryDisplayName(uri: Uri): String? {
        return try {
            contentResolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst()) cursor.getString(0) else null
                }
        } catch (_: Exception) { null }
    }
}

@androidx.compose.runtime.Composable
private fun HomeStudio(
    projects: List<ProjectEntity>,
    onCreateProject: () -> Unit,
    onOpenProject: (String) -> Unit,
    onDeleteProject: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("🍌 GLUAY GLUAY PRO", style = MaterialTheme.typography.headlineMedium, color = Color.White)
        Text("HOME STUDIO • LOCAL PROJECTS", color = AppMuted)
        Button(onClick = onCreateProject, modifier = Modifier.fillMaxWidth()) {
            Text("＋ สร้างโปรเจกต์ใหม่")
        }
        Text("โปรเจกต์ของฉัน (${projects.size})", color = AppAccent, style = MaterialTheme.typography.titleLarge)
        if (projects.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text("ยังไม่มีโปรเจกต์\nกดสร้างโปรเจกต์ใหม่เพื่อเริ่มต้น", color = AppMuted)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(projects, key = { it.id }) { project ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = AppSurface)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { onOpenProject(project.id) }.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(project.name, color = Color.White, style = MaterialTheme.typography.titleMedium)
                                Text("แก้ไขล่าสุด: ${java.text.DateFormat.getDateTimeInstance().format(java.util.Date(project.updatedAtEpochMs))}", color = AppMuted, style = MaterialTheme.typography.bodySmall)
                                Text("Schema v${project.schemaVersion}", color = AppMuted, style = MaterialTheme.typography.labelSmall)
                            }
                            TextButton(onClick = { onDeleteProject(project.id) }) {
                                Text("ลบ", color = Color(0xFFFF8A80))
                            }
                        }
                    }
                }
            }
        }
        Text("บันทึกข้อมูลโปรเจกต์ไว้ในเครื่อง • ไม่แก้ไขไฟล์ต้นฉบับ", color = AppMuted, style = MaterialTheme.typography.bodySmall)
    }
}

@androidx.compose.runtime.Composable
private fun EditorStudio(
    project: ProjectEntity,
    selectedVideo: Uri?,
    player: ExoPlayer,
    audioAssets: List<AudioAsset>,
    audioStatus: String,
    isRecording: Boolean,
    onBack: () -> Unit,
    onPickVideo: () -> Unit,
    onPickAudio: () -> Unit,
    onExtractAudio: () -> Unit,
    onToggleRecording: () -> Unit,
    onSaveNow: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("‹ โปรเจกต์") }
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(project.name, color = Color.White, style = MaterialTheme.typography.titleLarge)
                Text("EDITOR • ${project.id.take(8)}", color = AppMuted, style = MaterialTheme.typography.labelSmall)
            }
            TextButton(onClick = onSaveNow) { Text("บันทึก", color = AppAccent) }
        }
        Button(onClick = onPickVideo, modifier = Modifier.fillMaxWidth()) {
            Text(if (selectedVideo == null) "เลือกวิดีโอจากเครื่อง" else "เปลี่ยนวิดีโอ")
        }
        Box(
            modifier = Modifier.fillMaxWidth().weight(1f)
                .background(Color.Black, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (selectedVideo != null) {
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
            } else Text("นำเข้าวิดีโอเพื่อเริ่มดูตัวอย่าง", color = Color.White)
        }
        Text("AUDIO STUDIO", style = MaterialTheme.typography.titleLarge, color = AppAccent)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onPickAudio, modifier = Modifier.weight(1f)) { Text("เพิ่มเพลง / เสียง") }
            OutlinedButton(onClick = onExtractAudio, modifier = Modifier.weight(1f)) { Text("แยกเสียงวิดีโอ") }
        }
        Button(onClick = onToggleRecording, modifier = Modifier.fillMaxWidth()) {
            Text(if (isRecording) "หยุดอัดเสียงพากย์" else "● อัดเสียงพากย์")
        }
        Text(audioStatus, color = AppMuted, style = MaterialTheme.typography.bodySmall)
        Text("ไฟล์เสียงในโปรเจกต์: ${audioAssets.size}", color = Color.White)
        LazyColumn(modifier = Modifier.height(100.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(audioAssets, key = { it.id }) { asset ->
                Text("• ${asset.displayName} [${asset.sourceType}]", color = AppMuted, style = MaterialTheme.typography.bodySmall)
            }
        }
        Text("การบันทึกโปรเจกต์เก็บรายการสื่อและการตั้งค่า • ยังไม่ใช่ระบบตัดต่อ Timeline หรือ Export", color = AppMuted, style = MaterialTheme.typography.labelSmall)
    }
}

private fun buildProjectDocument(
    entity: ProjectEntity,
    selectedVideo: Uri?,
    selectedVideoDurationMs: Long,
    audioAssets: List<AudioAsset>
): String {
    val project = JSONObject()
        .put("id", entity.id)
        .put("name", entity.name)
        .put("createdAtEpochMs", entity.createdAtEpochMs)
        .put("updatedAtEpochMs", System.currentTimeMillis())
        .put("canvas", JSONObject()
            .put("width", 1080)
            .put("height", 1920)
            .put("aspectRatio", "9:16")
            .put("fit", "contain")
            .put("background", "#000000"))
        .put("frameRate", 30)

    val tracks = JSONArray()
    val videoClips = JSONArray()
    selectedVideo?.let { uri ->
        videoClips.put(JSONObject()
            .put("id", "video-clip-${entity.id}")
            .put("type", "video")
            .put("assetId", "video-source")
            .put("sourceUri", uri.toString())
            .put("sourceInMs", 0)
            .put("sourceOutMs", selectedVideoDurationMs.coerceAtLeast(1L))
            .put("timelineStartMs", 0)
            .put("speed", 1.0)
            .put("volume", 1.0))
    }
    tracks.put(JSONObject().put("id", "video-track-1").put("type", "video").put("role", "primary").put("clips", videoClips))

    val musicClips = JSONArray()
    val effectClips = JSONArray()
    val voiceClips = JSONArray()
    audioAssets.forEach { asset ->
        val clip = JSONObject()
            .put("id", asset.id)
            .put("type", "audio")
            .put("assetId", asset.id)
            .put("sourceUri", asset.uriOrPath)
            .put("sourceType", asset.sourceType)
            .put("sourceInMs", 0)
            .put("sourceOutMs", asset.durationMs.coerceAtLeast(1L))
            .put("timelineStartMs", 0)
            .put("volume", 1.0)
            .put("muted", false)
        when (asset.sourceType) {
            "voiceover" -> voiceClips.put(clip)
            "sfx" -> effectClips.put(clip)
            else -> musicClips.put(clip)
        }
    }
    tracks.put(JSONObject().put("id", "music-track-1").put("type", "audio").put("role", "music").put("clips", musicClips))
    tracks.put(JSONObject().put("id", "sfx-track-1").put("type", "audio").put("role", "soundEffects").put("clips", effectClips))
    tracks.put(JSONObject().put("id", "voice-track-1").put("type", "audio").put("role", "voiceover").put("clips", voiceClips))
    tracks.put(JSONObject().put("id", "caption-track-1").put("type", "caption").put("role", "subtitles").put("clips", JSONArray()))
    tracks.put(JSONObject().put("id", "text-track-1").put("type", "text").put("role", "titles").put("clips", JSONArray()))

    val assets = JSONArray()
    selectedVideo?.let { assets.put(JSONObject().put("id", "video-source").put("kind", "video").put("uri", it.toString()).put("durationMs", selectedVideoDurationMs)) }
    audioAssets.forEach { assets.put(JSONObject().put("id", it.id).put("kind", "audio").put("uri", it.uriOrPath).put("displayName", it.displayName) .put("sourceType", it.sourceType).put("durationMs", it.durationMs)) }

    return JSONObject()
        .put("schemaVersion", 2)
        .put("project", project.put("tracks", tracks).put("assets", assets)
            .put("aiSuggestions", JSONArray())
            .put("proxyFiles", JSONArray())
            .put("cacheReferences", JSONArray()))
        .toString()
}

private fun restoreProjectMedia(
    documentJson: String,
    onRestored: (Uri?, Long, List<AudioAsset>) -> Unit
) {
    try {
        val project = JSONObject(documentJson).getJSONObject("project")
        val assets = project.optJSONArray("assets") ?: JSONArray()
        var video: Uri? = null
        var videoDurationMs = 0L
        val audio = mutableListOf<AudioAsset>()
        for (index in 0 until assets.length()) {
            val item = assets.optJSONObject(index) ?: continue
            val uri = item.optString("uri").takeIf { it.isNotBlank() } ?: continue
            when (item.optString("kind")) {
                "video" -> {
                    video = Uri.parse(uri)
                    videoDurationMs = item.optLong("durationMs", 0L)
                }
                "audio" -> audio.add(AudioAsset(
                    id = item.optString("id", UUID.randomUUID().toString()),
                    uriOrPath = uri,
                    displayName = item.optString("displayName", "Audio"),
                    sourceType = item.optString("sourceType", "music"),
                    durationMs = item.optLong("durationMs", 0L)
                ))
            }
        }
        onRestored(video, videoDurationMs, audio)
    } catch (_: Exception) {
        onRestored(null, 0L, emptyList())
    }
}
