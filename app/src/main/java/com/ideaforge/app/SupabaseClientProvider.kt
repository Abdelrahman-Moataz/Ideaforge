package com.ideaforge.app

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest

/**
 * Client-side Supabase configuration.
 *
 * Only use the project's publishable key here. Database access must be protected
 * by Row Level Security policies; never ship a secret/service_role key in an APK.
 */
object SupabaseClientProvider {
    val isConfigured: Boolean
        get() = BuildConfig.SUPABASE_URL.isNotBlank() &&
            BuildConfig.SUPABASE_PUBLISHABLE_KEY.isNotBlank()

    val client by lazy {
        check(isConfigured) {
            "Supabase is not configured. Add SUPABASE_URL and SUPABASE_PUBLISHABLE_KEY to local.properties."
        }
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_PUBLISHABLE_KEY
        ) {
            install(Auth)
            install(Postgrest)
        }
    }
}
