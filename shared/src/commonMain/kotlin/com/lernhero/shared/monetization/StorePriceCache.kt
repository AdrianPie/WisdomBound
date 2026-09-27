package com.lernhero.shared.monetization

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set

/**
 * Ostatnie ceny, ktore podal sklep - w walucie gracza, sformatowane przez Google Play / App Store.
 *
 * Sklep odpowiada z opoznieniem (i wcale, gdy nie ma sieci), a wczesniej pokazywalismy wtedy ceny
 * zapasowe wpisane na sztywno w zlotowkach - gracz z Niemiec widzial "49,99 zl". Teraz ceny sa
 * pobierane przy starcie aplikacji i zapamietywane na urzadzeniu, wiec od drugiego uruchomienia
 * sklep w grze od razu pokazuje wlasciwa walute. Przy pierwszym uruchomieniu, zanim sklep
 * odpowie, zamiast ceny jest [PENDING] - nigdy obca waluta.
 */
object StorePriceCache {
    const val PENDING = "…"

    private val settings: Settings by lazy { Settings() }

    fun get(key: String): String? = settings.getStringOrNull(KEY_PREFIX + key)

    fun put(key: String, value: String?) {
        if (value.isNullOrBlank()) return
        settings[KEY_PREFIX + key] = value
    }

    /** Oferty subskrypcji: cena miesieczna, roczna, roczna w przeliczeniu na miesiac i znizka. */
    fun putOffers(offers: List<SubscriptionOffer>) {
        val month = offers.firstOrNull { it.period == "month" }
        val year = offers.firstOrNull { it.period == "year" }
        put(SUB_MONTH, month?.price)
        put(SUB_YEAR, year?.price)
        put(SUB_YEAR_PER_MONTH, year?.pricePerMonth)
        put(SUB_DISCOUNT, yearlyDiscount(month, year))
    }

    /** "-44%" policzone z cen sklepu, wiec zgadza sie w kazdym kraju - albo null, gdy brak danych. */
    fun yearlyDiscount(month: SubscriptionOffer?, year: SubscriptionOffer?): String? {
        val m = month?.priceMicros ?: return null
        val y = year?.priceMicros ?: return null
        if (m <= 0 || y <= 0) return null
        val percent = ((1.0 - y.toDouble() / (m * 12.0)) * 100).toInt()
        return if (percent > 0) "-$percent%" else null
    }

    const val SUB_MONTH = "sub_month"
    const val SUB_YEAR = "sub_year"
    const val SUB_YEAR_PER_MONTH = "sub_year_per_month"
    const val SUB_DISCOUNT = "sub_discount"
    private const val KEY_PREFIX = "store_price_"
}
