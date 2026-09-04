package com.lernhero.shared.resources

import lernhero.shared.generated.resources.Res
import lernhero.shared.generated.resources.ar
import lernhero.shared.generated.resources.at
import lernhero.shared.generated.resources.au
import lernhero.shared.generated.resources.be
import lernhero.shared.generated.resources.bg
import lernhero.shared.generated.resources.br
import lernhero.shared.generated.resources.ca
import lernhero.shared.generated.resources.ch
import lernhero.shared.generated.resources.cn
import lernhero.shared.generated.resources.cz
import lernhero.shared.generated.resources.de
import lernhero.shared.generated.resources.dk
import lernhero.shared.generated.resources.eg
import lernhero.shared.generated.resources.es
import lernhero.shared.generated.resources.fi
import lernhero.shared.generated.resources.fr
import lernhero.shared.generated.resources.gb
import lernhero.shared.generated.resources.gr
import lernhero.shared.generated.resources.hu
import lernhero.shared.generated.resources.ie
import lernhero.shared.generated.resources.it
import lernhero.shared.generated.resources.jp
import lernhero.shared.generated.resources.ke
import lernhero.shared.generated.resources.kr
import lernhero.shared.generated.resources.ma
import lernhero.shared.generated.resources.mx
import lernhero.shared.generated.resources.ng
import lernhero.shared.generated.resources.nl
import lernhero.shared.generated.resources.no
import lernhero.shared.generated.resources.nz
import lernhero.shared.generated.resources.ph
import lernhero.shared.generated.resources.pl
import lernhero.shared.generated.resources.pt
import lernhero.shared.generated.resources.ro
import lernhero.shared.generated.resources.se
import lernhero.shared.generated.resources.sg
import lernhero.shared.generated.resources.sk
import lernhero.shared.generated.resources.th
import lernhero.shared.generated.resources.tr
import lernhero.shared.generated.resources.ua
import lernhero.shared.generated.resources.us
import lernhero.shared.generated.resources.vn
import lernhero.shared.generated.resources.za
import org.jetbrains.compose.resources.DrawableResource

/**
 * Flagi krajow. Pytania quizowe zyja w domenie i posluguja sie kodem kraju ("pl"), bo domena
 * nie moze zalezec od Compose. Tutaj kod zamienia sie w obrazek - i tylko tutaj.
 */
object FlagResources {

    private val flags: Map<String, DrawableResource> = mapOf(
        "pl" to Res.drawable.pl,
        "de" to Res.drawable.de,
        "fr" to Res.drawable.fr,
        "es" to Res.drawable.es,
        "it" to Res.drawable.it,
        "pt" to Res.drawable.pt,
        "gb" to Res.drawable.gb,
        "ie" to Res.drawable.ie,
        "us" to Res.drawable.us,
        "ca" to Res.drawable.ca,
        "mx" to Res.drawable.mx,
        "br" to Res.drawable.br,
        "ar" to Res.drawable.ar,
        "cn" to Res.drawable.cn,
        "jp" to Res.drawable.jp,
        "kr" to Res.drawable.kr,
        "au" to Res.drawable.au,
        "nz" to Res.drawable.nz,
        "no" to Res.drawable.no,
        "se" to Res.drawable.se,
        "fi" to Res.drawable.fi,
        "dk" to Res.drawable.dk,
        "nl" to Res.drawable.nl,
        "be" to Res.drawable.be,
        "ch" to Res.drawable.ch,
        "at" to Res.drawable.at,
        "cz" to Res.drawable.cz,
        "sk" to Res.drawable.sk,
        "hu" to Res.drawable.hu,
        "ro" to Res.drawable.ro,
        "bg" to Res.drawable.bg,
        "gr" to Res.drawable.gr,
        "tr" to Res.drawable.tr,
        "ua" to Res.drawable.ua,
        "eg" to Res.drawable.eg,
        "ma" to Res.drawable.ma,
        "ng" to Res.drawable.ng,
        "ke" to Res.drawable.ke,
        "za" to Res.drawable.za,
        "th" to Res.drawable.th,
        "vn" to Res.drawable.vn,
        "ph" to Res.drawable.ph,
        "sg" to Res.drawable.sg
    )

    fun flagFor(code: String): DrawableResource? = flags[code]
}
