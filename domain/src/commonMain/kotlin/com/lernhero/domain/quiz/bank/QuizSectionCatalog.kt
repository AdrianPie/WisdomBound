// PLIK GENEROWANY z domain/quizbank/sections.tsv przez `python3 tools/quizbank.py generate`.
// Nie edytuj go recznie - dzialy dopisuje sie do sections.tsv (patrz domain/quizbank/README.md).
package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizSection

/**
 * Wszystkie dzialy tematow podzielonych na katalogi, w kolejnosci z sections.tsv - galaz zawsze
 * przed swoimi poddzialami. Wygodny dostep do drzewa daje [com.lernhero.domain.quiz.QuizSections].
 */
object QuizSectionCatalog {

    val all: List<QuizSection> = listOf(
        QuizSection("history/prehistory", "Prehistory", "🦴", QuizCategory.History),
        QuizSection("history/antiquity", "Antiquity", "🏺", QuizCategory.History),
        QuizSection("history/antiquity/near_east", "Mesopotamia & the Near East", "🧱", QuizCategory.History),
        QuizSection("history/antiquity/ancient_egypt", "Ancient Egypt", "🐫", QuizCategory.History),
        QuizSection("history/antiquity/ancient_greece", "Ancient Greece", "🏛️", QuizCategory.History),
        QuizSection("history/antiquity/ancient_persia", "Ancient Persia", "🦁", QuizCategory.History),
        QuizSection("history/antiquity/alexander_the_great", "Alexander & the Hellenistic World", "🐎", QuizCategory.History),
        QuizSection("history/antiquity/roman_republic", "Roman Republic", "⚖️", QuizCategory.History),
        QuizSection("history/antiquity/roman_empire", "Roman Empire", "🦅", QuizCategory.History),
        QuizSection("history/antiquity/ancient_china", "Ancient China", "🏮", QuizCategory.History),
        QuizSection("history/antiquity/ancient_india", "Ancient India", "🐘", QuizCategory.History),
        QuizSection("history/middle_ages", "Middle Ages", "🏰", QuizCategory.History),
        QuizSection("history/middle_ages/byzantium", "Byzantine Empire", "⛪", QuizCategory.History),
        QuizSection("history/middle_ages/early_middle_ages", "Franks & the Early Middle Ages", "🗡️", QuizCategory.History),
        QuizSection("history/middle_ages/vikings", "Vikings", "⛵", QuizCategory.History),
        QuizSection("history/middle_ages/islamic_world", "Islamic World", "🕌", QuizCategory.History),
        QuizSection("history/middle_ages/crusades", "Crusades", "✝️", QuizCategory.History),
        QuizSection("history/middle_ages/knights_castles", "Knights & Castles", "🛡️", QuizCategory.History),
        QuizSection("history/middle_ages/medieval_britain", "Medieval Britain", "📜", QuizCategory.History),
        QuizSection("history/middle_ages/hundred_years_war", "Hundred Years' War", "⚜️", QuizCategory.History),
        QuizSection("history/middle_ages/medieval_life", "Medieval Life & the Black Death", "🐀", QuizCategory.History),
        QuizSection("history/middle_ages/mongols", "Mongol Empire", "🏹", QuizCategory.History),
        QuizSection("history/middle_ages/imperial_china", "Imperial China & Korea", "🐉", QuizCategory.History),
        QuizSection("history/middle_ages/feudal_japan", "Samurai Japan", "⛩️", QuizCategory.History),
        QuizSection("history/middle_ages/african_kingdoms", "African Kingdoms", "🌍", QuizCategory.History),
        QuizSection("history/pre_columbian", "Aztecs, Maya & Inca", "☀️", QuizCategory.History),
        QuizSection("history/early_modern", "Early Modern Era", "🕰️", QuizCategory.History),
        QuizSection("history/early_modern/renaissance", "Renaissance", "🎨", QuizCategory.History),
        QuizSection("history/early_modern/age_of_discovery", "Age of Discovery", "🧭", QuizCategory.History),
        QuizSection("history/early_modern/reformation", "Reformation & Wars of Religion", "📖", QuizCategory.History),
        QuizSection("history/early_modern/tudors_stuarts", "Tudors & Stuarts", "🌹", QuizCategory.History),
        QuizSection("history/early_modern/absolutism", "Age of Absolutism", "👑", QuizCategory.History),
        QuizSection("history/early_modern/ottoman_empire", "Ottoman Empire", "🌙", QuizCategory.History),
        QuizSection("history/early_modern/mughal_india", "Mughal India", "💎", QuizCategory.History),
        QuizSection("history/early_modern/pirates", "Pirates & the High Seas", "☠️", QuizCategory.History),
        QuizSection("history/early_modern/colonial_america", "Colonial America", "🌽", QuizCategory.History),
        QuizSection("history/early_modern/enlightenment", "Science & Enlightenment", "🔭", QuizCategory.History),
        QuizSection("history/revolutions", "Age of Revolutions", "🔥", QuizCategory.History),
        QuizSection("history/revolutions/american_revolution", "American Revolution", "🔔", QuizCategory.History),
        QuizSection("history/revolutions/french_revolution", "French Revolution", "🗼", QuizCategory.History),
        QuizSection("history/revolutions/napoleon", "Napoleonic Era", "🎖️", QuizCategory.History),
        QuizSection("history/nineteenth_century", "19th Century", "⚙️", QuizCategory.History),
        QuizSection("history/nineteenth_century/industrial_revolution", "Industrial Revolution", "🏭", QuizCategory.History),
        QuizSection("history/nineteenth_century/victorian_era", "Victorian Britain & the Empire", "🎩", QuizCategory.History),
        QuizSection("history/nineteenth_century/europe", "Europe in the 1800s", "🚂", QuizCategory.History),
        QuizSection("history/nineteenth_century/us_civil_war", "American Civil War", "🎺", QuizCategory.History),
        QuizSection("history/nineteenth_century/wild_west", "Wild West & 1800s America", "🤠", QuizCategory.History),
        QuizSection("history/nineteenth_century/latin_america", "Latin America", "🌎", QuizCategory.History),
        QuizSection("history/nineteenth_century/imperialism", "Age of Imperialism", "🗺️", QuizCategory.History),
        QuizSection("history/twentieth_century", "20th Century", "📻", QuizCategory.History),
        QuizSection("history/twentieth_century/world_war_1", "World War I", "🪖", QuizCategory.History),
        QuizSection("history/twentieth_century/titanic", "Titanic", "🚢", QuizCategory.History),
        QuizSection("history/twentieth_century/russian_revolution", "Russian Revolution & the USSR", "🚩", QuizCategory.History),
        QuizSection("history/twentieth_century/interwar", "Between the Wars", "🎷", QuizCategory.History),
        QuizSection("history/twentieth_century/world_war_2", "World War II", "✈️", QuizCategory.History),
        QuizSection("history/twentieth_century/cold_war", "Cold War", "☢️", QuizCategory.History),
        QuizSection("history/twentieth_century/space_race", "Space Race", "🚀", QuizCategory.History),
        QuizSection("history/twentieth_century/decolonization", "End of Empires", "🕊️", QuizCategory.History),
        QuizSection("history/twentieth_century/civil_rights", "Civil Rights & Suffrage", "✊", QuizCategory.History),
        QuizSection("history/twentieth_century/modern_china", "Modern China", "🐼", QuizCategory.History),
        QuizSection("history/twentieth_century/explorers", "Explorers & Aviators", "🧗", QuizCategory.History),
        QuizSection("history/since_1990", "Since 1990", "🌐", QuizCategory.History),
        QuizSection("history/us_presidents", "US Presidents", "🇺🇸", QuizCategory.History),
        QuizSection("history/famous_women", "Women in History", "👩", QuizCategory.History)
    )
}
