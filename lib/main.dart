import 'package:flutter/material.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

// IdeaForge Supabase project credentials.
// The publishable key is intended for client-side use.
// Protect your data with Supabase Row Level Security (RLS) policies.
const String supabaseUrl = 'https://jvqjgymftnohgbbcxkoo.supabase.co';
const String supabasePublishableKey =
    'sb_publishable_7Ihrd4QdbObH8khgUSqlKQ_IVwfsQEO';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();

  await Supabase.initialize(
    url: supabaseUrl,
    anonKey: supabasePublishableKey,
  );

  runApp(const IdeaForgeApp());
}

class IdeaForgeApp extends StatelessWidget {
  const IdeaForgeApp({super.key});

  @override
  Widget build(BuildContext context) => MaterialApp(
    title: 'IdeaForge',
    debugShowCheckedModeBanner: false,
    theme: ThemeData(
      brightness: Brightness.dark,
      scaffoldBackgroundColor: const Color(0xFF0B0D1A),
      colorScheme: ColorScheme.fromSeed(
        seedColor: const Color(0xFF9A8CFF),
        brightness: Brightness.dark,
      ),
      useMaterial3: true,
    ),
    home: StreamBuilder<AuthState>(
      stream: Supabase.instance.client.auth.onAuthStateChange,
      builder: (context, snapshot) =>
      Supabase.instance.client.auth.currentSession == null
          ? const AuthPage()
          : const WorkspacePage(),
    ),
  );
}

class AuthPage extends StatefulWidget {
  const AuthPage({super.key});

  @override
  State<AuthPage> createState() => _AuthPageState();
}

class _AuthPageState extends State<AuthPage> {
  final email = TextEditingController();
  final password = TextEditingController();

  bool signup = false;
  bool busy = false;
  String error = '';

  Future<void> submit() async {
    if (email.text.trim().isEmpty || password.text.isEmpty) {
      setState(() => error = 'Enter your email and password.');
      return;
    }

    setState(() {
      busy = true;
      error = '';
    });

    try {
      final auth = Supabase.instance.client.auth;

      if (signup) {
        await auth.signUp(
          email: email.text.trim(),
          password: password.text,
        );
      } else {
        await auth.signInWithPassword(
          email: email.text.trim(),
          password: password.text,
        );
      }
    } catch (e) {
      if (mounted) setState(() => error = e.toString());
    } finally {
      if (mounted) setState(() => busy = false);
    }
  }

  @override
  void dispose() {
    email.dispose();
    password.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) => Scaffold(
    body: Center(
      child: SingleChildScrollView(
        padding: const EdgeInsets.all(24),
        child: ConstrainedBox(
          constraints: const BoxConstraints(maxWidth: 440),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              const Icon(
                Icons.lightbulb_outline,
                size: 64,
                color: Color(0xFF9A8CFF),
              ),
              const Text(
                'IdeaForge',
                textAlign: TextAlign.center,
                style: TextStyle(
                  fontSize: 32,
                  fontWeight: FontWeight.bold,
                ),
              ),
              const SizedBox(height: 8),
              Text(
                signup ? 'Create your account' : 'From idea to reality',
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 28),
              TextField(
                controller: email,
                keyboardType: TextInputType.emailAddress,
                decoration: const InputDecoration(labelText: 'Email'),
              ),
              const SizedBox(height: 12),
              TextField(
                controller: password,
                obscureText: true,
                decoration: const InputDecoration(labelText: 'Password'),
              ),
              if (error.isNotEmpty)
                Padding(
                  padding: const EdgeInsets.only(top: 12),
                  child: Text(
                    error,
                    style: const TextStyle(color: Colors.redAccent),
                  ),
                ),
              const SizedBox(height: 18),
              FilledButton(
                onPressed: busy ? null : submit,
                child: Text(
                  busy
                      ? 'Please wait…'
                      : signup
                      ? 'Create account'
                      : 'Sign in',
                ),
              ),
              TextButton(
                onPressed: busy
                    ? null
                    : () => setState(() {
                  signup = !signup;
                  error = '';
                }),
                child: Text(
                  signup
                      ? 'Already registered? Sign in'
                      : 'Create an account',
                ),
              ),
            ],
          ),
        ),
      ),
    ),
  );
}

class WorkspacePage extends StatefulWidget {
  const WorkspacePage({super.key});

  @override
  State<WorkspacePage> createState() => _WorkspacePageState();
}

class _WorkspacePageState extends State<WorkspacePage> {
  final client = Supabase.instance.client;

  List<Map<String, dynamic>> ideas = [];
  bool loading = true;
  String error = '';
  int tab = 0;

  @override
  void initState() {
    super.initState();
    loadIdeas();
  }

  Future<void> loadIdeas() async {
    if (!mounted) return;

    setState(() {
      loading = true;
      error = '';
    });

    try {
      final user = client.auth.currentUser;
      if (user == null) {
        setState(() => ideas = []);
        return;
      }

      final rows = await client
          .from('ideas')
          .select()
          .eq('user_id', user.id)
          .order('created_at', ascending: false);

      if (mounted) {
        setState(() {
          ideas = List<Map<String, dynamic>>.from(rows);
        });
      }
    } catch (e) {
      if (mounted) setState(() => error = e.toString());
    } finally {
      if (mounted) setState(() => loading = false);
    }
  }

  Future<void> createIdea() async {
    final name = TextEditingController();
    final problem = TextEditingController();
    final audience = TextEditingController();
    final solution = TextEditingController();
    final notes = TextEditingController();

    String category = 'Personal';

    final saved = await showDialog<bool>(
      context: context,
      builder: (ctx) => StatefulBuilder(
        builder: (ctx, refresh) => AlertDialog(
          title: const Text('Capture an idea'),
          content: SingleChildScrollView(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                TextField(
                  controller: name,
                  decoration: const InputDecoration(labelText: 'Idea name *'),
                ),
                TextField(
                  controller: problem,
                  minLines: 2,
                  maxLines: 4,
                  decoration: const InputDecoration(labelText: 'Problem *'),
                ),
                TextField(
                  controller: audience,
                  decoration:
                  const InputDecoration(labelText: 'Target customer'),
                ),
                TextField(
                  controller: solution,
                  minLines: 2,
                  maxLines: 4,
                  decoration:
                  const InputDecoration(labelText: 'Proposed solution'),
                ),
                DropdownButtonFormField<String>(
                  value: category,
                  decoration: const InputDecoration(labelText: 'Category'),
                  items: ['Personal', 'Work', 'Learning', 'Other']
                      .map(
                        (v) => DropdownMenuItem(
                      value: v,
                      child: Text(v),
                    ),
                  )
                      .toList(),
                  onChanged: (v) =>
                      refresh(() => category = v ?? 'Personal'),
                ),
                TextField(
                  controller: notes,
                  decoration: const InputDecoration(labelText: 'Notes'),
                ),
              ],
            ),
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(ctx, false),
              child: const Text('Cancel'),
            ),
            FilledButton(
              onPressed: name.text.trim().isEmpty ||
                  problem.text.trim().isEmpty
                  ? null
                  : () => Navigator.pop(ctx, true),
              child: const Text('Save'),
            ),
          ],
        ),
      ),
    );

    if (saved == true) {
      try {
        final user = client.auth.currentUser;
        if (user == null) throw Exception('You are not signed in.');

        await client.from('ideas').insert({
          'user_id': user.id,
          'name': name.text.trim(),
          'problem': problem.text.trim(),
          'audience': audience.text.trim(),
          'solution': solution.text.trim(),
          'category': category,
          'notes': notes.text.trim(),
          'stage': 'Captured',
          'tasks': [
            {
              'id': 'customer',
              'title': 'Define the target customer',
              'description': 'Describe who has this problem.',
              'done': false,
            },
            {
              'id': 'alternatives',
              'title': 'Review existing alternatives',
              'description': 'List competitors and workarounds.',
              'done': false,
            },
            {
              'id': 'interviews',
              'title': 'Interview 5 potential customers',
              'description': 'Validate the problem with real people.',
              'done': false,
            },
            {
              'id': 'mvp',
              'title': 'Define the smallest MVP',
              'description': 'Choose the smallest testable version.',
              'done': false,
            },
            {
              'id': 'pilot',
              'title': 'Run a small pilot',
              'description': 'Test with real users.',
              'done': false,
            },
          ],
        });

        await loadIdeas();
      } catch (e) {
        showMessage(e.toString());
      }
    }

    name.dispose();
    problem.dispose();
    audience.dispose();
    solution.dispose();
    notes.dispose();
  }

  Future<void> openIdea(Map<String, dynamic> idea) async {
    final tasks = List<Map<String, dynamic>>.from(
      (idea['tasks'] as List? ?? [])
          .map((e) => Map<String, dynamic>.from(e)),
    );

    await showModalBottomSheet<void>(
      context: context,
      isScrollControlled: true,
      builder: (ctx) => StatefulBuilder(
        builder: (ctx, refresh) => Padding(
          padding: EdgeInsets.fromLTRB(
            20,
            24,
            20,
            MediaQuery.of(ctx).viewInsets.bottom + 24,
          ),
          child: ListView(
            shrinkWrap: true,
            children: [
              Text(
                idea['name']?.toString() ?? '',
                style: Theme.of(ctx).textTheme.headlineSmall,
              ),
              const SizedBox(height: 8),
              Text(idea['problem']?.toString() ?? ''),
              if ((idea['audience'] ?? '').toString().isNotEmpty)
                Text('Customer: ${idea['audience']}'),
              if ((idea['solution'] ?? '').toString().isNotEmpty)
                Text('Solution: ${idea['solution']}'),
              const SizedBox(height: 16),
              const Text(
                'Execution roadmap',
                style: TextStyle(fontWeight: FontWeight.bold),
              ),
              ...tasks.asMap().entries.map(
                    (entry) => CheckboxListTile(
                  value: entry.value['done'] == true,
                  title: Text(entry.value['title']?.toString() ?? ''),
                  subtitle:
                  Text(entry.value['description']?.toString() ?? ''),
                  onChanged: (value) async {
                    tasks[entry.key]['done'] = value ?? false;
                    refresh(() {});

                    try {
                      await client
                          .from('ideas')
                          .update({'tasks': tasks})
                          .eq('id', idea['id'])
                          .eq(
                        'user_id',
                        client.auth.currentUser!.id,
                      );
                      await loadIdeas();
                    } catch (e) {
                      showMessage(e.toString());
                    }
                  },
                ),
              ),
              OutlinedButton.icon(
                onPressed: () async {
                  try {
                    await client
                        .from('ideas')
                        .delete()
                        .eq('id', idea['id'])
                        .eq('user_id', client.auth.currentUser!.id);

                    if (ctx.mounted) Navigator.pop(ctx);
                    await loadIdeas();
                  } catch (e) {
                    showMessage(e.toString());
                  }
                },
                icon: const Icon(Icons.delete_outline),
                label: const Text('Delete idea'),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Future<void> askCoach() async {
    final prompt = TextEditingController();
    String answer = '';
    bool busy = false;

    await showModalBottomSheet<void>(
      context: context,
      isScrollControlled: true,
      builder: (ctx) => StatefulBuilder(
        builder: (ctx, refresh) => Padding(
          padding: EdgeInsets.fromLTRB(
            20,
            20,
            20,
            MediaQuery.of(ctx).viewInsets.bottom + 20,
          ),
          child: SingleChildScrollView(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                Text(
                  'Idea Coach',
                  style: Theme.of(ctx).textTheme.headlineSmall,
                ),
                const SizedBox(height: 8),
                const Text(
                  'Challenge assumptions, validate demand, and turn ideas '
                      'into an MVP plan.',
                ),
                const SizedBox(height: 14),
                TextField(
                  controller: prompt,
                  minLines: 3,
                  maxLines: 6,
                  decoration: const InputDecoration(
                    hintText: 'Describe your idea or ask a question…',
                  ),
                ),
                const SizedBox(height: 12),
                FilledButton(
                  onPressed: busy || prompt.text.trim().isEmpty
                      ? null
                      : () async {
                    final question = prompt.text.trim();
                    refresh(() {
                      busy = true;
                      answer = '';
                    });

                    try {
                      final response = await client.functions.invoke(
                        'idea-coach',
                        body: {'prompt': question},
                      );

                      if (response.status < 200 ||
                          response.status >= 300) {
                        throw Exception(
                          response.data is Map
                              ? response.data['error'] ??
                              'AI request failed'
                              : 'AI request failed',
                        );
                      }

                      answer = response.data is Map
                          ? response.data['text']?.toString() ??
                          'Empty AI response'
                          : response.data.toString();
                    } catch (e) {
                      answer = e.toString();
                    } finally {
                      refresh(() => busy = false);
                    }
                  },
                  child: Text(busy ? 'Thinking…' : 'Ask Nemotron'),
                ),
                if (busy)
                  const Padding(
                    padding: EdgeInsets.all(16),
                    child: Center(child: CircularProgressIndicator()),
                  ),
                if (answer.isNotEmpty)
                  Padding(
                    padding: const EdgeInsets.only(top: 16),
                    child: SelectableText(answer),
                  ),
              ],
            ),
          ),
        ),
      ),
    );

    prompt.dispose();
  }

  void showMessage(String message) {
    if (!mounted) return;
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text(message)),
    );
  }

  @override
  Widget build(BuildContext context) => Scaffold(
    appBar: AppBar(
      title: const Text('IdeaForge'),
      actions: [
        IconButton(
          onPressed: () => client.auth.signOut(),
          icon: const Icon(Icons.logout),
          tooltip: 'Sign out',
        ),
      ],
    ),
    floatingActionButton: tab == 0
        ? FloatingActionButton.extended(
      onPressed: createIdea,
      icon: const Icon(Icons.add),
      label: const Text('New idea'),
    )
        : null,
    body: tab == 0
        ? RefreshIndicator(
      onRefresh: loadIdeas,
      child: loading
          ? const Center(child: CircularProgressIndicator())
          : error.isNotEmpty
          ? ListView(
        children: [
          const SizedBox(height: 100),
          Center(child: Text(error)),
          Center(
            child: TextButton(
              onPressed: loadIdeas,
              child: const Text('Retry'),
            ),
          ),
        ],
      )
          : ideas.isEmpty
          ? ListView(
        padding: const EdgeInsets.all(24),
        children: const [
          SizedBox(height: 80),
          Icon(
            Icons.lightbulb_outline,
            size: 64,
            color: Color(0xFF9A8CFF),
          ),
          SizedBox(height: 16),
          Text(
            'Every great product starts with an idea.',
            textAlign: TextAlign.center,
            style: TextStyle(
              fontSize: 22,
              fontWeight: FontWeight.bold,
            ),
          ),
          SizedBox(height: 8),
          Text(
            'Capture your first idea and turn it into '
                'an actionable roadmap.',
            textAlign: TextAlign.center,
          ),
        ],
      )
          : ListView.builder(
        padding: const EdgeInsets.all(16),
        itemCount: ideas.length,
        itemBuilder: (ctx, i) {
          final idea = ideas[i];
          final tasks =
          List<Map<String, dynamic>>.from(
            (idea['tasks'] as List? ?? []).map(
                  (e) => Map<String, dynamic>.from(e),
            ),
          );
          final done = tasks
              .where((t) => t['done'] == true)
              .length;
          final progress =
          tasks.isEmpty ? 0.0 : done / tasks.length;

          return Card(
            child: ListTile(
              onTap: () => openIdea(idea),
              title: Text(
                idea['name']?.toString() ?? '',
                style: const TextStyle(
                  fontWeight: FontWeight.bold,
                ),
              ),
              subtitle: Column(
                crossAxisAlignment:
                CrossAxisAlignment.start,
                children: [
                  Text(
                    idea['problem']?.toString() ?? '',
                    maxLines: 2,
                    overflow: TextOverflow.ellipsis,
                  ),
                  const SizedBox(height: 10),
                  LinearProgressIndicator(
                    value: progress,
                  ),
                  Text(
                    '${(progress * 100).round()}% '
                        'roadmap complete',
                  ),
                ],
              ),
              trailing:
              const Icon(Icons.chevron_right),
            ),
          );
        },
      ),
    )
        : Center(
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Icon(
              Icons.psychology_alt_outlined,
              size: 64,
              color: Color(0xFF9A8CFF),
            ),
            const SizedBox(height: 16),
            const Text(
              'Ask Nemotron to challenge your idea and build '
                  'a practical plan.',
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 16),
            FilledButton.icon(
              onPressed: askCoach,
              icon: const Icon(Icons.auto_awesome),
              label: const Text('Open AI Idea Coach'),
            ),
          ],
        ),
      ),
    ),
    bottomNavigationBar: NavigationBar(
      selectedIndex: tab,
      onDestinationSelected: (value) => setState(() => tab = value),
      destinations: const [
        NavigationDestination(
          icon: Icon(Icons.dashboard_outlined),
          label: 'Workspace',
        ),
        NavigationDestination(
          icon: Icon(Icons.auto_awesome_outlined),
          label: 'AI Coach',
        ),
      ],
    ),
  );
}
