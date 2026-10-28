package com.ideaforge.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

private val Accent = Color(0xFF9A8CFF)
private val Background = Color(0xFF0B0D1A)
private val Panel = Color(0xFF1A1D34)
private val Muted = Color(0xFF9EA3C5)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { IdeaForgeApp(IdeaStore(this)) }
    }
}

@Composable
private fun IdeaForgeApp(store: IdeaStore) {
    var isAuthenticated by remember { mutableStateOf(false) }
    var signedInEmail by remember { mutableStateOf("") }
    var cloudMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var ideas by remember { mutableStateOf(store.all()) }
    var selectedId by remember { mutableStateOf<String?>(null) }
    var creating by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var problem by remember { mutableStateOf("") }
    var audience by remember { mutableStateOf("") }
    var solution by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Personal") }
    var notes by remember { mutableStateOf("") }
    var screen by remember { mutableStateOf("home") }
    var aiPrompt by remember { mutableStateOf("") }
    var aiResponse by remember { mutableStateOf("") }
    var aiLoading by remember { mutableStateOf(false) }
    val selected = ideas.firstOrNull { it.id == selectedId }
    fun refresh() { ideas = store.all() }
    MaterialTheme(colorScheme = darkColorScheme(primary = Accent, background = Background, surface = Panel)) {
        if (!isAuthenticated) {
            AuthScreen { _, email ->
                signedInEmail = email
                scope.launch {
                    try {
                        val auth = SupabaseAuthRepository()
                        val userId = auth.currentUserId() ?: error("Signed in, but no user session was found.")
                        CloudIdeaRepository().migrateLocalIdeas(userId, store.all())
                        cloudMessage = "Signed in. Existing local ideas were safely checked for migration."
                        refresh()
                        isAuthenticated = true
                    } catch (e: Exception) {
                        cloudMessage = e.message ?: "Cloud migration failed. Your local ideas remain on this device."
                        // Don't delete local data or block the user from accessing it.
                        isAuthenticated = true
                    }
                }
            }
        } else {
        Surface(Modifier.fillMaxSize(), color = Background) {
            when {
                creating -> Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("‹ Back", color = Accent, modifier = Modifier.clickable { creating = false })
                    Text("Capture an idea", fontSize = 27.sp, fontWeight = FontWeight.Bold)
                    Text("Start rough. Improve it as you learn.", color = Muted)
                    IdeaInput("IDEA NAME *", name, { name = it }, "e.g. Trustworthy home services")
                    IdeaInput("PROBLEM *", problem, { problem = it }, "What problem does it solve?", 3)
                    IdeaInput("TARGET CUSTOMER", audience, { audience = it }, "Who has this problem?")
                    IdeaInput("PROPOSED SOLUTION", solution, { solution = it }, "How would your idea help?", 3)
                    Text("CATEGORY", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("Personal", "Work", "Learning", "Other").forEach { item -> FilterChip(selected = category == item, onClick = { category = item }, label = { Text(item) }) } }
                    IdeaInput("NOTES (OPTIONAL)", notes, { notes = it }, "Extra thoughts or assumptions", 2)
                    Spacer(Modifier.weight(1f))
                    Button(onClick = {
                        val idea = Idea(name = name.trim(), problem = problem.trim(), audience = audience.trim(), solution = solution.trim(), category = category, notes = notes.trim())
                        store.save(idea); refresh(); selectedId = idea.id
                        name = ""; problem = ""; audience = ""; solution = ""; notes = ""; category = "Personal"; creating = false
                    }, enabled = name.isNotBlank() && problem.isNotBlank(), modifier = Modifier.fillMaxWidth().height(52.dp)) {
                        Text("Create my roadmap →")
                    }
                }
                selected != null -> LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    item {
                        Text("‹ Workspace", color = Accent, modifier = Modifier.clickable { selectedId = null })
                        Spacer(Modifier.height(12.dp))
                        Text(selected.name, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        Text(selected.category + " · " + selected.stage, color = Muted)
                    }
                    item {
                        Column(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(18.dp)).padding(16.dp)) {
                            Text("THE PROBLEM", color = Accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(6.dp)); Text(selected.problem)
                            if (selected.audience.isNotBlank()) { Spacer(Modifier.height(14.dp)); Text("TARGET CUSTOMER", color = Accent, fontSize = 11.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(6.dp)); Text(selected.audience) }
                            if (selected.solution.isNotBlank()) { Spacer(Modifier.height(14.dp)); Text("PROPOSED SOLUTION", color = Accent, fontSize = 11.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(6.dp)); Text(selected.solution) }
                        }
                    }
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) { Text("Execution roadmap", fontSize = 20.sp, fontWeight = FontWeight.Bold); Text("${selected.tasks.count { it.done }} of ${selected.tasks.size} tasks complete", color = Muted) }
                            Text("${selected.progress}%", color = Accent, fontWeight = FontWeight.Bold)
                        }
                    }
                    item { LinearProgressIndicator(progress = { selected.progress / 100f }, modifier = Modifier.fillMaxWidth(), color = Accent) }
                    items(selected.tasks, key = { it.id }) { task ->
                        Row(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(14.dp)).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = task.done, onCheckedChange = { checked ->
                                store.save(selected.copy(tasks = selected.tasks.map { if (it.id == task.id) it.copy(done = checked) else it }))
                                refresh()
                            })
                            Column(Modifier.weight(1f)) {
                                Text(task.title, fontWeight = FontWeight.Medium)
                                if (task.description.isNotBlank()) Text(task.description, color = Muted, fontSize = 12.sp)
                            }
                        }
                    }
                    if (selected.notes.isNotBlank()) item { Column(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(16.dp)).padding(16.dp)) { Text("NOTES", color = Accent, fontSize = 11.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(6.dp)); Text(selected.notes) } }
                    item { OutlinedButton(onClick = { store.delete(selected.id); refresh(); selectedId = null }, modifier = Modifier.fillMaxWidth()) { Text("Delete idea") } }
                }
                screen == "assistant" -> LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    item { Text("‹ Workspace", color = Accent, modifier = Modifier.clickable { screen = "home" }); Text("AI Assistant", fontSize = 28.sp, fontWeight = FontWeight.Bold); Text("Get tailored feedback and practical next steps for your idea.", color = Muted) }
                    item {
                        Column(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(20.dp)).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Idea coach", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text("AI coaching powered by Gemini. Treat market claims as hypotheses to validate, not verified research.", color = Muted, fontSize = 12.sp)
                            IdeaInput("WHAT ARE YOU WORKING ON?", aiPrompt, { aiPrompt = it }, "Describe your idea or challenge", 4)
                            Button(onClick = {
                                if (aiPrompt.isBlank()) {
                                    aiResponse = "Describe your idea or ask a specific question first."
                                } else {
                                    aiLoading = true
                                    aiResponse = ""
                                    scope.launch {
                                        try {
                                            aiResponse = IdeaCoachRepository().ask(
                                                prompt = aiPrompt,
                                                ideaContext = selected?.let {
                                                    "Idea: ${it.name}\nProblem: ${it.problem}\nAudience: ${it.audience}\nSolution: ${it.solution}\nCategory: ${it.category}"
                                                }.orEmpty()
                                            )
                                        } catch (e: Exception) {
                                            aiResponse = "I couldn't reach the AI coach. Check your connection, sign-in session, and Supabase Edge Function setup.\n\nDetails: ${e.message ?: "Unknown error"}"
                                        } finally {
                                            aiLoading = false
                                        }
                                    }
                                }
                            }, enabled = !aiLoading, modifier = Modifier.fillMaxWidth()) {
                                Text(if (aiLoading) "Thinking…" else "Ask AI coach")
                            }
                        }
                    }
                    if (aiLoading) item { Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) { CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Accent, strokeWidth = 2.dp); Text("Thinking through your idea…", color = Muted) } }
                    if (aiResponse.isNotBlank()) item { Column(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(16.dp)).padding(16.dp)) { Text("AI COACH RESPONSE", color = Accent, fontWeight = FontWeight.Bold, fontSize = 11.sp); Spacer(Modifier.height(10.dp)); Text(aiResponse) } }
                    item { Text("Quick prompts", fontWeight = FontWeight.Bold, fontSize = 18.sp) }
                    items(listOf("How can I test demand cheaply?", "What assumptions should I validate?", "How should I define my first MVP?")) { prompt ->
                        OutlinedButton(onClick = { aiPrompt = prompt; aiResponse = "" }, modifier = Modifier.fillMaxWidth()) { Text(prompt) }
                    }
                }
                screen == "profile" -> LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    item { Text("‹ Workspace", color = Accent, modifier = Modifier.clickable { screen = "home" }); Text("Profile", fontSize = 28.sp, fontWeight = FontWeight.Bold); Text("Your IdeaForge workspace", color = Muted) }
                    item { Column(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(20.dp)).padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(Modifier.size(56.dp).background(Accent, RoundedCornerShape(28.dp)), contentAlignment = Alignment.Center) { Text("IF", color = Background, fontSize = 20.sp, fontWeight = FontWeight.Bold) }
                        Text("Local workspace", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(signedInEmail.ifBlank { "Signed-in workspace" }, color = Muted)
                        Divider(color = Muted.copy(alpha = 0.25f))
                        Text("Total ideas: ${ideas.size}")
                        Text("Tasks completed: ${ideas.sumOf { idea -> idea.tasks.count { it.done } }}")
                        Text("Tasks remaining: ${ideas.sumOf { idea -> idea.tasks.count { !it.done } }}")
                    } }
                    item { Text("Settings & capabilities", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
                    item { Text("IdeaForge uses Supabase authentication and cloud features. AI coaching requires the deployed idea-coach Edge Function and a server-side Gemini API key.", color = Muted) }
                }
                else -> LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(44.dp).background(Accent, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) { Text("IF", color = Background, fontWeight = FontWeight.Black) }
                            Spacer(Modifier.width(12.dp))
                            Column { Text("IdeaForge", fontSize = 24.sp, fontWeight = FontWeight.Bold); Text("From idea to reality", color = Muted) }
                        }
                    }
                    item {
                        Column(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(22.dp)).padding(20.dp)) {
                            Text("YOUR NEXT BIG THING", color = Accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(10.dp)); Text("Every great product starts with an idea.", fontSize = 27.sp, fontWeight = FontWeight.Bold, lineHeight = 33.sp)
                            Spacer(Modifier.height(8.dp)); Text("Capture it. Test it. Build it.", color = Muted)
                            Spacer(Modifier.height(18.dp))
                            Button(onClick = { creating = true }, modifier = Modifier.fillMaxWidth().height(50.dp)) { Text("+  Create a new idea") }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(onClick = { screen = "assistant" }, modifier = Modifier.weight(1f)) { Text("✦ AI Assistant") }
                                OutlinedButton(onClick = { screen = "profile" }, modifier = Modifier.weight(1f)) { Text("Profile") }
                            }
                        }
                    }
                    item { Text("Your workspace", fontSize = 20.sp, fontWeight = FontWeight.Bold); Text("${ideas.size} saved projects", color = Muted) }
                    if (ideas.isEmpty()) item { Text("No ideas yet. Create your first project to begin.", color = Muted) }
                    items(ideas, key = { it.id }) { idea ->
                        Column(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(18.dp)).clickable { selectedId = idea.id }.padding(16.dp)) {
                            Text(idea.name, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(5.dp)); Text(idea.problem, color = Muted, maxLines = 2)
                            Spacer(Modifier.height(12.dp)); Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Roadmap progress", color = Muted, fontSize = 12.sp); Spacer(Modifier.weight(1f)); Text("${idea.progress}%", color = Accent, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(6.dp)); LinearProgressIndicator(progress = { idea.progress / 100f }, modifier = Modifier.fillMaxWidth(), color = Accent)
                        }
                    }
                    item { Text("Your ideas are saved locally and migration preserves the original SQLite records. AI responses are generated on demand.", color = Muted, fontSize = 12.sp) }
                }
            }
        }
        }
    }
}

@Composable
private fun IdeaInput(label: String, value: String, onValueChange: (String) -> Unit, placeholder: String, minLines: Int = 1) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        OutlinedTextField(value = value, onValueChange = onValueChange, modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = Muted) }, minLines = minLines, shape = RoundedCornerShape(14.dp))
    }
}
