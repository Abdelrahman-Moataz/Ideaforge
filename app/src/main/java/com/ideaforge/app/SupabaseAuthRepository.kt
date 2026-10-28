package com.ideaforge.app

import io.github.jan.supabase.auth.providers.Email

class SupabaseAuthRepository {
    private val client get() = SupabaseClientProvider.client

    suspend fun signIn(email: String, password: String) {
        client.auth.signInWith(Email) {
            this.email = email.trim()
            this.password = password
        }
    }

    suspend fun signUp(email: String, password: String) {
        client.auth.signUpWith(Email) {
            this.email = email.trim()
            this.password = password
        }
    }

    suspend fun signOut() {
        client.auth.signOut()
    }

    fun currentUserId(): String? = client.auth.currentUserOrNull()?.id
}
