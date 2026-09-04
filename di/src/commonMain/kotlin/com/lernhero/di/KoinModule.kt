package com.lernhero.di

import com.lernhero.auth.AuthViewModel
import com.lernhero.data.PlayerRepositoryImpl
import com.lernhero.data.quiz.RemoteQuizGenerator
import com.lernhero.domain.repository.PlayerRepository
import com.lernhero.domain.quiz.ai.QuizGenerator
import com.lernhero.domain.run.RunSession
import com.lernhero.shared.i18n.AppLanguageStore
import com.lernhero.domain.story.CampaignSelection

import com.lernhero.game.battle.BattleViewModel
import com.lernhero.home.city.TownViewModel
import com.lernhero.home.fitness.FitnessViewModel
import com.lernhero.home.player.PlayerViewModel
import com.lernhero.home.quiz.CustomQuizViewModel
import com.lernhero.reader.LibraryViewModel
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val sharedModule = module {
    single<PlayerRepository> { PlayerRepositoryImpl() }
    single { RunSession() }
    // Gdzie gracz stoi na mapie kampanii - dzielone miedzy zakladka "Walcz" a ekranem Mapy.
    single { CampaignSelection() }
    // Device-local UI language (not account data) - patrz shared/i18n/AppLanguage.kt.
    single { AppLanguageStore() }
    // Generator pytan do wlasnych talii. RemoteQuizGenerator sam wraca do zachowania Stub
    // (isAvailable == false), dopoki ServerConfig.AI_QUIZ_SERVER_BASE_URL jest puste - czyli
    // dzis, przed pierwszym wdrozeniem :server na Cloud Run, UI dziala dokladnie tak jak wczesniej.
    single<QuizGenerator> { RemoteQuizGenerator() }
    viewModelOf(::AuthViewModel)
    viewModelOf(::BattleViewModel)
    viewModelOf(::PlayerViewModel)
    viewModelOf(::CustomQuizViewModel)
    viewModelOf(::TownViewModel)
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
