package com.hanzyukukobo.arukikata.di

import android.content.Context
import androidx.room.Room
import com.hanzyukukobo.arukikata.data.repositories.GaitAnalysisRepository
import com.hanzyukukobo.arukikata.data.sources.FrameLocalDataSource
import com.hanzyukukobo.arukikata.data.sources.GaitAnalysisDatabase
import com.hanzyukukobo.arukikata.data.sources.LogsLocalDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Singleton
    @Provides
    fun provideGaitAnalysisRepository(
        frameLocalDataSource: FrameLocalDataSource,
        logsLocalDataSource: LogsLocalDataSource
    ): GaitAnalysisRepository {
        return GaitAnalysisRepository(frameLocalDataSource, logsLocalDataSource)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DataSourceModule {
    @Singleton
    @Provides
    fun provideFrameLocalDataSource(
        database: GaitAnalysisDatabase,
        @DefaultDispatcher dispatcher: CoroutineDispatcher
    ): FrameLocalDataSource {
        return FrameLocalDataSource(database.frameDao(), dispatcher)
    }

    @Singleton
    @Provides
    fun provideLogsLocalDataSource(
        database: GaitAnalysisDatabase,
        @DefaultDispatcher dispatcher: CoroutineDispatcher
    ): LogsLocalDataSource {
        return LogsLocalDataSource(database.logDao(), dispatcher)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Singleton
    @Provides
    fun provideDataBase(@ApplicationContext context: Context): GaitAnalysisDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            GaitAnalysisDatabase::class.java,
            "app_database"
        ).build()
    }
}