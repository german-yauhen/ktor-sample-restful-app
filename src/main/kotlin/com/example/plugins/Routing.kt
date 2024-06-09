package com.example.plugins

import com.example.model.Priority
import com.example.model.TaskRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
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
                        template = "all-tasks",
                        model = mapOf("tasks" to listOf(task))
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
                        template = "all-tasks",
                        model = mapOf("tasks" to tasks)
                    )
                )
            }

//            post("/creation") {
//                val task = call.receive<Task>()
//                TaskRepository.add(task)
//                call.respondRedirect("/tasks")
//            }
        }
    }

}
