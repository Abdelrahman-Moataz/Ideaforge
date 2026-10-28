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
    var ideas by remember { mutableStateOf(store.all()) }
    var selectedId by remember { mutableStateOf<String?>(null) }
    var creating by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var problem by remember { mutableStateOf("") }
    var audience by remember { mutableStateOf("") }
    var solution by remember { mutableStateOf("") }
    val selected = ideas.firstOrNull { it.id == selectedId }
    fun refresh() { ideas = store.all() }
    MaterialTheme(colorScheme = darkColorScheme(primary = Accent, background = Background, surface = Panel)) {
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
                    Spacer(Modifier.weight(1f))
                    Button(onClick = {
                        val idea = Idea(name = name.trim(), problem = problem.trim(), audience = audience.trim(), solution = solution.trim())
                        store.save(idea); refresh(); selectedId = idea.id
                        name = ""; problem = ""; audience = ""; solution = ""; creating = false
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
                    item { OutlinedButton(onClick = { store.delete(selected.id); refresh(); selectedId = null }, modifier = Modifier.fillMaxWidth()) { Text("Delete idea") } }
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
                    item { Text("Ideas are saved locally on this device. Market validation and AI features are not connected in this MVP.", color = Muted, fontSize = 12.sp) }
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
