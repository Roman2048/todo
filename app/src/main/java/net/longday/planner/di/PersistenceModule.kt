package net.longday.planner.di

import android.app.Application

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.longday.planner.R
import net.longday.planner.data.PlannerDatabase
import java.util.*
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
        )
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    Thread {
                        db.execSQL(
                            "INSERT INTO categories VALUES ('${UUID.randomUUID()}','${
                                application.applicationContext.getString(
                                    R.string.personal_category_title
                                )
                            }', 0 , null);"
                        )
                        db.execSQL(
                            "INSERT INTO categories VALUES ('${UUID.randomUUID()}','${
                                application.applicationContext.getString(
                                    R.string.wishlist_category_title
                                )
                            }', 2, null);"
                        )
                    }.start()
                }
            })
//            .fallbackToDestructiveMigration()
//            .setQueryCallback({ sqlQuery, bindArgs ->
//                println("SQL Query: $sqlQuery SQL Args: $bindArgs")
//            }, Executors.newSingleThreadExecutor())
            .build()

    @Provides
    @Singleton
    fun provideTaskDao(plannerDatabase: PlannerDatabase) = plannerDatabase.taskDao()

    @Provides
    @Singleton
    fun provideCategoryDao(plannerDatabase: PlannerDatabase) = plannerDatabase.categoryDao()

    @Provides
    @Singleton
    fun provideTabDao(plannerDatabase: PlannerDatabase) = plannerDatabase.tabDao()

    @Provides
    @Singleton
    fun provideReminderDao(plannerDatabase: PlannerDatabase) = plannerDatabase.reminderDao()
}
