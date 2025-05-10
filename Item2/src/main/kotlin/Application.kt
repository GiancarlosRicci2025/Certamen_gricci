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
                call.respondText("persona no encontrada", status = io.ktor.http.HttpStatusCode.NotFound)
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
                call.respondText("persona actualizada", status = io.ktor.http.HttpStatusCode.OK)
            } else {
                call.respondText("Persona no encontrada", status = io.ktor.http.HttpStatusCode.NotFound)
            }
        }

        delete("/personas/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            val persona = personas.find { it.id == id }
            if (persona != null) {
                personas.remove(persona)
                call.respondText("persona eliminada", status = io.ktor.http.HttpStatusCode.NoContent)
            } else {
                call.respondText("Persona no encontrada", status = io.ktor.http.HttpStatusCode.NotFound)
            }
        }

        get("/personas/regiones/{regionId}") {
            val regionId = call.parameters["regionId"]?.toIntOrNull()
            val personasFiltradas = personas.filter { it.region.id == regionId }
            if (personasFiltradas.isNotEmpty()) {
                call.respond(personasFiltradas)
            } else {
                call.respondText("No se encontraron personas con esa región", status = io.ktor.http.HttpStatusCode.NotFound)
            }
        }

        get("/personas/sexo/{sexo}") {
            val sexo = call.parameters["sexo"]
            val personasFiltradas = personas.filter { it.sexo.equals(sexo, ignoreCase = true) }
            if (personasFiltradas.isNotEmpty()) {
                call.respond(personasFiltradas)
            } else {
                call.respondText("No se encontraron personas con ese sexo", status = io.ktor.http.HttpStatusCode.NotFound)
            }
        }

        get("/personas/nombre/{nombre}") {
            val nombre = call.parameters["nombre"]
            val personasFiltradas = personas.filter { it.nombre.contains(nombre ?: "", ignoreCase = true) }
            if (personasFiltradas.isNotEmpty()) {
                call.respond(personasFiltradas)
            } else {
                call.respondText("no hay personas con ese nombre", status = io.ktor.http.HttpStatusCode.NotFound)
            }
        }
        get("/personas/comuna/{comuna}") {
            val comuna = call.parameters["comuna"]
            val personasFiltradas = personas.filter { it.region.comuna.equals(comuna, ignoreCase = true) }

            if (personasFiltradas.isNotEmpty()) {
                call.respond(personasFiltradas)
            } else {
                call.respondText("no hay personas con esa comuna", status = io.ktor.http.HttpStatusCode.NotFound)
            }
        }


        get("/personas/filtrar") {
            val nombre = call.request.queryParameters["nombre"]
            val regionId = call.request.queryParameters["regionId"]?.toIntOrNull()
            val sexo = call.request.queryParameters["sexo"]

            val personasFiltradas = personas.filter {
                (nombre == null || it.nombre.contains(nombre, ignoreCase = true)) &&
                        (regionId == null || it.region.id == regionId) &&
                        (sexo == null || it.sexo.equals(sexo, ignoreCase = true))
            }

            if (personasFiltradas.isNotEmpty()) {
                call.respond(personasFiltradas)
            } else {
                call.respondText("No se encontraron personas con esos fitros", status = io.ktor.http.HttpStatusCode.NotFound)
            }
        }
    }
}