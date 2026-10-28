# IdeaForge — Kotlin Android App

IdeaForge is a native Android MVP built with Kotlin and Jetpack Compose for capturing ideas and turning them into an actionable checklist.

## Included
- Dark-themed home dashboard and idea cards
- Create ideas with title, problem, target audience, proposed solution, category, and notes
- Idea details and deletion
- Interactive roadmap checklist with progress tracking
- Local SQLite persistence
- Assistant screen with a reusable idea-validation checklist (offline demo, not a live AI model)
- Profile/workspace screen with idea and task statistics
- Supabase Kotlin client dependencies and client provider
- SQL schema for a user-scoped cloud ideas table with Row Level Security (RLS)

## Requirements
- Android Studio (recent stable release)
- JDK 17
- Android SDK 35

## Run locally
1. Open this repository in Android Studio.
2. Copy `local.properties.example` values into your local `local.properties` file (keep `local.properties` untracked).
3. Set `SUPABASE_URL=https://jvqjgymftnohgbbcxkoo.supabase.co`.
4. Replace the example value for `SUPABASE_PUBLISHABLE_KEY` with your Supabase publishable key.
5. Run the Gradle sync, then run the `app` configuration on an emulator or Android device.

The Supabase client is initialized in `SupabaseClientProvider.kt`. The current app screens still use local SQLite; sign-in UI, cloud repository operations, and SQLite-to-cloud migration have not yet been connected.

## Configure Supabase database
1. Open the Supabase project SQL Editor.
2. Run `supabase/schema.sql` to create the `public.ideas` table and its RLS policies.
3. In Supabase Authentication settings, enable the sign-in method you want before implementing the app's sign-in screen.
4. Verify the table is available through the Data API. Keep RLS enabled.

The schema requires authenticated users and restricts each user to their own ideas. Do not disable RLS to make requests work.

## Security
- The mobile app must use only a Supabase publishable key.
- Never place a Supabase secret or `service_role` key, database password, or other server secret in the app or repository.
- The publishable key is not a replacement for RLS and correct database permissions.

## Current limitations
- Sign-in and account management screens are not implemented yet.
- Idea data is currently stored locally on the device; cloud sync and migration are not connected yet.
- The assistant provides a built-in checklist and does not call an AI API.
- Market research, competitor data, and demand validation are not automated.
