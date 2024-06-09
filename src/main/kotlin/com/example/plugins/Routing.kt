package com.example.plugins

import com.example.model.Priority
import com.example.model.Task
import com.example.model.TaskRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.thymeleaf.*

fun Application.configureRouting() {
    routing {

        staticResources("/static", "static")

        route("/tasks") {
            get {
                val tasks = TaskRepository.allTasks()
                call.respond(
                    ThymeleafContent(
                        template = "all-tasks",
                        model = mapOf("tasks" to tasks)
                    )
                )
            }

            get("/byName") {
                val name = call.request.queryParameters["name"]
                if (name.isNullOrBlank()) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }
                val task = TaskRepository.withName(name)
                if (task == null) {
                    call.respond(HttpStatusCode.NotFound)
                    return@get
                }
                call.respond(
                    ThymeleafContent(
                        template = "single-task",
                        model = mapOf("task" to task)
                    )
                )
            }

            get("/byPriority") {
                val priorityParam = call.request.queryParameters["priority"]
                val priority = Priority.entries.find { it.name.equals(priorityParam, ignoreCase = true) }
                if (priority == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        "The specified $priorityParam priority is not maintained."
                    )
                    return@get
                }
                val tasks = TaskRepository.withPriority(priority)
                call.respond(
                    ThymeleafContent(
                        template = "tasks-by-priority",
                        model = mapOf("tasks" to tasks)
                    )
                )
            }

            post("/creation") {
                val formContent = call.receiveParameters()
                val params = Triple(
                    formContent["name"] ?: "",
                    formContent["description"] ?: "",
                    formContent["priority"] ?: ""
                )
                if (params.toList().any { it.isEmpty() }) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@post
                }
                val priority = Priority.valueOf(params.third)
                val task = Task(params.first, params.second, priority)
                try {
                    TaskRepository.add(task)
                    val tasks = TaskRepository.allTasks()
                    call.respond(
                        ThymeleafContent("all-tasks", mapOf("tasks" to tasks))
                    )
                } catch (exc: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }
        }
    }

}
