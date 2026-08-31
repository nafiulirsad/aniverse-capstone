package com.nafiulirsad.capstone.core.di

import com.nafiulirsad.capstone.core.BuildConfig
import com.nafiulirsad.capstone.core.data.AnimeRepository
import com.nafiulirsad.capstone.core.data.SettingRepository
import com.nafiulirsad.capstone.core.data.source.local.LocalDataSource
import com.nafiulirsad.capstone.core.data.source.local.preference.SettingPreference
import com.nafiulirsad.capstone.core.data.source.local.room.AnimeDatabase
import com.nafiulirsad.capstone.core.data.source.local.room.EncryptedDatabaseFactory
import com.nafiulirsad.capstone.core.data.source.local.security.DatabasePassphraseProvider
import com.nafiulirsad.capstone.core.data.source.remote.RemoteDataSource
import com.nafiulirsad.capstone.core.data.source.remote.network.ApiService
import com.nafiulirsad.capstone.core.data.source.remote.network.CertificatePinnerFactory
import com.nafiulirsad.capstone.core.data.source.remote.network.NetworkErrorMapper
import com.nafiulirsad.capstone.core.data.source.remote.network.RetryInterceptor
import com.nafiulirsad.capstone.core.domain.repository.IAnimeRepository
import com.nafiulirsad.capstone.core.domain.repository.ISettingRepository
import com.nafiulirsad.capstone.core.domain.usecase.AnimeInteractor
import com.nafiulirsad.capstone.core.domain.usecase.AnimeUseCase
import com.nafiulirsad.capstone.core.domain.usecase.SettingInteractor
import com.nafiulirsad.capstone.core.domain.usecase.SettingUseCase
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

private const val TIMEOUT_SECONDS = 30L

/** `single` - the database and its DAO are expensive and must be shared process-wide. */
val databaseModule = module {
    single { DatabasePassphraseProvider(androidContext(), AnimeDatabase.DATABASE_NAME) }
    single { EncryptedDatabaseFactory.create(androidContext(), get()) }
    single { get<AnimeDatabase>().animeDao() }
    single { LocalDataSource(get()) }
    single { SettingPreference(androidContext()) }
}

/** `single` - one OkHttp connection pool for the whole app. */
val networkModule = module {
    single {
        // A release build must not print request URLs into logcat.
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }
    single { RetryInterceptor() }
    single {
        OkHttpClient.Builder()
            .addInterceptor(get<HttpLoggingInterceptor>())
            .addInterceptor(get<RetryInterceptor>())
            // Certificate pinning + TLS 1.2/1.3 only: no downgrade, no proxy in the middle.
            .certificatePinner(CertificatePinnerFactory.create())
            .connectionSpecs(listOf(ConnectionSpec.RESTRICTED_TLS))
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }
    single {
        Retrofit.Builder()
            .baseUrl(RemoteDataSource.BASE_URL)
            .client(get())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
    single { NetworkErrorMapper(androidContext()) }
    single { RemoteDataSource(get(), get()) }
}

/** `single` - the repository is the single source of truth, so exactly one instance may exist. */
val repositoryModule = module {
    single<IAnimeRepository> { AnimeRepository(get(), get()) }
    single<ISettingRepository> { SettingRepository(get()) }
}

/** `factory` - interactors are stateless and cheap, so every consumer gets its own. */
val useCaseModule = module {
    factory<AnimeUseCase> { AnimeInteractor(get()) }
    factory<SettingUseCase> { SettingInteractor(get()) }
}
