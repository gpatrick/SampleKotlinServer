package me.gregpatrick

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import spark.Filter
import spark.Request
import spark.Response
import spark.kotlin.Http
import spark.kotlin.ignite

data class Message(val from:String, var to:String, var message:String)

fun main(args: Array<String>) {
    val (http, json) = initialize()
    setupMessagesEndpoint(http, json)
}

fun initialize(): Pair<Http, ObjectMapper> {
    val mapper = initializeJSONMapper()
    val http: Http = initializeHTTPProvider()
    enableCORS(http)
    return Pair(http, mapper)
}

fun initializeJSONMapper(): ObjectMapper {
    return ObjectMapper().registerModule(KotlinModule())
}

fun initializeHTTPProvider(): Http {
    return ignite()
}

fun enableCORS(http:Http) {
    setupOptions(http)
    setupActionsBeforeServicingRequests(http)
}

fun setupOptions(http:Http) {
    http.options("/*") {
        setHeader("Access-Control-Request-Headers", request, response)
        setHeader("Access-Control-Request-Method", request, response)
        "OK"
    }
}

fun setHeader(header: String, request: Request, response: Response) {
    val accessControlRequest = request.headers(header)
    if (accessControlRequest != null) {
        response.header(header, accessControlRequest)
    }
}

fun setupActionsBeforeServicingRequests(http:Http) {
    http.before(Filter { _, response ->
        response.header("Access-Control-Allow-Origin", "*")
        response.type("application/json")
    })
}

fun setupMessagesEndpoint(http: Http, json: ObjectMapper) {
    http.get("/messages") {
        val result = arrayListOf<Message>(
                Message(from = "Alice", to = "Bob", message = "Hello"),
                Message(from = "John", to = "Doe", message = "World")
        )
        json.writeValueAsString(result)
    }
}