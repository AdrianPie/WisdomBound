package com.lernhero.shared

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

data class FlagCountry(
    val code: String,
    val countryName: String,
    val flag: DrawableResource
)

object FlagQuizResources {
    val countries = listOf(
        FlagCountry(code = "pl", countryName = "Polska", flag = Res.drawable.pl),
        FlagCountry(code = "de", countryName = "Niemcy", flag = Res.drawable.de),
        FlagCountry(code = "fr", countryName = "Francja", flag = Res.drawable.fr),
        FlagCountry(code = "es", countryName = "Hiszpania", flag = Res.drawable.es),
        FlagCountry(code = "it", countryName = "Wlochy", flag = Res.drawable.it),
        FlagCountry(code = "pt", countryName = "Portugalia", flag = Res.drawable.pt),
        FlagCountry(code = "gb", countryName = "Wielka Brytania", flag = Res.drawable.gb),
        FlagCountry(code = "ie", countryName = "Irlandia", flag = Res.drawable.ie),
        FlagCountry(code = "us", countryName = "Stany Zjednoczone", flag = Res.drawable.us),
        FlagCountry(code = "ca", countryName = "Kanada", flag = Res.drawable.ca),
        FlagCountry(code = "mx", countryName = "Meksyk", flag = Res.drawable.mx),
        FlagCountry(code = "br", countryName = "Brazylia", flag = Res.drawable.br),
        FlagCountry(code = "ar", countryName = "Argentyna", flag = Res.drawable.ar),
        FlagCountry(code = "cn", countryName = "Chiny", flag = Res.drawable.cn),
        FlagCountry(code = "jp", countryName = "Japonia", flag = Res.drawable.jp),
        FlagCountry(code = "kr", countryName = "Korea Poludniowa", flag = Res.drawable.kr),
        FlagCountry(code = "au", countryName = "Australia", flag = Res.drawable.au),
        FlagCountry(code = "nz", countryName = "Nowa Zelandia", flag = Res.drawable.nz),
        FlagCountry(code = "no", countryName = "Norwegia", flag = Res.drawable.no),
        FlagCountry(code = "se", countryName = "Szwecja", flag = Res.drawable.se),
        FlagCountry(code = "fi", countryName = "Finlandia", flag = Res.drawable.fi),
        FlagCountry(code = "dk", countryName = "Dania", flag = Res.drawable.dk),
        FlagCountry(code = "nl", countryName = "Holandia", flag = Res.drawable.nl),
        FlagCountry(code = "be", countryName = "Belgia", flag = Res.drawable.be),
        FlagCountry(code = "ch", countryName = "Szwajcaria", flag = Res.drawable.ch),
        FlagCountry(code = "at", countryName = "Austria", flag = Res.drawable.at),
        FlagCountry(code = "cz", countryName = "Czechy", flag = Res.drawable.cz),
        FlagCountry(code = "sk", countryName = "Slowacja", flag = Res.drawable.sk),
        FlagCountry(code = "hu", countryName = "Wegry", flag = Res.drawable.hu),
        FlagCountry(code = "ro", countryName = "Rumunia", flag = Res.drawable.ro),
        FlagCountry(code = "bg", countryName = "Bulgaria", flag = Res.drawable.bg),
        FlagCountry(code = "gr", countryName = "Grecja", flag = Res.drawable.gr),
        FlagCountry(code = "tr", countryName = "Turcja", flag = Res.drawable.tr),
        FlagCountry(code = "ua", countryName = "Ukraina", flag = Res.drawable.ua),
        FlagCountry(code = "eg", countryName = "Egipt", flag = Res.drawable.eg),
        FlagCountry(code = "ma", countryName = "Maroko", flag = Res.drawable.ma),
        FlagCountry(code = "ng", countryName = "Nigeria", flag = Res.drawable.ng),
        FlagCountry(code = "ke", countryName = "Kenia", flag = Res.drawable.ke),
        FlagCountry(code = "za", countryName = "Republika Poludniowej Afryki", flag = Res.drawable.za),
        FlagCountry(code = "th", countryName = "Tajlandia", flag = Res.drawable.th),
        FlagCountry(code = "vn", countryName = "Wietnam", flag = Res.drawable.vn),
        FlagCountry(code = "ph", countryName = "Filipiny", flag = Res.drawable.ph),
        FlagCountry(code = "sg", countryName = "Singapur", flag = Res.drawable.sg)
    )
}
