package net.longday.planner.di

import android.app.Application
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
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
import java.io.File
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
        ).addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                Thread {
                    db.execSQL("INSERT INTO categories VALUES ('402ca0fd-7a92-4a55-aa34-bf3077cfe805','Personal');")
                    db.execSQL("INSERT INTO categories VALUES ('4705dfd2-ce2e-4a5e-8c59-8ebe17c6c5f8','Wishlist');")
                }.start()
            }
        }).build()

    @Provides
    @Singleton
    fun provideTaskDao(plannerDatabase: PlannerDatabase) = plannerDatabase.taskDao()

    @Provides
    @Singleton
    fun provideCategoryDao(plannerDatabase: PlannerDatabase) = plannerDatabase.categoryDao()
}
