package com.lernhero.domain.reading

import kotlinx.serialization.Serializable

/**
 * Slowo albo zdanie zapisane z czytnika przyciskiem "zakladka". [sentence] jedzie razem ze
 * slowem, bo bez zdania-kontekstu powtorka nie ma jak przypomniec, w jakim znaczeniu gracz to
 * slowo widzial.
 *
 * [isSentence] rozroznia, co dokladnie zapisano - pojedyncze slowo, czy calosc zaznaczenia
 * rozszerzonego na zdanie (przycisk "rozszerz" w popupie tlumaczenia). Ma to znaczenie dla
 * [com.lernhero.domain.quiz.WordQuizBank]: quiz pyta WYLACZNIE o pojedyncze slowa - zdanie jako
 * pytanie albo jako jedna z czterech odpowiedzi nie ma sensu (ani do zgadniecia, ani jako
 * wiarygodny dystraktor).
 *
 * Liczniki nauki ([correctStreak], [correctTotal], [wrongTotal]) siedza NA fiszce, a nie w osobnej
 * mapie obok - dzieki temu slowo i jego historia nie moga sie rozjechac ani przy kasowaniu fiszki,
 * ani przy (de)serializacji konta. Patrz [VocabularyBox] po regule awansu do archiwum.
 */
@Serializable
data class SavedWord(
    val id: String,
    val text: String,
    val translation: String,
    val sentence: String,
    val bookId: String,
    val bookTitle: String,
    /** epoch millis - kiedy zapisano, do sortowania i do rozkladania powtorek w czasie. */
    val savedAt: Long,
    val isSentence: Boolean = false,
    /** Ile razy Z RZEDU odpowiedziano poprawnie. Kazda pomylka zeruje - patrz [VocabularyBox]. */
    val correctStreak: Int = 0,
    /** Wszystkie trafienia, lacznie - licznik "ile razy dobrze" na kafelku slowka. */
    val correctTotal: Int = 0,
    /** Wszystkie pomylki, lacznie - licznik "ile razy zle" na kafelku slowka. */
    val wrongTotal: Int = 0,
    /**
     * Kiedy slowo trafilo do archiwum "opanowane" (epoch millis), null = wciaz sie go uczymy.
     * Trzymamy moment, a nie samo `true`, zeby dalo sie sortowac archiwum po swiezosci i zeby
     * pozniej mozna bylo dorobic powtorki przypominajace bez zmiany modelu.
     */
    val masteredAt: Long? = null
) {
    /** W ktorym z dwoch archiwow lezy ta fiszka. */
    val box: VocabularyBox get() = if (masteredAt != null) VocabularyBox.Mastered else VocabularyBox.Learning

    /** Ile odpowiedzi w ogole padlo na to slowo - mianownik skutecznosci. */
    val answersTotal: Int get() = correctTotal + wrongTotal

    /** Skutecznosc 0f..1f. Zero odpowiedzi = 0f (a nie dzielenie przez zero). */
    val accuracy: Float get() = if (answersTotal == 0) 0f else correctTotal.toFloat() / answersTotal
}
