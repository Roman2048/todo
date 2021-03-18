package net.longday.planner.di

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.longday.planner.data.PlannerDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PersistenceModule {

    @Provides
    @Singleton
    fun provideDatabase(application: Application) =
        Room.databaseBuilder(
            application,
            PlannerDatabase::class.java,
            "planner"
        ).build()

    @Provides
    @Singleton
    fun provideTaskDao(plannerDatabase: PlannerDatabase) = plannerDatabase.taskDao()

    @Provides
    @Singleton
    fun provideCategoryDao(plannerDatabase: PlannerDatabase) = plannerDatabase.categoryDao()

}
