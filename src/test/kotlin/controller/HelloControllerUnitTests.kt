package es.unizar.webeng.hello.controller

import es.unizar.webeng.hello.greeting.GreetingLanguage
import es.unizar.webeng.hello.greeting.GreetingService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.ui.ExtendedModelMap
import org.springframework.ui.Model
import java.time.LocalTime

/**
 * Unit tests for the web and API controllers.
 *
 * These tests call the controller methods directly.
 * Spring and HTTP are not started here.
 */
class HelloControllerUnitTests {

    private lateinit var controller: HelloController
    private lateinit var model: Model

    @BeforeEach
    fun setup() {
        // Creates a controller with a fixed message for the tests
        controller = HelloController("Test Message")

        // Simple Model implementation used without starting Spring
        model = ExtendedModelMap()
    }

    @Test
    fun `should return welcome view with default message`() {
        val view = controller.welcome(model, "")

        // Spanish is the default language
        val expectedPrefix = GreetingService.timeOfDayGreeting(
            LocalTime.now(),
            GreetingLanguage.SPANISH
        )

        assertThat(view).isEqualTo("welcome")
        assertThat(model.getAttribute("message"))
            .isEqualTo("$expectedPrefix! Test Message")
        assertThat(model.getAttribute("name")).isEqualTo("")
        assertThat(model.getAttribute("language")).isEqualTo("es")
        assertThat(model.getAttribute("timeOfDay")).isNotNull()
    }

    @Test
    fun `should return welcome view with personalized message`() {
        val view = controller.welcome(model, "Developer")

        val expectedPrefix = GreetingService.timeOfDayGreeting(
            LocalTime.now(),
            GreetingLanguage.SPANISH
        )

        assertThat(view).isEqualTo("welcome")
        assertThat(model.getAttribute("message"))
            .isEqualTo("$expectedPrefix, Developer!")
        assertThat(model.getAttribute("name")).isEqualTo("Developer")
        assertThat(model.getAttribute("language")).isEqualTo("es")
    }

    @Test
    fun `should honour the lang parameter over the Accept-Language header`() {
        val view = controller.welcome(
            model,
            "Developer",
            lang = "es",
            acceptLanguage = "en-US"
        )

        val expectedPrefix = GreetingService.timeOfDayGreeting(
            LocalTime.now(),
            GreetingLanguage.SPANISH
        )

        assertThat(view).isEqualTo("welcome")
        assertThat(model.getAttribute("message"))
            .isEqualTo("$expectedPrefix, Developer!")
        assertThat(model.getAttribute("language")).isEqualTo("es")
    }

    @Test
    fun `should use English when lang parameter is en`() {
        val view = controller.welcome(
            model,
            "Developer",
            lang = "en",
            acceptLanguage = "es-ES"
        )

        val expectedPrefix = GreetingService.timeOfDayGreeting(
            LocalTime.now(),
            GreetingLanguage.ENGLISH
        )

        assertThat(view).isEqualTo("welcome")
        assertThat(model.getAttribute("message"))
            .isEqualTo("$expectedPrefix, Developer!")
        assertThat(model.getAttribute("language")).isEqualTo("en")
    }

    @Test
    fun `should return API response with timestamp`() {
        val apiController = HelloApiController()

        val response = apiController.helloApi("Test")

        // No lang is provided, so Spanish is used by default
        val expectedPrefix = GreetingService.timeOfDayGreeting(
            LocalTime.now(),
            GreetingLanguage.SPANISH
        )

        assertThat(response).containsKey("message")
        assertThat(response).containsKey("timestamp")
        assertThat(response).containsKey("language")
        assertThat(response).containsKey("timeOfDay")

        assertThat(response["message"])
            .isEqualTo("$expectedPrefix, Test!")
        assertThat(response["timestamp"]).isNotNull()
        assertThat(response["language"]).isEqualTo("es")
        assertThat(response["timeOfDay"]).isNotNull()
    }
}