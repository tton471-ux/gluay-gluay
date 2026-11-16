package com.gluaygluay.editor.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Debounces rapid editor changes and persists the latest complete document.
 * The caller owns the scope and should cancel it with the editor lifecycle.
 */
class ProjectAutosaver(
    private val repository: ProjectRepository,
    private val scope: CoroutineScope,
    private val debounceMs: Long = 700L,
    private val onSaveError: (Throwable) -> Unit = {}
) {
    private var pendingSave: Job? = null

    fun schedule(
        projectId: String,
        name: String,
        schemaVersion: Int,
        documentJson: () -> String
    ) {
        pendingSave?.cancel()
        pendingSave = scope.launch {
            delay(debounceMs)
            try {
                repository.saveProject(
                    projectId = projectId,
                    name = name,
                    schemaVersion = schemaVersion,
                    documentJson = documentJson()
                )
            } catch (error: Throwable) {
                onSaveError(error)
            }
        }
    }

    suspend fun flush(
        projectId: String,
        name: String,
        schemaVersion: Int,
        documentJson: String
    ) {
        pendingSave?.cancel()
        pendingSave = null
        repository.saveProject(projectId, name, schemaVersion, documentJson)
    }

    fun cancel() {
        pendingSave?.cancel()
        pendingSave = null
    }
}
