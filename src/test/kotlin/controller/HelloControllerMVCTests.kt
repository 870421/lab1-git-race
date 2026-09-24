package es.unizar.webeng.hello.controller

import es.unizar.webeng.hello.greeting.GreetingLanguage
import es.unizar.webeng.hello.greeting.GreetingService
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalTime

/**
 * MVC tests for the web and API controllers.
 *
 * These tests use MockMvc to simulate HTTP requests
 * without starting a real web server.
 */
@WebMvcTest(HelloController::class, HelloApiController::class)
class HelloControllerMVCTests {

    @Value("\${app.message:Welcome to the Modern Web App!}")
    private lateinit var message: String

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `should return home page with default message`() {
        // No lang parameter is sent, so Spanish is used by default
        val expectedPrefix = GreetingService.timeOfDayGreeting(
            LocalTime.now(),
            GreetingLanguage.SPANISH
        )

        mockMvc.perform(get("/"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(view().name("welcome"))
            .andExpect(model().attribute("message", equalTo("$expectedPrefix! $message")))
            .andExpect(model().attribute("name", equalTo("")))
            .andExpect(model().attribute("language", equalTo("es")))
            .andExpect(model().attribute("timeOfDay", equalTo(
                GreetingService.timeOfDay(LocalTime.now()).name.lowercase()
            )))
    }

    @Test
    fun `should return home page with personalized message`() {
        val expectedPrefix = GreetingService.timeOfDayGreeting(
            LocalTime.now(),
            GreetingLanguage.SPANISH
        )

        mockMvc.perform(
            get("/")
                .param("name", "Developer")
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(view().name("welcome"))
            .andExpect(model().attribute("message", equalTo("$expectedPrefix, Developer!")))
            .andExpect(model().attribute("name", equalTo("Developer")))
            .andExpect(model().attribute("language", equalTo("es")))
    }

    @Test
    fun `should use the lang query parameter to pick the language`() {
        val expectedPrefix = GreetingService.timeOfDayGreeting(
            LocalTime.now(),
            GreetingLanguage.ENGLISH
        )

        mockMvc.perform(
            get("/")
                .param("name", "Developer")
                .param("lang", "en")
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(view().name("welcome"))
            .andExpect(model().attribute("message", equalTo("$expectedPrefix, Developer!")))
            .andExpect(model().attribute("language", equalTo("en")))
    }

    @Test
    fun `should prefer lang parameter over Accept-Language header`() {
        val expectedPrefix = GreetingService.timeOfDayGreeting(
            LocalTime.now(),
            GreetingLanguage.ENGLISH
        )

        mockMvc.perform(
            get("/")
                .param("name", "Developer")
                .param("lang", "en")
                .header("Accept-Language", "es-ES,es;q=0.9")
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(view().name("welcome"))
            .andExpect(model().attribute("message", equalTo("$expectedPrefix, Developer!")))
            .andExpect(model().attribute("language", equalTo("en")))
    }

    @Test
    fun `should use Accept-Language header when lang is absent`() {
        val expectedPrefix = GreetingService.timeOfDayGreeting(
            LocalTime.now(),
            GreetingLanguage.ENGLISH
        )

        mockMvc.perform(
            get("/")
                .param("name", "Developer")
                .header("Accept-Language", "en-US,en;q=0.9")
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(view().name("welcome"))
            .andExpect(model().attribute("message", equalTo("$expectedPrefix, Developer!")))
            .andExpect(model().attribute("language", equalTo("en")))
    }

    @Test
    fun `should return API response as JSON`() {
        val expectedPrefix = GreetingService.timeOfDayGreeting(
            LocalTime.now(),
            GreetingLanguage.SPANISH
        )

        val expectedTimeOfDay = GreetingService.timeOfDay(
            LocalTime.now()
        ).name.lowercase()

        mockMvc.perform(
            get("/api/hello")
                .param("name", "Test")
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.message", equalTo("$expectedPrefix, Test!")))
            .andExpect(jsonPath("$.language", equalTo("es")))
            .andExpect(jsonPath("$.timeOfDay", equalTo(expectedTimeOfDay)))
            .andExpect(jsonPath("$.timestamp").exists())
    }
}