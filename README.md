# MoneyUpApplication - Personal Finance & Budget Tracker

**Money_Up** is an Android application developed in Kotlin using Android Studio and Room Database. The app helps users track their monthly budgets, log daily expenses, filter spending within specific timeframes, and categorize expenses with descriptions and receipt images.

---

## ✨ Innovative features for POE

- 🔍  **Data Export** Allow users to export their financial data for backup, analysis, or external use. This feature is valuable because it ensures data security and enables users to perform deeper analysis or use the data in other tools, such as tax preparation or financial planning software. The minimum and maximum goals is exported for the user in an excel sheet.
- 🧮  **Calculator** Providing a built-in calculator to allow users to perform quick calculations for any amounts they have in mind. This feature is useful to make it easy for users to calculate amounts based on arithmetic operations such as divide, multiply, addition, and subtraction

## 🚀 Features for POE

- 📈  **Graph** line graph showing the sum of money spent per category when user selects a month, presents min and max goals.
- 🔘  **Progress** progress bar demonstrating user stays in between min and max budget goals.
- 🏆  **Gamification** rewards and badges displayed when user meets budget goals.
- 📲  **Phone** application can run on a mobile phone with the help of the APK file.

## 🚀 Features for Part 2

- 🧾 **Add Expenses** with name, amount, date, time, description, category, and photo.
- 📆 **Filter by Date Range** to view expenses during a user-selectable period.
- 📂 **Category-Based Expense Summary** – See total money spent per category during a selected period.
- 📸 **Attach Photos** to expense entries (e.g., receipts or bills).
- 💼 **Monthly Budget Management** – Set minimum and maximum budget per month.
- 📊 **Total Budget Calculation** per user.
- 👤 **User Profile** support (with future login/registration capability).
- 🔄 **Live Data with Flow** – Real-time updates using Kotlin coroutines and Flow.
- 📱 **Bottom Navigation** – Seamless navigation between Home, Budget, Expenses, Profile, and Settings.

---

## 🛠️ Tech Stack

- **Language**: Kotlin
- **Architecture**: MVVM (Model-View-ViewModel)
- **Persistence**: Room Database (SQLite) for Part 2, Real Time Firebase (online) for POE
- **Async Handling**: Kotlin Coroutines + Flow
- **UI**: Material Design Components
- **Tools**: Android Studio, Jetpack Libraries

---
# ▶️ How to run the app from a zipped folder:


To run the **Money_Up** Android app from a zipped project folder, first extract the contents of the `.zip` file to a location on your computer.
Open **Android Studio**, click **"Open"**, and navigate to the extracted folder — make sure to select the main project directory (where the `build.gradle` or `settings.gradle` file is located)
. Once opened, Android Studio will begin syncing the Gradle files; if it doesn’t happen automatically, you can trigger it manually via **File > Sync Project with Gradle Files**.
After syncing completes, connect your Android device or start an emulator, then click the green **Run** ▶ button at the top of Android Studio to build and launch the app.
Make sure you have a stable internet connection during the first launch to download any missing dependencies.
If you encounter issues related to Room database migrations (such as missing columns), uninstall the app from the emulator or device and rebuild it to reset the database.

---

# 🎬 YouTube Link: [https://youtu.be/dsMaCm0MtCc](https://youtu.be/iyrzqjhYKco)

---

# 👥 Group Members:
- Meshaya Munnhar ST10272710
- Panashe Mavhunga ST10393030
- Zoe Heyneke ST10305921
