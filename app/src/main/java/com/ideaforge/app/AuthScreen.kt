package com.ideaforge.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(onAuthenticated: (Boolean, String) -> Unit) {
    val accent = Color(0xFF9A8CFF)
    val panel = Color(0xFF1A1D34)
    val muted = Color(0xFF9EA3C5)
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    Column(
        Modifier.fillMaxSize().background(Color(0xFF0B0D1A)).padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("IdeaForge", color = accent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        Text(if (isSignUp) "Create your account" else "Welcome back", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Sign in to access your ideas across sessions.", color = muted)
        Spacer(Modifier.height(24.dp))
        Column(Modifier.fillMaxWidth().background(panel, RoundedCornerShape(20.dp)).padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation(), singleLine = true, modifier = Modifier.fillMaxWidth())
            if (message.isNotBlank()) Text(message, color = muted)
            Button(
                enabled = !busy && email.contains("@") && password.length >= 6,
                onClick = {
                    scope.launch {
                        busy = true
                        message = ""
                        try {
                            val auth = SupabaseAuthRepository()
                            if (isSignUp) {
                                auth.signUp(email, password)
                                message = "Account created. If email confirmation is enabled, verify your email, then sign in."
                                isSignUp = false
                            } else {
                                auth.signIn(email, password)
                                onAuthenticated(true, email.trim())
                            }
                        } catch (e: Exception) {
                            message = e.message ?: "Authentication failed. Please try again."
                        } finally {
                            busy = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (busy) "Please wait…" else if (isSignUp) "Create account" else "Sign in") }
            TextButton(onClick = { isSignUp = !isSignUp; message = "" }, modifier = Modifier.fillMaxWidth()) {
                Text(if (isSignUp) "Already have an account? Sign in" else "New to IdeaForge? Create account")
            }
        }
    }
}
