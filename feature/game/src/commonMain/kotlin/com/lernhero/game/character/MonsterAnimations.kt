package com.lernhero.game.character

/**
 * Katalog gotowych animacji potworow - to, co wychodzi z Dev Animation Center i czego uzywa walka.
 *
 * ==========================================================================================
 *  TU SIE WKLEJA WYNIK Z LABU.
 *  W labie: ZAPISZ -> kod ladzie w schowku -> wklej gotowa `MonsterAnimationClip` ponizej
 *  i podepnij ja w [clipFor]. Nic wiecej nie trzeba - walka sama zacznie jej uzywac.
 *
 *  Pivoty z naglowka wygenerowanego kodu naleza do RIGU, nie do klipu - przenosi sie je recznie
 *  do [MonsterRigs], bo obowiazuja wszystkie animacje danego gatunku naraz.
 * ==========================================================================================
 *
 * Zwrocenie null z [clipFor] jest calkowicie normalne i znaczy "ten potwor nie ma tej animacji" -
 * renderer po prostu pokazuje go nieruchomo, bez zadnego bledu.
 */
object MonsterAnimations {

    /**
     * Smok rozwiera paszcze i laduje w niej wirujacy pocisk. Zrobione w Dev Animation Center.
     *
     * Rozwarcie idzie GORNA czaszka (+39°), a dolna szczeka odchyla sie tylko lekko w druga
     * strone (-12°) - obie wokol tego samego stawu (patrz pivoty w [MonsterRigs]), wiec paszcza
     * otwiera sie jak u prawdziwego gada, a nie jak klapa.
     *
     * Pocisk jest przypiety do `body`, ktore nie ma wlasnej sciezki - dzieki temu wisi nieruchomo
     * przed pyskiem i tylko rosnie, zamiast jezdzic razem z obracajaca sie szczeka.
     *
     * [MonsterAnimationClip.durationMs] = 8000 to tempo, w ktorym klip byl autorowany. Zdarzenia
     * bossa i tak rozciagaja go na caly czas na odpowiedz (patrz [CharacterUiModel.animDurationMs]),
     * wiec ta liczba ma znaczenie tylko tam, gdzie klip zagra we wlasnym tempie.
     */
    private val bossSkeletonDragonAttack = MonsterAnimationClip(
        id = "boss_skeleton_dragon_attack",
        speciesKey = "boss_skeleton_dragon",
        durationMs = 8000,
        tracks = listOf(
            PartTrack(
                "skull_down", listOf(
                    Keyframe(at = 0.0f, rotationDeg = 0.0f, offsetX = 0.0f, offsetY = 0.0f, scale = 1.0f, alpha = 1.0f, easing = ClipEasing.Smooth),
                    Keyframe(at = 0.99f, rotationDeg = -12.14f, offsetX = 0.0f, offsetY = 0.0f, scale = 1.0f, alpha = 1.0f, easing = ClipEasing.Smooth)
                )
            ),
            PartTrack(
                "skull_up", listOf(
                    Keyframe(at = 0.0f, rotationDeg = 0.0f, offsetX = 0.0f, offsetY = 0.0f, scale = 1.0f, alpha = 1.0f, easing = ClipEasing.Smooth),
                    Keyframe(at = 0.99f, rotationDeg = 39.29f, offsetX = 0.0f, offsetY = 0.0f, scale = 1.0f, alpha = 1.0f, easing = ClipEasing.Smooth)
                )
            )
        ),
        effects = listOf(
            ClipEffect(
                effectKey = "magic_missile_rings",
                from = 0.0f,
                to = 1.0f,
                anchorPartId = "body",
                anchorX = 0.19f,
                anchorY = 0.35f,
                sizeFraction = 0.35f,
                keyframes = listOf(
                    Keyframe(at = 0.0f, scale = 0.1f, alpha = 1.0f, rotationDeg = 0.0f, easing = ClipEasing.Linear),
                    Keyframe(at = 1.0f, scale = 1.0f, alpha = 1.0f, rotationDeg = 0.0f, easing = ClipEasing.Smooth)
                ),
                layerSpins = mapOf("ring1" to 0.18f, "ring2" to -0.14f, "ring3" to 0.32f)
            )
        )
    )

    /**
     * Animacja danego gatunku w danym gniezdzie, albo null gdy jej nie ma.
     *
     * Nowa animacja = jeden `val` wyzej + jedna linijka w tym `when`.
     */
    fun clipFor(speciesKey: String, slot: AnimSlot): MonsterAnimationClip? = when {
        // Zdarzenia specjalne bossa prosza o [AnimSlot.Cast]. W labie klip siedzi w gniezdzie ATAK,
        // ale nazwa gniazda w labie to tylko etykieta - o tym, co gra kiedy, decyduje TO mapowanie.
        //
        // Swiadomie NIE podpinamy tego pod [AnimSlot.Attack]: zwykly cios czeka na koniec animacji
        // ataku, zanim ruszy dalej salwa, a ten klip trwa 8 sekund - kazdy cios blokowalby ture.
        speciesKey == "boss_skeleton_dragon" && slot == AnimSlot.Cast -> bossSkeletonDragonAttack

        else -> null
    }
}
