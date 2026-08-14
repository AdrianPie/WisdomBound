package com.lernhero.di

import com.lernhero.auth.AuthViewModel
import com.lernhero.data.PlayerRepositoryImpl
import com.lernhero.domain.repository.PlayerRepository
import com.lernhero.domain.run.RunSession

import com.lernhero.game.battle.BattleViewModel
import com.lernhero.home.fitness.FitnessViewModel
import com.lernhero.home.player.PlayerViewModel
import com.lernhero.home.reading.LibraryViewModel
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val sharedModule = module {
    single<PlayerRepository> { PlayerRepositoryImpl() }
    single { RunSession() }
    viewModelOf(::AuthViewModel)
    viewModelOf(::BattleViewModel)
    viewModelOf(::PlayerViewModel)
    viewModelOf(::FitnessViewModel)
    viewModelOf(::LibraryViewModel)
}
fun initializeKoin(
    config: (KoinApplication.() -> Unit)? = null
) {
    startKoin {
        config?.invoke(this)

        modules(sharedModule)
    }
}
