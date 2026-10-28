package com.ideaforge.app

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.json.JSONObject

/** Calls the authenticated Supabase Edge Function. The Gemini API key stays server-side. */
class IdeaCoachRepository {
    suspend fun ask(prompt: String, ideaContext: String = ""): String {
        check(SupabaseClientProvider.isConfigured) {
            "Supabase is not configured. Add the URL and publishable key to local.properties."
        }
        val token = SupabaseClientProvider.client.auth.currentAccessTokenOrNull()
            ?: error("Your session has expired. Sign in again to use AI coaching.")
        val url = BuildConfig.SUPABASE_URL.trimEnd('/') + "/functions/v1/idea-coach"
        val payload = JSONObject().put("prompt", prompt).put("ideaContext", ideaContext).toString()
        val http = HttpClient(Android)
        try {
            val response = http.post(url) {
                header("apikey", BuildConfig.SUPABASE_PUBLISHABLE_KEY)
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(payload)
            }
            val raw = response.bodyAsText()
            if (response.status.value !in 200..299) {
                val message = runCatching { JSONObject(raw).optString("error") }.getOrNull()
                error(message?.takeIf { it.isNotBlank() } ?: "AI request failed (${response.status.value}).")
            }
            return JSONObject(raw).optString("text").takeIf { it.isNotBlank() }
                ?: error("The AI service returned an empty response.")
        } finally {
            http.close()
        }
    }
}
