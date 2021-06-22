package net.longday.planner.di

import android.app.Application
import androidx.room.Room
import androidx.room.RoomDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.longday.planner.PlannerApplication
import net.longday.planner.data.PlannerDatabase
import net.longday.planner.data.entity.Category
import java.util.*
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

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
        )
            .createFromAsset("databases/prepopulate.db")
            .setQueryCallback(RoomDatabase.QueryCallback { sqlQuery, bindArgs ->
                println("SQL Query: $sqlQuery SQL Args: $bindArgs")
            }, Executors.newSingleThreadExecutor())
            .build()

    @Provides
    @Singleton
    fun provideTaskDao(plannerDatabase: PlannerDatabase) = plannerDatabase.taskDao()

    @Provides
    @Singleton
    fun provideCategoryDao(plannerDatabase: PlannerDatabase) = plannerDatabase.categoryDao()

    private fun prepopulateDb(provideDatabase: Any?) {
        GlobalScope.launch(Dispatchers.Main) {
            provideCategoryDao(provideDatabase(PlannerApplication())).insert(
                Category(
                    id = UUID.randomUUID().toString(),
                    title = "Home",
                )
            )
        }
    }
}
