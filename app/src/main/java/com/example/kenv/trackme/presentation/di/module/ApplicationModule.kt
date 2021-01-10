package com.example.kenv.trackme.presentation.di.module

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.example.kenv.trackme.data.local.AppDatabase
import com.example.kenv.trackme.domain.coroutine.CoroutineDispatcherProvider
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

/**
 * Created by KeNV on 18,December,2020
 * VNG company,
 * HCM, Viet Nam
 */
@Module
class ApplicationModule {
    @Singleton
    @Provides
    fun provideApplicationContext(application: Application): Context = application

    @Singleton
    @Provides
    fun provideAppDatabase(context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "track-me-database"
        ).build()
    }

    @Singleton
    @Provides
    fun provideCoroutineDispatcherProvider(): CoroutineDispatcherProvider {
        return CoroutineDispatcherProvider(
            Dispatchers.IO,
            Dispatchers.Main,
            Dispatchers.Default
        )
    }
}
