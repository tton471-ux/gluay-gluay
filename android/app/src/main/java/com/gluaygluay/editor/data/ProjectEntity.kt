package com.gluaygluay.editor.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A complete, versioned project document. JSON keeps the project format forward-compatible. */
@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val schemaVersion: Int,
    val documentJson: String,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long,
    val lastOpenedAtEpochMs: Long
)
