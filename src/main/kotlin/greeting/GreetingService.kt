package es.unizar.webeng.hello.greeting

import java.time.LocalTime

/**
 * Idiomas soportados por el saludo localizado.
 *
 * @property code la etiqueta de dos letras usada en el parametro `lang` y
 *   extraida de la cabecera `Accept-Language` (p. ej. `"en"`, `"es"`).
 */
enum class GreetingLanguage(val code: String) {
    ENGLISH("en"),
    SPANISH("es");

    companion object {
        private val byCode = entries.associateBy { it.code }

        /** Resuelve un idioma a partir de [code], sin distinguir mayusculas/minusculas, o `null` si no esta soportado/esta vacio. */
        fun fromCode(code: String?): GreetingLanguage? =
            code?.trim()?.takeIf { it.isNotEmpty() }?.lowercase()?.let { byCode[it] }
    }
}

/** Franja horaria usada para elegir la frase de saludo. */
enum class TimeOfDay { MORNING, AFTERNOON, EVENING, NIGHT }

/**
 * Logica pura y sin estado detras del saludo decidido por el servidor: que [TimeOfDay]
 * es, que [GreetingLanguage] usar, y la frase de saludo final.
 *
 * Se mantiene como [object] con funciones que reciben sus datos de entrada explicitamente
 * (sin reloj ni estado de la peticion oculto), de forma que el comportamiento es trivial
 * de testear sin Spring.
 */
object GreetingService {

    private val phrases: Map<GreetingLanguage, Map<TimeOfDay, String>> = mapOf(
        GreetingLanguage.ENGLISH to mapOf(
            TimeOfDay.MORNING to "Good morning",
            TimeOfDay.AFTERNOON to "Good afternoon",
            TimeOfDay.EVENING to "Good evening",
            TimeOfDay.NIGHT to "Good night"
        ),
        GreetingLanguage.SPANISH to mapOf(
            TimeOfDay.MORNING to "Buenos dias",
            TimeOfDay.AFTERNOON to "Buenas tardes",
            TimeOfDay.EVENING to "Buenas noches",
            TimeOfDay.NIGHT to "Buenas noches"
        )
    )

    /** Convierte una hora del dia en una franja [TimeOfDay]: 5-11 manana, 12-17 tarde, 18-21 noche-tarde, resto noche. */
    fun timeOfDay(time: LocalTime): TimeOfDay = when (time.hour) {
        in 5..11 -> TimeOfDay.MORNING
        in 12..17 -> TimeOfDay.AFTERNOON
        in 18..21 -> TimeOfDay.EVENING
        else -> TimeOfDay.NIGHT
    }

    /** Devuelve la frase de saludo (p. ej. "Good morning") para [time] en [language]. */
    fun timeOfDayGreeting(time: LocalTime, language: GreetingLanguage): String =
        phrases.getValue(language).getValue(timeOfDay(time))

    /**
     * Resuelve que [GreetingLanguage] usar: un parametro `lang` explicito tiene prioridad;
     * si no, se usa la primera etiqueta de la cabecera `Accept-Language`; si ninguno esta
     * presente o soportado, se usa [GreetingLanguage.ENGLISH] por defecto.
     */
    fun resolveLanguage(langParam: String?, acceptLanguageHeader: String?): GreetingLanguage {
        GreetingLanguage.fromCode(langParam)?.let { return it }
        val headerLanguage = acceptLanguageHeader
            ?.split(",")
            ?.firstOrNull()
            ?.split(";")
            ?.firstOrNull()
            ?.split("-")
            ?.firstOrNull()
        return GreetingLanguage.fromCode(headerLanguage) ?: GreetingLanguage.SPANISH
    }

    /**
     * Construye la frase de saludo final para [time] y [language].
     *
     * Cuando [name] esta vacio, devuelve la frase de la franja horaria seguida de
     * [defaultMessage] (p. ej. "Good morning! Welcome to the Modern Web App!"); en caso
     * contrario saluda a [name] directamente (p. ej. "Good morning, Developer!").
     */
    fun greet(name: String, time: LocalTime, language: GreetingLanguage, defaultMessage: String): String {
        val prefix = timeOfDayGreeting(time, language)
        return if (name.isNotBlank()) "$prefix, $name!" else "$prefix! $defaultMessage"
    }
}
