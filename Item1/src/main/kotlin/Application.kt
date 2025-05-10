package com.example

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable


@Serializable
data class Persona(var id: Int, var nombre: String, var region: Region, var sexo: String)

@Serializable
data class Region(val id: Int, val region: String, val comuna: String)


fun main() {
    embeddedServer(Netty, port = 9090, host = "0.0.0.0", module = Application::module).start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }

    val personas = mutableListOf(
        Persona(1, "Giancarlos Ricci", Region(1, "Metropolitana", "Puente_Alto"), "hombre"),
        Persona(2, "Luis Ricci", Region(2, "Metropolitana", "Puente_Alto"), "hombre"),
        Persona(3, "Catalina", Region(3, "Valparaiso", "Quilicura"), "mujer"),
        Persona(4, "Maria Lopez", Region(4, "Valparaiso", "La_Granja"), "mujer"),
        Persona(5, "Javier Gomez", Region(5, "Antofagasta", "Providencia"), "hombre")
    )

    routing {
        get("/personas") {
            call.respond(personas)
        }
        get("/personas/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            val persona = personas.find { it.id == id }
            if (persona != null) {
                call.respond(persona)
            } else {
                call.respondText("Persona no encontrada", status = io.ktor.http.HttpStatusCode.NotFound)
            }
        }
        post("/personas") {
            val persona = call.receive<Persona>()
            personas.add(persona)
            call.respondText("Persona creada", status = io.ktor.http.HttpStatusCode.Created)
        }
        put("/personas/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            val persona = personas.find { it.id == id }
            if (persona != null) {
                val updatedPersona = call.receive<Persona>()
                persona.nombre = updatedPersona.nombre
                persona.region = updatedPersona.region
                persona.sexo = updatedPersona.sexo
                call.respondText("Persona actualizada", status = io.ktor.http.HttpStatusCode.OK)
            } else {
                call.respondText("Persona no encontrada", status = io.ktor.http.HttpStatusCode.NotFound)
            }
        }
        delete("/personas/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            val persona = personas.find { it.id == id }
            if (persona != null) {
                personas.remove(persona)
                call.respondText("Persona eliminada", status = io.ktor.http.HttpStatusCode.NoContent)
            } else {
                call.respondText("Persona no encontrada", status = io.ktor.http.HttpStatusCode.NotFound)
            }
        }
    }
}