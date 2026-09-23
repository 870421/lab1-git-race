package es.unizar.webeng.hello.greeting

import java.time.LocalTime

/**
 * Supported languages for the greeting.
 *
 * Each language has a short code used in the `lang` query parameter
 * and in the `Accept-Language` HTTP header.
 */
enum class GreetingLanguage(val code: String) {
    ENGLISH("en"),
    SPANISH("es");

    companion object {

        // Creates a map like:
        // "en" -> ENGLISH
        // "es" -> SPANISH
        private val byCode = entries.associateBy { it.code }

        /**
         * Converts a language code into a [GreetingLanguage].
         *
         * Returns `null` if the code is empty or not supported.
         */
        fun fromCode(code: String?): GreetingLanguage? =
            code
                ?.trim() // Removes spaces before and after the text
                ?.takeIf { it.isNotEmpty() } // Keeps the value only if it is not empty
                ?.lowercase() // Makes the comparison case-insensitive
                ?.let { byCode[it] } // Looks for the code in the map
    }
}

/**
 * Parts of the day used to choose the greeting.
 */
enum class TimeOfDay {
    MORNING,
    AFTERNOON,
    EVENING,
    NIGHT
}

/**
 * Contains the logic used to build greetings.
 *
 * It decides:
 * - the part of the day,
 * - the language,
 * - and the final greeting message.
 *
 * The functions receive all required data as parameters,
 * which makes them easy to test without Spring.
 */
object GreetingService {

    // Stores the greeting text for each language and part of the day.
    private val phrases: Map<GreetingLanguage, Map<TimeOfDay, String>> = mapOf(
        GreetingLanguage.ENGLISH to mapOf(
            TimeOfDay.MORNING to "Good morning",
            TimeOfDay.AFTERNOON to "Good afternoon",
            TimeOfDay.EVENING to "Good evening",
            TimeOfDay.NIGHT to "Good night"
        ),
        GreetingLanguage.SPANISH to mapOf(
            TimeOfDay.MORNING to "Buenos días",
            TimeOfDay.AFTERNOON to "Buenas tardes",
            TimeOfDay.EVENING to "Buenas tardes",
            TimeOfDay.NIGHT to "Buenas noches"
        )
    )

    /**
     * Converts a time into a [TimeOfDay].
     */
    fun timeOfDay(time: LocalTime): TimeOfDay = when (time.hour) {
        in 6..12 -> TimeOfDay.MORNING
        in 13..17 -> TimeOfDay.AFTERNOON
        in 18..20 -> TimeOfDay.EVENING
        else -> TimeOfDay.NIGHT
    }

    /**
     * Returns the greeting text for a given time and language.
     *
     * Example: 10:00 + Spanish -> "Buenos días".
     */
    fun timeOfDayGreeting(
        time: LocalTime,
        language: GreetingLanguage
    ): String =
        phrases
            .getValue(language) // Gets the map for the selected language
            .getValue(timeOfDay(time)) // Gets the phrase for the current part of the day

    /**
     * Chooses which language should be used.
     *
     * Priority:
     * 1. `lang` query parameter.
     * 2. `Accept-Language` HTTP header.
     * 3. Spanish by default.
     */
    fun resolveLanguage(
        langParam: String?,
        acceptLanguageHeader: String?
    ): GreetingLanguage {

        // If a valid language was sent in ?lang=..., use it immediately.
        GreetingLanguage.fromCode(langParam)?.let {
            return it
        }

        // Example header:
        // es-ES,es;q=0.9,en;q=0.8
        //
        // This code extracts only the first language code: "es".
        val headerLanguage = acceptLanguageHeader
            ?.split(",")
            ?.firstOrNull()
            ?.split(";")
            ?.firstOrNull()
            ?.split("-")
            ?.firstOrNull()

        // If the browser language is supported, use it.
        // Otherwise, use Spanish.
        return GreetingLanguage.fromCode(headerLanguage)
            ?: GreetingLanguage.SPANISH
    }

    /**
     * Builds the final greeting message.
     *
     * If a name is provided:
     * "Good afternoon, Mario!"
     *
     * If no name is provided:
     * "Good afternoon! $defaultMessage!"
     */
    fun greet(
        name: String,
        time: LocalTime,
        language: GreetingLanguage,
        defaultMessage: String
    ): String {

        // Gets the first part of the greeting, for example "Buenas tardes".
        val prefix = timeOfDayGreeting(time, language)

        // If there is a name, greet the user directly.
        // Otherwise, append the default message.
        return if (name.isNotBlank()) {
            "$prefix, $name!"
        } else {
            "$prefix! $defaultMessage"
        }
    }
}