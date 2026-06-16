# Ludo Master - Admin Web Console
A responsive, high-fidelity production-ready Admin Dashboard engineered in **Flutter Web** to manage users, ban malicious actors, configure championships, observe retention analytics, and audit server economy circulation indices.

---

## 🛠️ Project Structure
```tree
admin_web_dashboard/
├── pubspec.yaml                 # Dependencies and Flutter configurations
├── web/
│   └── index.html               # Web bootstrap container entry and styles
└── lib/
    ├── main.dart                # Application entry and centralized M3 dark theme configuration
    ├── models/
    │   └── admin_models.dart    # Strong domain schemas (LudoUser, Tournament, AuditLog)
    ├── services/
    │   └── mock_api_service.dart# Real-time state manager and CRUD controller database singleton
    └── screens/
        ├── dashboard_screen.dart# Master responsive layout containing sidebars, tabs, and headers
        ├── analytics_view.dart  # Canvas telemetry graphics, concurrency, and node charts
        ├── users_view.dart      # Account filters lists, gold override forms, and block actions
        ├── tournaments_view.dart# Active championship grids, slot trackers, and live creator forms
        └── coins_view.dart      # Gold currency indices trackers and direct ledger injections
```

---

## 🚀 Interactive Quick Start Guide (Flutter Web)

### 1. Prerequisites
Make sure you have Flutter installed on your machine (`sdk >= 3.0.0`).
You can verify your installation by running:
```bash
flutter --version
```

### 2. Install Project Dependencies
Navigate to the root directory of the web dashboard and pull packages:
```bash
cd admin_web_dashboard
flutter pub get
```

### 3. Run on Local Web Server
Launch the compiler and boot a server in your browser:
```bash
flutter run -d chrome
```

### 4. Build Optimized Release Artifacts
When ready to deploy on hosting services like Firebase Hosting, Vercel, or Netlify, compile the optimized release builds:
```bash
flutter build web --release
```
The output HTML/JS artifacts will be located in **`build/web`**!

---

## 💎 Core Features Integrated & Fully Functional

1. **Telemetry & Live Analytics (`analytics_view.dart`)**:
   - Dynamic Custom Canvas line charts tracking concurrent daily active users vs. intelligent bot ensembles over time.
   - Interactive KPI cards calculating system-wide active retention percentage indices.
   - Master-region node queue tables tracking online, warning, and offline service levels.

2. **User Profiles Management & Blocks (`users_view.dart`)**:
   - Advanced string search filters parsing player IDs, emails, names, or VIP standings.
   - Modals executing instant **Account Bans** or profile overrides cleanly keeping users out of active matchmakings.
   - Profile visual cards highlighting matches played, custom win ratio metrics, and active dice styles.

3. **Championship Campaign Room (`tournaments_view.dart`)**:
   - Form parameters dynamically creating championships with custom Prize Pool payouts, entrance registration fees, max player limits, and release delays.
   - Real-time linear progress sliders monitoring current slot capacity registers.
   - Cancellation triggers instantly destroying specific Lobby slots.

4. **Gold Currency Inflation Ledger (`coins_view.dart`)**:
   - Overall indicators keeping precise track of total Gold Coins and Emerald Gems in public circulation across database servers.
   - Detailed ledger lists highlighting custom overrides, manual transaction logs, and security audit stamps.
   - Manual balance overwriting form allowing immediate administrative ledger injections.

---
**Core engineered with dedication by Athram Kamal. High-fidelity layouts adhering to Material 3 standard specifications.**
