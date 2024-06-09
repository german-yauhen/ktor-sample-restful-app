package com.example

import com.example.model.Priority
import com.example.model.Task
import com.jayway.jsonpath.JsonPath
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.server.testing.*
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.test.assertContains
import kotlin.test.assertTrue

class ApplicationJsonPathTest {

    @Test
    fun tasksCanBeFound() = testApplication {
        val responseBody = client
            .get("/tasks") { accept(ContentType.Application.Json) }
            .bodyAsText()
        val json = JsonPath.parse(responseBody)

        val taskNames: List<String> = json.read("$[*].name")
        assertEquals("cleaning", taskNames[0])
        assertEquals("gardening", taskNames[1])
        assertEquals("shopping", taskNames[2])
        assertEquals("painting", taskNames[3])
    }

    @Test
    fun taskCanBeFoundByName() = testApplication {
        val name = "painting"
        val responseBody = client
            .get("/tasks/byName/$name") { accept(ContentType.Application.Json) }
            .bodyAsText()
        val json = JsonPath.parse(responseBody)

        assertEquals(name, json.read("$['name']"))
    }

    @Test
    fun taskCannotBeFoundByNameAndStatusBadRequest() = testApplication {
        val name = "%20" // Inclines an empty string containing only whitespace
        val response = client
            .get("/tasks/byName/%20") { accept(ContentType.Application.Json) }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun taskCannotBeFoundByNameAndStatusNotFound() = testApplication {
        val name = "unknown"
        val response = client
            .get("/tasks/byName/$name") { accept(ContentType.Application.Json) }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun taskCanBeCreatedAndListOfTasksReflectsCreatedOne() = testApplication {
        val newTask = Task("refactoring", "refactor the code", Priority.Low)

        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }
        val response = client.post("/tasks/creation") {
            contentType(ContentType.Application.Json)
            setBody(newTask)
        }
        assertTrue(HttpStatusCode.PermanentRedirect == response.status || HttpStatusCode.Found == response.status)

        val responseBody = client
            .get("/tasks") { accept(ContentType.Application.Json) }
            .bodyAsText()
        val json = JsonPath.parse(responseBody)

        val taskNames: List<String> = json.read("$[*].name")
        assertContains(taskNames, newTask.name)
    }

}