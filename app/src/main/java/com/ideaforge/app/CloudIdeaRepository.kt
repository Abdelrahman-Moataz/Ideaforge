package com.ideaforge.app

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class CloudRoadmapTask(
    val id: String,
    val title: String,
    val description: String = "",
    val done: Boolean = false
)

@Serializable
data class CloudIdea(
    val id: String,
    @SerialName("user_id") val userId: String,
    val name: String,
    val problem: String,
    val audience: String = "",
    val solution: String = "",
    val category: String = "Personal",
    val notes: String = "",
    val stage: String = "Captured",
    val tasks: List<CloudRoadmapTask> = emptyList(),
    @SerialName("created_at") val createdAt: String? = null
)

class CloudIdeaRepository {
    private val client get() = SupabaseClientProvider.client
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun listForUser(userId: String): List<CloudIdea> =
        client.from("ideas").select {
            filter { eq("user_id", userId) }
        }.decodeList<CloudIdea>()

    suspend fun create(userId: String, idea: Idea) {
        client.from("ideas").insert(idea.toCloud(userId))
    }

    suspend fun update(userId: String, idea: Idea) {
        client.from("ideas").update(idea.toCloud(userId)) {
            filter {
                eq("id", idea.id)
                eq("user_id", userId)
            }
        }
    }

    suspend fun delete(userId: String, ideaId: String) {
        client.from("ideas").delete {
            filter {
                eq("id", ideaId)
                eq("user_id", userId)
            }
        }
    }

    /**
     * Upload local ideas one by one. This deliberately does not delete or clear
     * SQLite data; the caller should only mark migration complete after every
     * upload succeeds. Existing UUIDs are preserved for safe retry.
     */
    suspend fun migrateLocalIdeas(userId: String, localIdeas: List<Idea>) {
        val existingIds = listForUser(userId).map { it.id }.toSet()
        localIdeas.filterNot { it.id in existingIds }.forEach { idea ->
            create(userId, idea)
        }
    }

    private fun Idea.toCloud(userId: String) = CloudIdea(
        id = id,
        userId = userId,
        name = name,
        problem = problem,
        audience = audience,
        solution = solution,
        category = category,
        notes = notes,
        stage = stage,
        tasks = tasks.map {
            CloudRoadmapTask(
                id = it.id,
                title = it.title,
                description = it.description,
                done = it.done
            )
        }
    )
}
