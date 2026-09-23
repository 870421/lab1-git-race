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
 * Renders the welcome page with a greeting the server picks based on the time of day
 * and the requested language (`lang` query parameter or `Accept-Language` header).
 */
@Controller
class HelloController(
    @param:Value("\${app.message:Hello World}")
    private val message: String
) {

    /**
     * Serves the home page. [name], when present, is greeted directly; otherwise the
     * time-of-day phrase is combined with the configured default [message]. Language
     * resolution follows [GreetingService.resolveLanguage].
     */
    @GetMapping("/")
    fun welcome(
        model: Model,
        @RequestParam(defaultValue = "") name: String,
        @RequestParam(required = false) lang: String? = null,
        @RequestHeader(value = "Accept-Language", required = false) acceptLanguage: String? = null
    ): String {
        val language = GreetingService.resolveLanguage(lang, acceptLanguage)
        val greeting = GreetingService.greet(name, LocalTime.now(), language, message)
        model.addAttribute("message", greeting)
        model.addAttribute("name", name)
        model.addAttribute("language", language.code)
        model.addAttribute("timeOfDay", GreetingService.timeOfDay(LocalTime.now()).name.lowercase())
        return "welcome"
    }
}

/** JSON counterpart of [HelloController], returning the same server-decided greeting. */
@RestController
class HelloApiController {

    /** Returns the greeting, resolved language, time-of-day segment and a timestamp as JSON. */
    @GetMapping("/api/hello", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun helloApi(
        @RequestParam(defaultValue = "World") name: String,
        @RequestParam(required = false) lang: String? = null,
        @RequestHeader(value = "Accept-Language", required = false) acceptLanguage: String? = null
    ): Map<String, String> {
        val language = GreetingService.resolveLanguage(lang, acceptLanguage)
        val time = LocalTime.now()
        return mapOf(
            "message" to GreetingService.greet(name, time, language, "Welcome!"),
            "language" to language.code,
            "timeOfDay" to GreetingService.timeOfDay(time).name.lowercase(),
            "timestamp" to java.time.Instant.now().toString()
        )
    }
}
