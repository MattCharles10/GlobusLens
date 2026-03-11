<div align="center">

# GlobusLens - Smart Shopping Assistant 📱

![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-blue.svg)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%2520Compose-2024.04.01-brightgreen.svg)
![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)
![API Level](https://img.shields.io/badge/API-24%252B-brightgreen.svg?style=flat)


## Scan. Translate. Shop.

Your ultimate shopping companion - scan product labels, translate text, and manage shopping lists

</div>

---

**GlobusLens** is an Android application that helps users scan product labels, translate ingredients, and manage shopping information using modern Android technologies.

Built with **Kotlin**, **Jetpack Compose**, and **ML Kit**, the app provides fast scanning, translation, and product organization features.

---

## ✨ Features

- 📷 **Text Scanner**  
  Scan product labels using **Google ML Kit Text Recognition**

- 🔎 **Barcode Scanner**  
  Supports **UPC, EAN, and QR codes**

- 🌐 **Translation**  
  Multi-language support using **LibreTranslate** and **MyMemory APIs**

- 📦 **Offline Mode**  
  Built-in dictionary for common food terms

- 🛒 **Shopping Lists**  
  Create and manage shopping lists

- ⭐ **Favorites**  
  Save and organize favorite products

- 🌙 **Dark / Light Theme**  
  Automatic theme switching

- 🎨 **Material 3 Design**  
  Modern UI with smooth animations

---

## 🛠 Tech Stack

| Category | Technology |
|--------|-------------|
| Language | Kotlin 1.9.22 |
| UI | Jetpack Compose 2024.04.01 |
| Architecture | MVVM + Clean Architecture |
| Dependency Injection | Dagger Hilt 2.50 |
| Database | Room 2.6.1 |
| Camera | CameraX 1.3.2 |
| ML | ML Kit Text Recognition 16.0.0 |
| Barcode | ML Kit Barcode Scanning 17.3.0 |
| Networking | Retrofit 2.9.0 + OkHttp 4.12.0 |
| Image Loading | Coil 2.5.0 |

---

## 📁 Project Structure

```
app/
├── src/main/java/com/globuslens/
│
├── ui/           # Screens, UI components, theme
├── viewmodel/    # ViewModels
├── navigation/   # Navigation graph
├── camera/       # CameraX & ML Kit integration
├── database/     # Room entities and DAOs
├── repository/   # Repository pattern
├── network/      # API services
└── utils/        # Utilities & extensions
```

---

## 🚀 Getting Started

### Prerequisites

- Android Studio **Hedgehog (2023.1.1+)**
- **JDK 17**
- **Android SDK 34**

---

### Installation

Clone the repository

```bash
git clone https://github.com/MattCharles10/GlobusLens.git
```

Open the project in **Android Studio** and build/run the app.

---

## 📦 Build APK

### Debug APK

```bash
./gradlew assembleDebug
```

### Release APK

```bash
./gradlew assembleRelease
```

APK location:

```
app/build/outputs/apk/debug/
app/build/outputs/apk/release/
```

---

## 🧪 Testing

Run unit tests

```bash
./gradlew test
```

Run instrumentation tests

```bash
./gradlew connectedAndroidTest
```

---

## 📄 License

This project is licensed under the **Apache License 2.0**.

See the LICENSE file for the specific language governing permissions
and limitations under the License.

---

## 👤 Contact

**Developer:** Mathew Charles 

GitHub: https://github.com/MattCharles10

---

<div align="center">

Built with ❤️ using **Kotlin** and **Jetpack Compose**

</div>


