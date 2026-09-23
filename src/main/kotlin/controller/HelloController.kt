package es.unizar.webeng.hello.controller

import es.unizar.webeng.hello.greeting.GreetingService
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalTime

/**
 * Handles the web page greeting.
 *
 * The greeting depends on:
 * - the user's name,
 * - the current time,
 * - and the selected language.
 *
 * The language can come from the `lang` query parameter
 * or from the `Accept-Language` HTTP header.
 */
@Controller
class HelloController(

    // Gets the value of app.message from application.properties.
    // If it does not exist, "Hello World" is used.
    @param:Value("\${app.message:Hello World}")
    private val message: String
) {

    /**
     * Handles GET requests to the home page.
     *
     * Examples:
     * /?name=Mario
     * /?name=Mario&lang=es
     */
    @GetMapping("/")
    fun welcome(
        model: Model,

        // Gets the name from the URL.
        // If it is not present, an empty string is used.
        @RequestParam(defaultValue = "")
        name: String,

        // Optional language from the URL, for example ?lang=es.
        @RequestParam(required = false)
        lang: String? = null,

        // Optional language sent automatically by the browser.
        @RequestHeader(
            value = "Accept-Language",
            required = false
        )
        acceptLanguage: String? = null
    ): String {

        // Decides which language to use.
        val language =
            GreetingService.resolveLanguage(lang, acceptLanguage)

        // Gets the current server time.
        val time = LocalTime.now()

        // Builds the final greeting message.
        val greeting =
            GreetingService.greet(name, time, language, message)

        // Sends values to the Thymeleaf template.
        model.addAttribute("message", greeting)
        model.addAttribute("name", name)
        model.addAttribute("language", language.code)
        model.addAttribute(
            "timeOfDay",
            GreetingService.timeOfDay(time).name.lowercase()
        )

        // Loads src/main/resources/templates/welcome.html
        return "welcome"
    }
}

/**
 * Handles the JSON version of the greeting.
 *
 * It uses the same greeting logic as [HelloController],
 * but returns JSON instead of an HTML page.
 */
@RestController
class HelloApiController {

    /**
     * Handles GET requests to /api/hello.
     *
     * Example:
     * /api/hello?name=Mario&lang=es
     */
    @GetMapping(
        "/api/hello",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun helloApi(

        // Gets the name from the URL.
        // If it is not present, "World" is used.
        @RequestParam(defaultValue = "World")
        name: String,

        // Optional language from the URL.
        @RequestParam(required = false)
        lang: String? = null,

        // Optional language sent by the browser.
        @RequestHeader(
            value = "Accept-Language",
            required = false
        )
        acceptLanguage: String? = null
    ): Map<String, String> {

        // Decides which language to use.
        val language =
            GreetingService.resolveLanguage(lang, acceptLanguage)

        // Uses the same time for all values in the response.
        val time = LocalTime.now()

        // Spring converts this Map into JSON automatically.
        return mapOf(
            "message" to GreetingService.greet(
                name,
                time,
                language,
                "Welcome!"
            ),
            "language" to language.code,
            "timeOfDay" to GreetingService
                .timeOfDay(time)
                .name
                .lowercase(),
            "timestamp" to java.time.Instant.now().toString()
        )
    }
}