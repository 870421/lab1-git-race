package es.unizar.webeng.hello

import es.unizar.webeng.hello.greeting.GreetingLanguage
import es.unizar.webeng.hello.greeting.GreetingService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.resttestclient.TestRestTemplate
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import java.time.LocalTime

/**
 * Integration tests for the complete application.
 *
 * Spring Boot starts a real web server on a random port.
 * The tests send real HTTP requests to the application.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class IntegrationTest {

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @Test
    fun `should return home page with modern title and client-side HTTP debug`() {
        val response = restTemplate.getForEntity(
            "http://localhost:$port",
            String::class.java
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).contains("<title>Modern Web App</title>")
        assertThat(response.body).contains("Welcome to Modern Web App")
        assertThat(response.body).contains("Interactive HTTP Testing & Debug")
        assertThat(response.body).contains("Client-Side Educational Tool")
    }

    @Test
    fun `should return personalized greeting when name is provided`() {
        // No lang parameter is sent, so Spanish is used by default
        val expectedPrefix = GreetingService.timeOfDayGreeting(
            LocalTime.now(),
            GreetingLanguage.SPANISH
        )

        val response = restTemplate.getForEntity(
            "http://localhost:$port?name=Developer",
            String::class.java
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).contains("$expectedPrefix, Developer!")
        assertThat(response.body).contains("Language: es")
    }

    @Test
    fun `should return an English greeting when lang=en is provided`() {
        val expectedPrefix = GreetingService.timeOfDayGreeting(
            LocalTime.now(),
            GreetingLanguage.ENGLISH
        )

        val response = restTemplate.getForEntity(
            "http://localhost:$port?name=Developer&lang=en",
            String::class.java
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).contains("$expectedPrefix, Developer!")
        assertThat(response.body).contains("Language: en")
    }

    @Test
    fun `should return API response with timestamp`() {
        // No lang parameter is sent, so Spanish is used by default
        val expectedPrefix = GreetingService.timeOfDayGreeting(
            LocalTime.now(),
            GreetingLanguage.SPANISH
        )

        val expectedTimeOfDay = GreetingService.timeOfDay(
            LocalTime.now()
        ).name.lowercase()

        val response = restTemplate.getForEntity(
            "http://localhost:$port/api/hello?name=Test",
            String::class.java
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.headers.contentType).isEqualTo(MediaType.APPLICATION_JSON)
        assertThat(response.body).contains("$expectedPrefix, Test!")
        assertThat(response.body).contains("\"language\":\"es\"")
        assertThat(response.body).contains("\"timeOfDay\":\"$expectedTimeOfDay\"")
        assertThat(response.body).contains("timestamp")
    }

    @Test
    fun `should honour the Accept-Language header on the API endpoint`() {
        val expectedPrefix = GreetingService.timeOfDayGreeting(
            LocalTime.now(),
            GreetingLanguage.ENGLISH
        )

        val headers = HttpHeaders().apply {
            set("Accept-Language", "en-US,en;q=0.9")
        }

        val response = restTemplate.exchange(
            "http://localhost:$port/api/hello?name=Test",
            org.springframework.http.HttpMethod.GET,
            HttpEntity<Void>(headers),
            String::class.java
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).contains("$expectedPrefix, Test!")
        assertThat(response.body).contains("\"language\":\"en\"")
    }

    @Test
    fun `should serve Bootstrap CSS correctly`() {
        val response = restTemplate.getForEntity(
            "http://localhost:$port/webjars/bootstrap/5.3.8/css/bootstrap.min.css",
            String::class.java
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).contains("body")
        assertThat(response.headers.contentType)
            .isEqualTo(MediaType.valueOf("text/css"))
    }

    @Test
    fun `should expose actuator health endpoint`() {
        val response = restTemplate.getForEntity(
            "http://localhost:$port/actuator/health",
            String::class.java
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).contains("UP")
    }

    @Test
    fun `should display client-side HTTP debug interface`() {
        val response = restTemplate.getForEntity(
            "http://localhost:$port?name=Student",
            String::class.java
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).contains("Interactive HTTP Testing & Debug")
        assertThat(response.body).contains("Client-Side Educational Tool")
        assertThat(response.body).contains("Web Page Greeting")
        assertThat(response.body).contains("API Endpoint")
        assertThat(response.body).contains("Health Check")
        assertThat(response.body).contains("Learning Notes:")
        assertThat(response.body).contains("Language")
    }
}