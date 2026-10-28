# IdeaForge — Kotlin Android App

IdeaForge is a native Android starter app built with Kotlin and Jetpack Compose. It helps users capture an idea and turn it into a simple execution roadmap.

## Included
- Dark dashboard and project cards
- Create ideas with problem, audience, and solution fields
- Idea detail screen
- Starter roadmap checklist with interactive completion
- Progress calculation and sample projects
- Local SQLite persistence via `IdeaStore.kt`

## Requirements
- Android Studio (recent stable release)
- JDK 17
- Android SDK 35

## Run
1. Open this project folder in Android Studio.
2. Allow Gradle sync to finish.
3. Run the `app` configuration on an emulator or Android device.

## Current MVP limitations
- Uses local on-device SQLite storage; cloud sync and authentication are not connected.
- Roadmaps are starter templates, not AI-generated research.
- Supabase or other remote backend integration is not yet configured.

## Next steps
1. Add authentication and cloud sync.
2. Connect an AI-backed idea validation and roadmap service.
3. Add web research and citations for competitor and demand claims.
4. Add export/share support and basic analytics.
