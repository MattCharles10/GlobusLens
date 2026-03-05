package com.globuslens.utils


object Constants {
    // Navigation Routes
    const val ROUTE_SCANNER = "scanner"
    const val ROUTE_RESULT = "result"
    const val ROUTE_FAVORITES = "favorites"
    const val ROUTE_SHOPPING_LIST = "shopping_list"
    const val ROUTE_PRODUCT_DETAIL = "product_detail/{productId}"

    // API Endpoints
    const val LIBRE_TRANSLATE_API = "https://libretranslate.com/"
    const val MY_MEMORY_API = "https://api.mymemory.translated.net/"

    // Database
    const val DATABASE_NAME = "globuslens_database"
    const val PRODUCT_TABLE = "products"
    const val SHOPPING_LIST_TABLE = "shopping_list"

    // Translation
    const val DEFAULT_TARGET_LANG = "en"
    const val SOURCE_LANG_AUTO = "auto"
    const val MY_MEMORY_EMAIL = "user@example.com" // Replace with your email

    // Preferences
    const val PREF_NAME = "globuslens_prefs"
    const val PREF_FIRST_LAUNCH = "first_launch"
    const val PREF_TARGET_LANGUAGE = "target_language"

    // Camera
    const val CAMERA_PERMISSION_REQUEST = 1001

    // Scanner
    const val SCAN_DELAY_MS = 2000L
    const val MIN_TEXT_LENGTH = 3

    // Offline Dictionary
    val OFFLINE_DICTIONARY = mapOf(
        "milk" to "moloko",
        "bread" to "khleb",
        "water" to "voda",
        "sugar" to "sakhar",
        "salt" to "sol",
        "butter" to "maslo",
        "cheese" to "syr",
        "meat" to "myaso",
        "fish" to "ryba",
        "chicken" to "kuritsa",
        "rice" to "ris",
        "pasta" to "makorony",
        "eggs" to "yaytsa",
        "apple" to "yabloko",
        "banana" to "banan",
        "orange" to "apelsin",
        "tomato" to "pomidor",
        "potato" to "kartofel",
        "onion" to "luk",
        "garlic" to "chesnok"
    )
}