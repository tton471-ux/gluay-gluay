package com.gluaygluay.editor.data

import android.content.Context
import org.json.JSONObject
import java.util.UUID

/**
 * Local-first project persistence.
 * The full project document is stored atomically in Room; original media is never rewritten.
 */
class ProjectRepository(context: Context) {
    private val dao = GluayDatabase.getInstance(context).projectDao()

    fun observeProjects() = dao.observeProjects()

    suspend fun createProject(name: String = "Untitled Project"): ProjectEntity {
        val now = System.currentTimeMillis()
        val id = UUID.randomUUID().toString()
        val safeName = name.trim().ifEmpty { "Untitled Project" }
        val document = JSONObject()
            .put("schemaVersion", 2)
            .put("project", JSONObject()
                .put("id", id)
                .put("name", safeName)
                .put("createdAtEpochMs", now)
                .put("updatedAtEpochMs", now)
                .put("canvas", JSONObject()
                    .put("width", 1080)
                    .put("height", 1920)
                    .put("aspectRatio", "9:16")
                    .put("fit", "contain")
                    .put("background", "#000000"))
                .put("frameRate", 30)
                .put("tracks", org.json.JSONArray())
                .put("assets", org.json.JSONArray())
                .put("aiSuggestions", org.json.JSONArray())
                .put("proxyFiles", org.json.JSONArray())
                .put("cacheReferences", org.json.JSONArray()))
            .toString()
        val entity = ProjectEntity(
            id = id,
            name = safeName,
            schemaVersion = 2,
            documentJson = document,
            createdAtEpochMs = now,
            updatedAtEpochMs = now,
            lastOpenedAtEpochMs = now
        )
        dao.upsert(entity)
        return entity
    }

    suspend fun saveProject(
        projectId: String,
        name: String,
        schemaVersion: Int,
        documentJson: String
    ) {
        require(schemaVersion >= 1) { "Invalid project schema version" }
        val parsed = JSONObject(documentJson)
        require(parsed.has("project")) { "Project document is missing the project object" }
        val now = System.currentTimeMillis()
        val previous = dao.getProject(projectId)
        dao.upsert(
            ProjectEntity(
                id = projectId,
                name = name.trim().ifEmpty { "Untitled Project" },
                schemaVersion = schemaVersion,
                documentJson = documentJson,
                createdAtEpochMs = previous?.createdAtEpochMs ?: now,
                updatedAtEpochMs = now,
                lastOpenedAtEpochMs = now
            )
        )
    }

    suspend fun openProject(projectId: String): ProjectEntity? {
        val project = dao.getProject(projectId) ?: return null
        dao.markOpened(projectId, System.currentTimeMillis())
        return project
    }

    suspend fun deleteProject(projectId: String) = dao.deleteProject(projectId)

    suspend fun projectCount(): Int = dao.countProjects()
}
