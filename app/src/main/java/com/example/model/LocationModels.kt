package com.example.model

data class CityLocation(
    val name: String,
    val subLocations: List<String>
)

val ethiopianCities = listOf(
    CityLocation("Addis Ababa", listOf(
        "Bole", "Yeka", "Kirkos", "Lideta", "Arada", "Gullele", 
        "Kolfe Keranio", "Nifas Silk-Lafto", "Akaki Kality", "Lemi Kura"
    )),
    CityLocation("Hawassa", listOf(
        "Tabor", "Misrak", "Hayipha", "Millennium", "Lewi", "Amora Gedel", "Bahel Adarash", "Hawassa Lake"
    )),
    CityLocation("Adama", listOf(
        "Bole", "Kebele 01", "Geda", "Melka Adama", "Franco"
    )),
    CityLocation("Bahir Dar", listOf(
        "Kebele 3", "Kebele 4", "Bezawit", "Ginbot 20", "Shimbit", "Tana", "Abay"
    )),
    CityLocation("Dessie", listOf(
        "Kebele 1", "Kebele 2", "Segno Gebeya", "Hotie", "Menen", "Robit", "Arab Ganda"
    )),
    CityLocation("Jimma", listOf(
        "Ginjo", "Hermata", "Kochi", "Mendera", "Jiren", "Seto Semero"
    )),
    CityLocation("Mekelle", listOf(
        "Adi Haki", "Kedamay Weyane", "Ayder", "Hadnet", "Hawelti", "Semien"
    )),
    CityLocation("Gondar", listOf(
        "Arada", "Azezo", "Piazza", "Kebele 18", "Maraki", "Wolleka"
    )),
    CityLocation("Dire Dawa", listOf(
        "Kebele 01", "Kebele 02", "Sabian", "Depo", "Shinile"
    )),
    CityLocation("Harar", listOf(
        "Jegol", "Kebele 01", "Kebele 02", "Dakar", "Aboker"
    )),
    CityLocation("Jijiga", listOf(
        "Kebele 01", "Kebele 02", "Kebele 03"
    )),
    CityLocation("Arba Minch", listOf(
        "Sikela", "Secha", "Limat"
    )),
    CityLocation("Hosaena", listOf(
        "Kebele 01", "Kebele 02", "Lichamba"
    )),
    CityLocation("Dilla", listOf(
        "Kebele 01", "Kebele 02", "Haroresa"
    )),
    CityLocation("Nekemte", listOf(
        "Kebele 01", "Kebele 02"
    )),
    CityLocation("Debre Birhan", listOf(
        "Kebele 01", "Kebele 02", "Tebase"
    )),
    CityLocation("Debre Markos", listOf(
        "Kebele 01", "Kebele 02"
    )),
    CityLocation("Asella", listOf(
        "Kebele 01", "Kebele 02"
    )),
    CityLocation("Bishoftu", listOf(
        "Babogaya", "Bishoftu Lake", "Hora", "Kuriftu"
    )),
    CityLocation("Shashemene", listOf(
        "Kebele 01", "Kebele 02", "Rastafarian Quarter"
    )),
    CityLocation("Gambela", listOf(
        "Kebele 01", "Kebele 02"
    )),
    CityLocation("Semera", listOf(
        "Kebele 01", "Logia"
    ))
)
