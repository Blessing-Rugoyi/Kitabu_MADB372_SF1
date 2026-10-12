# Kitabu — Library Book Rental & Reservation System (MADB372 SF1)

Native Android app written in **Kotlin** with **Jetpack Compose (Material 3)**, **Room**,
**ViewModel + StateFlow**, **Navigation Compose** and **Kotlin Coroutines/Flow**.

## Open & run
1. Open the `Kitabu_MADB372_SF1` folder in **Android Studio Ladybug (2024.2) or newer**.
2. Let Gradle sync (it downloads Gradle 8.9 and all dependencies; JDK 17 is bundled with Studio).
3. Run the `app` configuration on an emulator/device with **API 26+**.

The database is pre-populated on first launch with 12 sample books and 3 sample bookings.
To reset the data, uninstall the app or clear its storage.

## Troubleshooting: "Kitabu isn't responding"
1. Use an **x86_64** emulator image (API 34 recommended) with hardware acceleration enabled, at least 4 GB RAM,
   and run **Device Manager -> Wipe Data / Cold Boot** once. Software-emulated emulators are too slow for Compose.
2. Open **Logcat**, select the app's process, and search for `ANR in` and `StrictMode`. Debug builds log any
   main-thread disk/network work as a `StrictMode` warning.
3. Run **Build -> Clean Project**, then **Run** again after uninstalling the old app.

## Architecture
```
ui/           Compose screens, ViewModels, navigation, theme        (presentation)
domain/       models, RentalPolicy (date rules), InputSanitizer,    (business rules)
              LibraryRepository interface
data/         Room entities, DAOs, converters, database,            (persistence)
              LibraryRepositoryImpl, mappers
di/           AppContainer + ViewModel factory (manual DI)
```
Data flows one way: **Room Flow → Repository → ViewModel StateFlow → Compose UI**. The UI only sends
events up (`onReserve`, `onRenew`, ...), so badges and the dashboard update automatically.

## Booking lifecycle
| Action | Rule | Database effect (one transaction) |
| --- | --- | --- |
| Reserve | Book must be available (atomic claim) | insert `PENDING` booking, `isAvailable = 0` |
| Confirm pickup | Booking must be `PENDING` | `PENDING → ACTIVE` |
| Renew | `ACTIVE`, not overdue, max 42 days total | deadline + 7 days |
| Return | Booking must be `ACTIVE` | `ACTIVE → RETURNED`, `isAvailable = 1` |
| Cancel | Booking must be `PENDING` | delete booking, `isAvailable = 1` |

## Notes / assumptions
- No sign-in (per the wireframe notes): one local member is assumed.
- "Confirm pickup" is an addition that completes the lifecycle between Reserve and Renew/Return.
- Status is always shown as text + icon, never by colour alone; action buttons have text labels.
- Theme is light-only to match the wireframe palette (navy `#102A43`, teal `#0F766E`, amber `#F59E0B`).



## Tests
`./gradlew test` runs unit tests for `RentalPolicy` (dates/renewal rules) and `InputSanitizer`.
