package net.longday.planner.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.longday.planner.MainActivity
import javax.inject.Inject


class SuperText @Inject constructor() {
    val greet = "super greet"
}