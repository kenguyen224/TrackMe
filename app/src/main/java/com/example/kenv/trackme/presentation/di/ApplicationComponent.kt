package com.example.kenv.trackme.presentation.di

import android.app.Application
import android.content.Context
import com.example.kenv.AndroidApplication
import com.example.kenv.trackme.data.local.AppDatabase
import com.example.kenv.trackme.domain.coroutine.CoroutineDispatcherProvider
import com.example.kenv.trackme.presentation.di.module.ApplicationModule
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

/**
 * Created by Kenv on 12/12/2020.
 */
@Singleton
@Component(
    modules = [ApplicationModule::class]
)
interface ApplicationComponent {

    val context: Context
    val appDatabase: AppDatabase
    val dispatcherProvider: CoroutineDispatcherProvider

    fun inject(androidApplication: AndroidApplication)

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder
        fun build(): ApplicationComponent
    }

    companion object {
        fun create(application: Application): ApplicationComponent {
            return DaggerApplicationComponent.builder()
                .application(application)
                .build()
        }
    }
}
