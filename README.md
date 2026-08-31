# AniVerse — Capstone Project (Dicoding Menjadi Android Developer Expert)

[![Android CI](https://github.com/nafiulirsad/aniverse-capstone/actions/workflows/android-ci.yml/badge.svg)](https://github.com/nafiulirsad/aniverse-capstone/actions/workflows/android-ci.yml)

Aplikasi katalog anime yang dibangun dengan **Clean Architecture**, **Modularization**
(1 Android Library + 1 Dynamic Feature), **Dependency Injection dengan Koin**, dan
**Reactive Programming dengan Kotlin Flow**.

Sumber data: [**Kitsu API**](https://kitsu.docs.apiary.io) — katalog anime publik ber-format JSON:API.
**Tidak memerlukan API key sama sekali**, jadi proyek bisa langsung di-*build* dan dijalankan.

---

## 📱 Fitur

| # | Fitur | Keterangan |
|---|---|---|
| 1 | **List Item** (`Jelajah`) | Daftar anime terpopuler dalam grid 2 kolom, dengan *pull-to-refresh* dan cache offline (Room). |
| 2 | **Detail Item** | Backdrop *collapsing toolbar*, poster, skor/peringkat/anggota, genre (chip), durasi, rating usia, sinopsis, tombol trailer YouTube, dan tombol favorit. |
| 3 | **List Favorite** | Modul **dynamic feature** terpisah yang membaca database Room secara *reactive*. |
| + | **Search** (fitur tambahan) | Pencarian judul dengan `debounce` + `flatMapLatest`, sehingga request lama otomatis dibatalkan. Bila server sedang bermasalah, hasil diambil dari cache lokal. |
| + | **Setting** (fitur tambahan) | Pilihan tema (Ikuti sistem / Terang / Gelap) yang disimpan di DataStore dan diterapkan secara *reactive*. |
| + | **Share** (fitur tambahan) | Membagikan judul, skor, dan tautan anime lewat `Intent.ACTION_SEND` dari menu toolbar halaman detail. |

---

## 🧱 Struktur Modul

```
AniVerse/
├── app/        → com.android.application   (presentation: Home, Detail, Setting, MainActivity)
├── core/       → com.android.library       (data + domain: Room, Retrofit, repository, use case)
└── favorite/   → com.android.dynamic-feature (presentation: Favorite)
```

Arah dependensi:

```
favorite ──▶ app ──▶ core
                      ├── domain  (model, repository interface, use case)  ← tidak bergantung ke siapa pun
                      └── data    (Room, Retrofit, mapper) ──▶ domain
```

`core/build.gradle.kts` sengaja memakai `implementation` (bukan `api`) untuk Room, Retrofit,
dan OkHttp. Artinya modul `app` dan `favorite` **secara teknis tidak bisa meng-import kelas
data layer** — *dependency rule* Clean Architecture ditegakkan oleh sistem build, bukan sekadar
konvensi.

---

## 🧩 Clean Architecture — 3 Model per Layer

| Layer | Model | Lokasi |
|---|---|---|
| **Data** | `AnimeDataResponse`/`AnimeAttributesResponse` (Gson, JSON:API), `AnimeEntity` & `FavoriteAnimeEntity` (Room) | `core/data/source/...` |
| **Domain** | `Anime`, `ThemeMode` | `core/domain/model/` |
| **Presentation** | `AnimeUi`, `AnimeDetailUi` | `app/presentation/model/` |

Mapper: `core/data/mapper/AnimeMapper.kt` (data ↔ domain) dan
`app/presentation/mapper/AnimeUiMapper.kt` (domain → presentation).

Model presentation sudah berisi *string* siap tampil (`scoreLabel`, `metaLabel`, `rankLabel`),
sehingga Adapter dan Fragment tidak pernah melakukan format atau *null-check* sendiri.

---

## 💉 Dependency Injection (Koin)

Semua dependensi dirakit di `AniVerseApplication` — **tidak ada manual injection**.

| Modul Koin | Scope | Alasan |
|---|---|---|
| `databaseModule` | `single` | Instance Room + DAO mahal dan harus tunggal se-proses. |
| `networkModule` | `single` | Satu OkHttp connection pool untuk seluruh aplikasi. |
| `repositoryModule` | `single` | Repository adalah *single source of truth*, hanya boleh ada satu. |
| `useCaseModule` | `factory` | Interactor *stateless* dan murah, tiap pemakai dapat instance sendiri. |
| `presentationModule` | `single` + `viewModel` | Mapper dipakai bersama; ViewModel hidup selama ownernya. |
| `favoriteModule` | `viewModel` | Dimuat runtime lewat `loadKoinModules()` saat modul dynamic feature dibuka. |

`DetailViewModel` menerima `animeId` lewat **injected parameter** Koin
(`viewModel { (animeId: Int) -> DetailViewModel(get(), get(), animeId) }`).

---

## ⚡ Reactive Programming (Kotlin Flow)

* **Network** → `RemoteDataSource` mengembalikan `Flow<ApiResponse<T>>` di `Dispatchers.IO`.
* **Database** → semua query Room mengembalikan `Flow`, jadi UI langsung ikut berubah saat data berubah.
* **Single source of truth** → `networkBoundResource()` menggabungkan keduanya: database selalu
  menyuapi UI, network hanya mengisi ulang database.
* **Ketahanan** → `RetryInterceptor` mengulang 3× untuk 500/502/503/504 (429 sengaja tidak diulang),
  dan pencarian jatuh ke query `LIKE` pada cache Room saat jaringan atau server gagal.
* **UI** → seluruh layar berupa `StateFlow`:
  * `HomeViewModel` menggabungkan input pencarian dan *pull-to-refresh* dalam satu pipeline
    (`debounce` + `flatMapLatest`), sehingga hasil request usang tidak pernah menang balapan.
  * `DetailViewModel` men-`combine` stream detail dan stream status favorit menjadi satu state.
  * `SettingViewModel` / `MainViewModel` mengalirkan pilihan tema dari DataStore ke `AppCompatDelegate`.

---

## 🧭 Navigasi Antar Modul

Memakai **Jetpack Navigation Component** dengan `DynamicNavHostFragment`.
Destinasi `favoriteFragment` di `nav_graph.xml` ditandai `app:moduleName="favorite"`, sehingga
Navigation Component yang mengurus modul dynamic feature-nya. Perpindahan ke halaman detail dari
Home maupun dari modul Favorite memakai satu *global action* yang sama
(`action_global_detail`).

---

## 🛠️ Tech Stack

| Kategori | Library |
|---|---|
| Bahasa | Kotlin 2.3.20 (built-in Kotlin support milik AGP 9) |
| UI | Android XML Views, Material 3, ViewBinding, ConstraintLayout |
| Arsitektur | Clean Architecture, MVVM, Repository, Use Case |
| Async | Kotlin Coroutines + Flow |
| DI | Koin 4.2.2 |
| Network | Retrofit 3 + OkHttp 5 + Gson (+ `RetryInterceptor` untuk 5xx) |
| Database | Room 2.8.4 (KSP), schema version 2 |
| Preferences | DataStore Preferences |
| Navigasi | Navigation Component + Dynamic Features Fragment |
| Gambar | Glide 5 |
| Keamanan | SQLCipher 4.18 (enkripsi database), Android Keystore (AES-256/GCM), OkHttp `CertificatePinner`, R8/ProGuard |
| Performa | LeakCanary 2.14 (khusus build debug) |
| Kualitas | ktlint 1.8, Android Lint, JaCoCo 0.8.14, JUnit 4 + MockK + Turbine, GitHub Actions |
| Build | AGP 9.3.2, Gradle 9.7.1, compileSdk 37, minSdk 24, targetSdk 36 |

---

## ▶️ Cara Menjalankan

```bash
# 1. Buka folder proyek ini di Android Studio 2026.1 (Otter) atau lebih baru, atau:
./gradlew assembleDebug

# 2. Jalankan unit test
./gradlew test

# 3. Pasang ke perangkat/emulator
./gradlew installDebug
```

Tidak ada `local.properties`, API key, atau konfigurasi tambahan yang perlu diisi.
Aplikasi hanya butuh koneksi internet saat pertama kali memuat daftar anime; setelah itu
daftar dan halaman favorit tetap bisa dibuka secara offline.

> Modul `favorite` memakai `dist:install-time`, jadi modul ikut terpasang bersama APK dasar
> ketika di-*run* dari Android Studio — tidak perlu Play Store.

---

## 🔐 Keamanan

Tiga teknik wajib, plus empat lapis tambahan. Semua lokasi kelas ditulis lengkap supaya reviewer
tidak perlu mencari.

| # | Teknik | Lokasi |
|---|---|---|
| 1 | **Obfuscation (ProGuard/R8)** | [`app/build.gradle.kts`](app/build.gradle.kts) → `buildTypes.release { isMinifyEnabled = true; isShrinkResources = true }`, aturan di [`app/proguard-rules.pro`](app/proguard-rules.pro) dan [`core/consumer-rules.pro`](core/consumer-rules.pro) |
| 2 | **Enkripsi database** | [`core/.../data/source/local/room/EncryptedDatabaseFactory.kt`](core/src/main/java/com/nafiulirsad/capstone/core/data/source/local/room/EncryptedDatabaseFactory.kt) + [`core/.../data/source/local/security/DatabasePassphraseProvider.kt`](core/src/main/java/com/nafiulirsad/capstone/core/data/source/local/security/DatabasePassphraseProvider.kt) |
| 3 | **Certificate pinning** | [`core/.../data/source/remote/network/CertificatePinnerFactory.kt`](core/src/main/java/com/nafiulirsad/capstone/core/data/source/remote/network/CertificatePinnerFactory.kt), dipasang di [`core/.../di/CoreModule.kt`](core/src/main/java/com/nafiulirsad/capstone/core/di/CoreModule.kt) |
| + | **Network security config** | [`app/src/main/res/xml/network_security_config.xml`](app/src/main/res/xml/network_security_config.xml) — HTTP polos ditolak, CA buatan pengguna tidak dipercaya |
| + | **Backup dimatikan** | [`AndroidManifest.xml`](app/src/main/AndroidManifest.xml) `allowBackup="false"` + [`backup_rules.xml`](app/src/main/res/xml/backup_rules.xml) / [`data_extraction_rules.xml`](app/src/main/res/xml/data_extraction_rules.xml) |
| + | **Log dibuang di release** | `-assumenosideeffects class android.util.Log` di [`app/proguard-rules.pro`](app/proguard-rules.pro) + `HttpLoggingInterceptor.Level.NONE` saat `!BuildConfig.DEBUG` |
| + | **TLS dibatasi** | `ConnectionSpec.RESTRICTED_TLS` di `networkModule` — TLS 1.2/1.3 dengan cipher modern saja |

### 1. Obfuscation

`assembleRelease` menjalankan R8 dengan `proguard-android-optimize.txt` + aturan proyek.
Selain me-*rename*, konfigurasinya memakai `-repackageclasses` dan `-allowaccessmodification`
sehingga seluruh kelas dipindah ke satu paket datar. Bukti dari `mapping.txt` hasil build:

```
com.nafiulirsad.capstone.core.data.AnimeRepository        -> com.nafiulirsad.capstone.s5:
com.nafiulirsad.capstone.core.domain.usecase.AnimeInteractor -> com.nafiulirsad.capstone.d6:
```

Kelas turunan `Fragment` sengaja dipertahankan namanya, karena Navigation Component memuatnya
dari `nav_graph.xml` berdasarkan nama kelas.

### 2. Enkripsi database

Room dibuka lewat `SupportOpenHelperFactory` milik **SQLCipher**, jadi seluruh halaman
`aniverse_encrypted.db` ditulis dalam bentuk terenkripsi AES-256.

*Passphrase*-nya 32 byte acak yang dibuat sekali di perangkat, lalu **disegel dengan kunci
AES-256/GCM di dalam Android Keystore** dan disimpan sebagai ciphertext di SharedPreferences.
Artinya kunci sesungguhnya tidak pernah ada di APK, tidak muncul di hasil *decompile*, dan tidak
ikut terbawa saat file preferensi disalin ke perangkat lain. Bila entri Keystore rusak
(mis. hasil restore backup), aplikasi membuat kunci baru dan database baru — tidak *crash*.

Verifikasi manual:

```bash
adb shell run-as com.nafiulirsad.capstone cat databases/aniverse_encrypted.db > db.bin
file db.bin      # "data" — bukan "SQLite 3.x database"
head -c 16 db.bin | xxd   # tidak ada header "SQLite format 3"
```

### 3. Certificate pinning

`CertificatePinnerFactory` menyematkan **dua** SHA-256 SPKI untuk host `kitsu.io`: sertifikat
*intermediate* Google Trust Services `WE1` dan root `GTS Root R4`. Sertifikat *leaf* sengaja tidak
disematkan karena diputar setiap ~90 hari — menyematkannya akan mematikan aplikasi begitu
sertifikat diperbarui. Dengan pin ini, proxy penyadap (Charles/mitmproxy) langsung gagal
*handshake*. Konfigurasinya dijaga unit test
[`CertificatePinnerFactoryTest`](core/src/test/java/com/nafiulirsad/capstone/core/data/source/remote/network/CertificatePinnerFactoryTest.kt).

---

## ⚡ Performa

* **LeakCanary 2.14** dipasang sebagai `debugImplementation` di [`app/build.gradle.kts`](app/build.gradle.kts),
  sehingga hanya ada di build debug dan tidak pernah ikut ke APK rilis.
* Pola anti-kebocoran yang dipakai di seluruh layar:
  * `_binding` di-`null`-kan pada `onDestroyView()`, dan `RecyclerView.adapter = null` sebelum itu;
  * pengumpulan Flow memakai `viewLifecycleOwner.repeatOnLifecycle(STARTED)`, bukan `lifecycleScope` milik Fragment;
  * `StateFlow` layar memakai `SharingStarted.WhileSubscribed`, jadi *upstream* berhenti saat layar tidak terlihat;
  * modul Koin milik dynamic feature di-`unloadKoinModules()` pada `onDestroy()`.
* **Android Lint** bersih untuk kategori performa (Overdraw, UseCompoundDrawables,
  DisableBaselineAlignment, UnusedResources semuanya sudah dibereskan). Satu-satunya peringatan
  yang tersisa adalah `OldTargetApi` (informasi bahwa `targetSdk` 36, sementara compileSdk 37) —
  disengaja, karena `targetSdk` 36 yang sudah diuji di perangkat fisik.

```bash
./gradlew lint          # laporan: app|core/build/reports/lint-results-debug.html
```

---

## ✅ Unit Test & Coverage

`./gradlew testDebugUnitTest` menjalankan **43 test**, semuanya lulus.

| Modul | Berkas | Yang diuji |
|---|---|---|
| `core` | `AnimeMapperTest` | JSON:API → domain: rescale skor 100→10, genre dari `included`, URL trailer, entri rusak dibuang |
| `core` | `NetworkBoundResourceTest` | Alur cache-hit, refresh sukses, refresh gagal |
| `core` | `AnimeRepositoryTest` | Fallback pencarian ke cache, simpan/hapus favorit |
| `core` | `AnimeInteractorTest` | Delegasi use case ke repository |
| `core` | `RetryInterceptorTest` | Retry 5xx dengan MockWebServer, 429 tidak diulang |
| `core` | `CertificatePinnerFactoryTest` | Konfigurasi pinning (2 pin, hanya host Kitsu) |
| `core` | `StringListConverterTest`, `ThemeModeTest` | TypeConverter Room, fallback tema |
| `app` | `HomeViewModelTest` | Debounce pencarian, pull-to-refresh, error non-blocking |
| `app` | `DetailViewModelTest` | Gabungan stream detail + favorit, toggle favorit |
| `app` | `SettingViewModelTest` | Baca/tulis preferensi tema |
| `app` | `AnimeUiMapperTest` | Format skor, plural episode/durasi, terjemahan status |

Coverage memakai **JaCoCo**:

```bash
./gradlew jacocoTestReport   # laporan: app|core/build/reports/jacoco/jacocoTestReport/html/index.html
```

Hasil terakhir: `core` **52% baris**, `app` **32% baris** (kelas Fragment/Activity memang tidak
diuji lewat unit test JVM).

---

## 🤖 Continuous Integration

GitHub Actions: [`.github/workflows/android-ci.yml`](.github/workflows/android-ci.yml) →
<https://github.com/nafiulirsad/aniverse-capstone/actions>

| Job | Isi |
|---|---|
| `quality` | `./gradlew ktlintCheck` (code style) + `./gradlew lint` (Android Lint), laporan diunggah sebagai artifact |
| `test` | `./gradlew jacocoTestReport` — unit test + coverage, ringkasan coverage ditulis ke Job Summary |
| `security` | **CodeQL** (`security-extended`, java-kotlin) + **Trivy** (`vuln,secret,misconfig`) — analisis keamanan kode dan dependensi |
| `build` | `./gradlew assembleRelease` (build yang menjalankan R8) dan `assembleDebug`; APK + `mapping.txt` diunggah sebagai artifact |

Job `build` menunggu `quality` dan `test` lulus lebih dulu, jadi APK hanya dibuat dari kode yang
sudah lolos analisis dan pengujian.

---

## 🧹 Perintah Verifikasi Lokal

```bash
./gradlew ktlintCheck        # code style
./gradlew lint               # Android Lint (Inspect Code)
./gradlew jacocoTestReport   # unit test + coverage
./gradlew assembleRelease    # APK ter-obfuscate + mapping.txt
./gradlew installDebug       # pasang build debug (berisi LeakCanary)
```

---

## 👤 Penulis

**Nafiul Irsad** — Dicoding ID: `nafiulirsad`
