package es.unizar.webeng.hello.greeting

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalTime

/**
 * Unit tests for GreetingService.
 *
 * These tests check the greeting logic directly,
 * without starting Spring or making HTTP requests.
 */
class GreetingServiceTest {

    @Test
    fun `should classify boundary hours into the right time of day`() {
        // Night: 21:00 - 05:59
        assertThat(GreetingService.timeOfDay(LocalTime.of(0, 0)))
            .isEqualTo(TimeOfDay.NIGHT)

        assertThat(GreetingService.timeOfDay(LocalTime.of(5, 59)))
            .isEqualTo(TimeOfDay.NIGHT)

        // Morning: 06:00 - 12:59
        assertThat(GreetingService.timeOfDay(LocalTime.of(6, 0)))
            .isEqualTo(TimeOfDay.MORNING)

        assertThat(GreetingService.timeOfDay(LocalTime.of(12, 59)))
            .isEqualTo(TimeOfDay.MORNING)

        // Afternoon: 13:00 - 17:59
        assertThat(GreetingService.timeOfDay(LocalTime.of(13, 0)))
            .isEqualTo(TimeOfDay.AFTERNOON)

        assertThat(GreetingService.timeOfDay(LocalTime.of(17, 59)))
            .isEqualTo(TimeOfDay.AFTERNOON)

        // Evening: 18:00 - 20:59
        assertThat(GreetingService.timeOfDay(LocalTime.of(18, 0)))
            .isEqualTo(TimeOfDay.EVENING)

        assertThat(GreetingService.timeOfDay(LocalTime.of(20, 59)))
            .isEqualTo(TimeOfDay.EVENING)

        // From 21:00 it is night again
        assertThat(GreetingService.timeOfDay(LocalTime.of(21, 0)))
            .isEqualTo(TimeOfDay.NIGHT)
    }

    @Test
    fun `should return English phrase for each time of day`() {
        assertThat(
            GreetingService.timeOfDayGreeting(
                LocalTime.of(8, 0),
                GreetingLanguage.ENGLISH
            )
        ).isEqualTo("Good morning")

        assertThat(
            GreetingService.timeOfDayGreeting(
                LocalTime.of(14, 0),
                GreetingLanguage.ENGLISH
            )
        ).isEqualTo("Good afternoon")

        assertThat(
            GreetingService.timeOfDayGreeting(
                LocalTime.of(19, 0),
                GreetingLanguage.ENGLISH
            )
        ).isEqualTo("Good evening")

        assertThat(
            GreetingService.timeOfDayGreeting(
                LocalTime.of(2, 0),
                GreetingLanguage.ENGLISH
            )
        ).isEqualTo("Good night")
    }

    @Test
    fun `should return Spanish phrase for each time of day`() {
        assertThat(
            GreetingService.timeOfDayGreeting(
                LocalTime.of(8, 0),
                GreetingLanguage.SPANISH
            )
        ).isEqualTo("Buenos días")

        assertThat(
            GreetingService.timeOfDayGreeting(
                LocalTime.of(14, 0),
                GreetingLanguage.SPANISH
            )
        ).isEqualTo("Buenas tardes")

        // Evening also uses "Buenas tardes" in Spanish
        assertThat(
            GreetingService.timeOfDayGreeting(
                LocalTime.of(19, 0),
                GreetingLanguage.SPANISH
            )
        ).isEqualTo("Buenas tardes")

        assertThat(
            GreetingService.timeOfDayGreeting(
                LocalTime.of(2, 0),
                GreetingLanguage.SPANISH
            )
        ).isEqualTo("Buenas noches")
    }

    @Test
    fun `should prefer the lang query parameter over the Accept-Language header`() {
        // Explicit lang parameter has priority over the browser header
        val language = GreetingService.resolveLanguage(
            "es",
            "en-US,en;q=0.9"
        )

        assertThat(language).isEqualTo(GreetingLanguage.SPANISH)
    }

    @Test
    fun `should fall back to the Accept-Language header when lang is absent`() {
        val language = GreetingService.resolveLanguage(
            null,
            "es-ES,es;q=0.9,en;q=0.8"
        )

        assertThat(language).isEqualTo(GreetingLanguage.SPANISH)
    }

    @Test
    fun `should default to Spanish when nothing is provided or supported`() {
        // Spanish is the default language of the application
        assertThat(
            GreetingService.resolveLanguage(null, null)
        ).isEqualTo(GreetingLanguage.SPANISH)

        assertThat(
            GreetingService.resolveLanguage("fr", "fr-FR")
        ).isEqualTo(GreetingLanguage.SPANISH)

        assertThat(
            GreetingService.resolveLanguage("", "")
        ).isEqualTo(GreetingLanguage.SPANISH)
    }

    @Test
    fun `should greet a named visitor without the default message`() {
        val greeting = GreetingService.greet(
            "Developer",
            LocalTime.of(9, 0),
            GreetingLanguage.ENGLISH,
            "Welcome!"
        )

        assertThat(greeting)
            .isEqualTo("Good morning, Developer!")
    }

    @Test
    fun `should greet an anonymous visitor with the default message`() {
        val greeting = GreetingService.greet(
            "",
            LocalTime.of(20, 0),
            GreetingLanguage.SPANISH,
            "Bienvenido!"
        )

        assertThat(greeting)
            .isEqualTo("Buenas tardes! Bienvenido!")
    }
}