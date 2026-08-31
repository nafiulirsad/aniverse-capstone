# AniVerse — Capstone Project (Dicoding Menjadi Android Developer Expert)

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
| Build | AGP 9.3.2, Gradle 9.7, compileSdk 37, minSdk 24, targetSdk 36 |

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

## ✅ Unit Test

11 test, semuanya lulus lewat `./gradlew test`.

```
core/src/test/java/com/nafiulirsad/capstone/core/
├── data/AnimeMapperTest.kt          → JSON:API → domain: rescale skor 100→10, genre dari
│                                      array `included`, URL trailer YouTube, entri rusak dibuang
├── data/NetworkBoundResourceTest.kt → alur cache-hit, refresh sukses, dan refresh gagal
└── domain/model/ThemeModeTest.kt    → fallback tema saat nilai tersimpan tidak dikenal
```

---

## 👤 Penulis

**Nafiul Irsad** — Dicoding ID: `nafiulirsad`
