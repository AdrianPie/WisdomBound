package com.lernhero.home.ui

import com.lernhero.domain.item.ItemCatalog
import com.lernhero.domain.item.catalog.MonsterGear
import kotlin.test.Test
import kotlin.test.assertTrue

class ItemArtTest {

    /**
     * Kazdy przedmiot w grze musi miec ikone.
     *
     * Bez tego testu bylo tak: katalog urosl z 29 do 85 przedmiotow, nikt nie dopisal nowych
     * kluczy do mapy ikon i pol ekwipunku wyswietlalo sie graczowi jako "?". Wychodzi to dopiero
     * na urzadzeniu, bo kod kompiluje sie bez zarzutu - `else` w `when` przyjmuje wszystko.
     */
    @Test
    fun `kazdy przedmiot ma ikone`() {
        val missing = (ItemCatalog.all + MonsterGear.all)
            .filter { it.iconKey !in MAPPED_ICON_KEYS }
            .map { "${it.id} (iconKey='${it.iconKey}')" }

        assertTrue(missing.isEmpty(), "Przedmioty bez ikony: $missing")
    }
}
