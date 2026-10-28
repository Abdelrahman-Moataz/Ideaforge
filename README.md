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

## Requirements
- Android Studio (recent stable release)
- JDK 17
- Android SDK 35

## Run
1. Open this repository in Android Studio.
2. Allow Gradle sync to finish.
3. Run the `app` configuration on an emulator or Android device.

## Current limitations
- No authentication or account management is connected.
- Data is stored locally on the device; cloud sync and backup are not configured.
- The assistant provides a built-in checklist and does not call an AI API.
- Market research, competitor data, and demand validation are not automated.
- Visual mockup concepts may show screens or capabilities that are not implemented in this MVP.

## Suggested next steps
1. Connect authentication and cloud sync (for example, Supabase).
2. Integrate a real AI service through a secure backend.
3. Add idea editing, export/share, and tests.
4. Add research sources and citations to market validation.
