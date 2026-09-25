# Lab 1 Git Race -- Project Report

This note uses the same disclosure fields as the group-project **AI use (10%)** slice. Lab 1 is still **limited**: assistive GenAI only — not a full or substantial generated solution. The project will later expect agents plus `AGENTS.md` and one skill; you do **not** need those here.

Do not invent a percentage of “AI vs original” lines. Empty or fake disclosure fails this lab.

## What I specified

I decided to extend the original greeting application with greetings that depend on the current time and the selected language.

The application should support English and Spanish. The language can be selected with the `lang` query parameter (`?lang=en` or `?lang=es`). If this parameter is not provided, the application should check the `Accept-Language` HTTP header. If no supported language is found, Spanish should be used by default.

The greeting should also depend on the current time of day: morning, afternoon, evening or night. The same behaviour should be available both on the HTML web page and on the `/api/hello` JSON endpoint.

I would know the increment works if:
- the greeting changes depending on the current time;
- `?lang=en` returns an English greeting;
- `?lang=es` returns a Spanish greeting;
- the `lang` parameter has priority over the `Accept-Language` header;
- Spanish is used when no supported language is provided;
- both the web page and the JSON API return the expected greeting.

## What I changed

I added a new `GreetingService` to keep the greeting logic separate from the controllers. This service decides the language, the time of day and the final greeting message.

I modified `HelloController.kt` so that both the HTML page and the `/api/hello` endpoint use the same greeting logic. The controllers now read the optional `lang` query parameter and the `Accept-Language` header, and they also expose the selected language and time of day.

I updated `welcome.html` to display the current language and time of day below the greeting. I also added a language selector so the interactive HTTP tests can send `lang=es` or `lang=en`.

I modified `http-debug.js` so the Web Page and API test buttons include the selected language in their requests. The API test also updates the greeting, language and time-of-day information shown on the page.

I added unit tests for `GreetingService` and updated the existing controller unit tests, MVC tests and integration tests to cover the new language and time-based behaviour.

## Technical decisions

I decided to keep the greeting logic in a separate `GreetingService` instead of duplicating it in the web controller and the API controller. This makes the code easier to reuse and test.

The `lang` query parameter has priority over the `Accept-Language` header because I wanted the language selected in the URL to have priority over the browser language. If neither provides a supported language, Spanish is used as the default language.

The time of day is divided into four ranges:
- morning: 06:00–12:59
- afternoon: 13:00–17:59
- evening: 18:00–20:59
- night: 21:00–05:59

Both the HTML page and the JSON API use the same greeting rules so their behaviour stays consistent.

For the interactive debug interface, I used one shared language selector for both the web-page test and the API test. I preferred this over using two separate selectors because both tests use the same language options.

I also kept different test levels: unit tests for the greeting logic and controllers, MVC tests with `MockMvc`, and integration tests with a real Spring Boot server on a random port. This allows the behaviour to be checked at different levels.

I rejected putting all the greeting logic directly inside the controllers because that would duplicate code and make testing harder.

## How I verified

I first ran `./gradlew check` to compile the project and run the configured tests. At the beginning, the command failed because the project required Java 25 but my system was using Java 23. I installed Java 25 LTS and ran the command again successfully.

I also ran `./gradlew bootRun` and checked that the original application worked correctly before making my changes. I verified that the home page loaded correctly and returned an HTML response, the `/api/hello` endpoint returned JSON, and `/actuator/health` returned an `UP` status.

After implementing the new greeting behaviour, I ran `./gradlew bootRun` again and tested the application manually in the browser.

I checked:
- the home page with and without a name;
- `?lang=es` and `?lang=en`;
- the default Spanish language when no supported language is provided;
- the `Accept-Language` header;
- the `/api/hello` JSON endpoint;
- the language and time-of-day values shown on the page;
- the interactive Web Page and API test buttons.

During development, some tests became outdated after I adjusted the time-of-day ranges and the Spanish greeting texts. I updated the unit, MVC and integration tests so that they matched the final behaviour of the application.

I also noticed that the interactive test buttons did not send the selected language, so I added a shared `es/en` selector and updated the JavaScript requests. After this change, I checked again that both test buttons sent the selected language correctly, and that the API test updated the greeting, language and time-of-day information shown on the page.

Finally, I ran `./gradlew check` again. The final result was `BUILD SUCCESSFUL`, so the unit, MVC and integration tests passed.

## AI disclosure

- **Tools / skills:** Claude Code and ChatGPT.

- **Purpose:** I used Claude Code to help create the first version of the greeting feature, including the new greeting service and changes in the controllers, tests and web interface. I used ChatGPT to review the generated code, understand how it worked, detect inconsistencies, adapt the behaviour to my final decisions, improve comments and documentation, and update the tests.

- **Representative prompts:** Examples include asking Claude Code to extend the original application with greetings depending on language and time of day, and asking ChatGPT to explain the generated Kotlin and test code, review the behaviour of `lang` and `Accept-Language`, add a language selector to the debug interface, and update the tests to match the final implementation.

- **Affected files/sections:** `GreetingService.kt`, `HelloController.kt`, `welcome.html`, `http-debug.js`, `GreetingServiceTest.kt`, `HelloControllerUnitTests.kt`, `HelloControllerMVCTests.kt`, `IntegrationTest.kt`, and parts of `REPORT.md`.

- **Validation steps:** I manually reviewed the generated changes, ran the application with `./gradlew bootRun`, tested the web page and `/api/hello` endpoint with different names and languages, checked the `Accept-Language` behaviour, and ran `./gradlew check`. The final result was `BUILD SUCCESSFUL`.

- **Citations:** No external code snippets were copied or adapted.

- **Human-reviewed:** I reviewed and changed the generated code before submission. I modified the default language behaviour, time-of-day ranges, Spanish greeting texts, comments, the language selector, the JavaScript behaviour, and the unit, MVC and integration tests. I also checked that I could explain the controllers, `GreetingService`, MockMvc tests and integration tests.
