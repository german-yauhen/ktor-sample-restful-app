package com.example.plugins

import com.example.model.Priority
import com.example.model.Task
import com.example.model.TaskRepository
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        install(ContentNegotiation) {
            json()
        }
        // Static plugin. Try to access `/static/index.html`
        staticResources("/static", "static")
        staticResources("/tasks/creation", "static")

        route("/tasks") {
            get {
                call.respond(TaskRepository.allTasks())
            }

            get("/byName/{name}") {
                val name = call.parameters["name"]
                if (name.isNullOrBlank()) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }
                val task = TaskRepository.withName(name)
                if (task == null) {
                    call.respond(HttpStatusCode.NotFound)
                    return@get
                }
                call.respond(task)
            }

            get("/byPriority/{priority}") {
                val priorityParam = call.parameters["priority"]
                if (priorityParam == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }
                val priority = Priority.entries.find { it.name.equals(priorityParam, ignoreCase = true) }
                if (priority == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        "The specified $priorityParam priority is not maintained."
                    )
                    return@get
                }
                val tasks = TaskRepository.withPriority(priority)
                if (tasks.isEmpty()) {
                    call.respond(HttpStatusCode.NotFound)
                    return@get
                }
                call.respond(tasks)
            }

            post("/creation") {
                val task = call.receive<Task>()
                TaskRepository.add(task)
                call.respondRedirect("/tasks")
            }
        }
    }
}
