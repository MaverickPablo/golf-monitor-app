# Sunday Tee Monitoring

Android app that finds the best-value Sunday-morning three-ball within 50 miles of Lenham (ME17 2DD), 07:00–12:00, up to £80 per player, and links straight to the booking page.

## Tech Stack and Framework

- **Language:** Kotlin 1.9+
- **UI:** Jetpack Compose Material 3
- **Architecture:** MVVM with Repository pattern
- **Persistence:** Room database for courses and deals cache
- **Preferences:** Jetpack DataStore Preferences for user filters
- **Background work:** WorkManager with `WeekendMonitorWorker` periodic 4h sync + manual pull-to-refresh
- **Networking:** Retrofit 2 + Gson, OkHttp LoggingInterceptor
- **Image loading:** Coil Compose
- **Min SDK:** 26, Target SDK 34, Compile SDK 34
- **Build:** Gradle Kotlin DSL with kapt for Room

Project structure:
```
app/src/main/java/com.golfmonitor/
  config/ AppConfig.kt
  data/db/ Room entities, DAOs, DealDatabase
  data/network/ Retrofit client and UkGolfApiService
  data/provider/ UkGolfApiProvider, GolfNowProvider
  data/preferences/ FilterPreferences DataStore
  repository/ DealRepository
  ui/ DealListScreen, DealCard
  work/ WeekendMonitorWorker
  model/ Course, TeeTimeDeal, CourseDetails
```

## Tee-time data sources

**Course metadata:** UK Golf Course Data API via RapidAPI
- Endpoint: `GET /clubs/nearby?lat=51.462&lng=0.081&radius_km=80`
- Header `X-RapidAPI-Key` supplied via `BuildConfig.UK_GOLF_API_KEY` from `local.properties`
- Fetched by `UkGolfApiProvider.fetchCoursesInSoutheast()`, mapped to `Course` and cached in Room.

**Availability / deals:** GolfNow provider stub
- `GolfNowProvider.fetchWeekendDeals(courses)` is scaffolded.
- Current implementation returns empty list; live integration requires GolfNow/TeeUpNow API credentials and sandbox endpoint.
- No scraping is performed. All data is via official APIs.

**Seed data:** On first launch `SeedData.seedIfEmpty` inserts four demo courses and deals so the UI is populated before live data arrives.

## Alerts and notifications

- No push notifications are implemented yet.
- Pull-to-refresh enqueues a one-time `WeekendMonitorWorker`. On completion the UI shows a Snackbar "Deals refreshed".
- WorkManager periodic job runs every 4h. Notifications for >10% discount deals are a TODO.

## Filters storage

User filters persist via Jetpack DataStore Preferences:
- `max_price` double
- `start_time` string HH:mm
- `end_time` string HH:mm
- `show_discounts_only` boolean

`FilterPreferences` exposes Flows for each key. `DealListScreen` collects them on launch and saves on change. The header shows "Last saved: HH:mm:ss" and a Snackbar confirms saves.

## Known bugs / TODOs

- **Live deals:** `GolfNowProvider.fetchWeekendDeals` returns empty list. Real GolfNow/TeeUpNow integration and booking URL mapping needed.
- **Course images:** `CourseEntity` and `UkGolfClubDto` lack `imageUrl`. Images in `DealCard` never load; placeholder shows only for sample data.
- **Date selector:** No UI to pick a specific weekend date. Deals are filtered only by time window.
- **Booking button:** Opens `deal.bookingUrl` if present; seed data has `bookingUrl = null` so button does nothing for demo deals.
- **Discount calculation:** `discountPercent` is not computed from baseline price in live flow; relies on provider to supply it.
- **Error handling:** Worker retries on exception but does not surface errors to user.
- **Testing:** No unit or instrumentation tests.

## Setup

1. Add API key to `app/local.properties`:
```
ukGolfApiKey=YOUR_RAPIDAPI_KEY
```
2. Open project in Android Studio, Sync Gradle
3. Run on device/emulator API 26+
