package com.ideaforge.app

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class RoadmapTask(val id: String = UUID.randomUUID().toString(), val title: String, val description: String = "", val done: Boolean = false)
data class Idea(
    val id: String = UUID.randomUUID().toString(), val name: String, val problem: String,
    val audience: String, val solution: String, val category: String = "Other",
    val createdAt: Long = System.currentTimeMillis(), val stage: String = "Captured",
    val tasks: List<RoadmapTask> = starterTasks(), val notes: String = ""
) { val progress: Int get() = if (tasks.isEmpty()) 0 else tasks.count { it.done } * 100 / tasks.size }

fun starterTasks() = listOf(
    RoadmapTask(title="Define the target customer", description="Describe who experiences the problem and when."),
    RoadmapTask(title="Review existing alternatives", description="List competitors and current workarounds."),
    RoadmapTask(title="Interview 5 potential customers", description="Ask about current behavior, pain points, and willingness to pay."),
    RoadmapTask(title="Write a clear value proposition", description="Explain who you help, which problem you solve, and how."),
    RoadmapTask(title="Choose the smallest testable version", description="Define the minimum scope needed to test the main assumption."),
    RoadmapTask(title="Run a small pilot", description="Test with real users and record what you learn.")
)

class IdeaStore(context: Context) : SQLiteOpenHelper(context, "ideaforge.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE ideas(id TEXT PRIMARY KEY, name TEXT NOT NULL, problem TEXT NOT NULL, audience TEXT NOT NULL, solution TEXT NOT NULL, category TEXT NOT NULL, createdAt INTEGER NOT NULL, stage TEXT NOT NULL, tasks TEXT NOT NULL, notes TEXT NOT NULL)")
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
    fun all(): List<Idea> {
        val result = mutableListOf<Idea>()
        readableDatabase.query("ideas", null, null, null, null, null, "createdAt DESC").use { c ->
            while (c.moveToNext()) result += Idea(
                id=c.getString(c.getColumnIndexOrThrow("id")), name=c.getString(c.getColumnIndexOrThrow("name")),
                problem=c.getString(c.getColumnIndexOrThrow("problem")), audience=c.getString(c.getColumnIndexOrThrow("audience")),
                solution=c.getString(c.getColumnIndexOrThrow("solution")), category=c.getString(c.getColumnIndexOrThrow("category")),
                createdAt=c.getLong(c.getColumnIndexOrThrow("createdAt")), stage=c.getString(c.getColumnIndexOrThrow("stage")),
                tasks=decode(c.getString(c.getColumnIndexOrThrow("tasks"))), notes=c.getString(c.getColumnIndexOrThrow("notes"))
            )
        }
        return result
    }
    fun save(i: Idea) {
        val v = ContentValues().apply {
            put("id",i.id); put("name",i.name); put("problem",i.problem); put("audience",i.audience); put("solution",i.solution)
            put("category",i.category); put("createdAt",i.createdAt); put("stage",i.stage); put("tasks",encode(i.tasks)); put("notes",i.notes)
        }
        writableDatabase.insertWithOnConflict("ideas", null, v, SQLiteDatabase.CONFLICT_REPLACE)
    }
    fun delete(id: String) { writableDatabase.delete("ideas", "id=?", arrayOf(id)) }
    private fun encode(tasks: List<RoadmapTask>): String {
        val a=JSONArray(); tasks.forEach { a.put(JSONObject().put("id",it.id).put("title",it.title).put("description",it.description).put("done",it.done)) }; return a.toString()
    }
    private fun decode(raw: String): List<RoadmapTask> = try {
        val a=JSONArray(raw); (0 until a.length()).map { n -> val o=a.getJSONObject(n); RoadmapTask(o.optString("id",UUID.randomUUID().toString()),o.optString("title"),o.optString("description"),o.optBoolean("done")) }
    } catch (_: Exception) { starterTasks() }
}
