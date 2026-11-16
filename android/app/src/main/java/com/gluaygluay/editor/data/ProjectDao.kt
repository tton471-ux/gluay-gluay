package com.gluaygluay.editor.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY lastOpenedAtEpochMs DESC")
    fun observeProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :projectId LIMIT 1")
    suspend fun getProject(projectId: String): ProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(project: ProjectEntity)

    @Query("UPDATE projects SET lastOpenedAtEpochMs = :openedAt WHERE id = :projectId")
    suspend fun markOpened(projectId: String, openedAt: Long)

    @Query("DELETE FROM projects WHERE id = :projectId")
    suspend fun deleteProject(projectId: String)

    @Query("SELECT COUNT(*) FROM projects")
    suspend fun countProjects(): Int
}
