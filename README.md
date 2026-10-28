# IdeaForge — Kotlin Android App

IdeaForge is a native Android MVP built with Kotlin and Jetpack Compose for capturing ideas and turning them into actionable plans.

## Included
- Dark-themed dashboard and idea cards
- Create ideas with problem, target audience, solution, category, and notes
- Idea details and interactive roadmap checklist
- Local SQLite persistence
- Supabase email/password authentication UI
- Supabase cloud idea schema and repository with user-scoped RLS
- SQLite-to-cloud migration helper that retains local records
- AI Idea Coach screen backed by a Supabase Edge Function and Gemini

## Requirements
- Android Studio (recent stable release)
- JDK 17
- Android SDK 35
- A Supabase project
- A Gemini API key for live AI coaching

## Run the Android app
1. Open this repository in Android Studio.
2. Copy the values from `local.properties.example` into the root `local.properties` file.
3. Set `SUPABASE_URL` to your Supabase project URL and `SUPABASE_PUBLISHABLE_KEY` to your publishable key.
4. Keep `local.properties` untracked; do not commit keys.
5. Run Gradle sync and launch the app.

## Configure Supabase authentication and database
1. In Supabase Authentication settings, enable Email sign-in.
2. Open the SQL Editor and run `supabase/schema.sql`.
3. Keep Row Level Security enabled. The policies restrict each user's idea rows to that user's authenticated ID.
4. Test sign-up/sign-in with an account in your own project.

SQLite data is not deleted during migration. The migration helper checks existing cloud IDs and uploads missing local ideas. The app should still be tested on a device/emulator before relying on cloud migration in production.

## Enable the AI Idea Coach
AI requests run through a Supabase Edge Function so the Gemini API key is not included in the APK or Android source.

1. Create a Gemini API key in Google AI Studio.
2. In the Supabase Dashboard, open **Edge Functions → Secrets** and add a secret named `GEMINI_API_KEY` with the Gemini key as its value. Do not put this key in Android `local.properties` or GitHub.
3. Deploy the `idea-coach` function from this repository. With the Supabase CLI installed and authenticated, run these commands from the repository root:

   ```bash
   supabase login
   supabase link --project-ref jvqjgymftnohgbbcxkoo
   supabase functions deploy idea-coach
   ```

4. Confirm `supabase/config.toml` keeps JWT verification enabled for the function.
5. Sign in to IdeaForge and open **AI Assistant**. Enter an idea or question and tap **Ask AI coach**.

The function calls Gemini's `gemini-2.5-flash` model. Provider availability, quotas, and billing depend on your Google AI Studio/Gemini account. The coach helps structure thinking; it does not independently verify market size, competitor information, or customer demand.

## Security
- Use only the Supabase publishable key in the Android app.
- Never put a Supabase secret key, service-role key, database password, or Gemini API key in Android code or the GitHub repository.
- Keep RLS enabled; a publishable key does not replace user authentication or database policies.
- The AI function requires a signed-in user's JWT.

## Current limitations
- End-to-end Android compilation and runtime behavior have not yet been verified in this environment.
- Cloud idea CRUD still needs full screen-level wiring and device testing.
- AI coaching requires the Edge Function to be deployed and the `GEMINI_API_KEY` secret configured.
- AI responses are generated on demand and are not a substitute for validated market research.
