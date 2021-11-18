package net.longday.planner.di

import android.app.Application

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
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

//    private val MIGRATION_1_2 = object : Migration(1, 2) {
//        override fun migrate(database: SupportSQLiteDatabase) {
//            database.execSQL("ALTER TABLE tasks ADD COLUMN isFocused INTEGER NOT NULL default 0")
//        }
//    }
//
//    private val MIGRATION_2_3 = object : Migration(2, 3) {
//        override fun migrate(database: SupportSQLiteDatabase) {
//            database.execSQL("ALTER TABLE tasks ADD COLUMN isCanceled INTEGER NOT NULL default 0")
//            database.execSQL("ALTER TABLE tasks ADD COLUMN cancelReason TEXT")
//            database.execSQL("ALTER TABLE tasks ADD COLUMN cancelTime INTEGER")
//        }
//    }

    @Provides
    @Singleton
    fun provideDatabase(application: Application) =
        Room.databaseBuilder(application, PlannerDatabase::class.java, "planner")
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
//            .addMigrations(MIGRATION_1_2)
//            .addMigrations(MIGRATION_2_3)
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
