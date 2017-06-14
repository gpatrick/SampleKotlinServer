package me.gregpatrick

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import spark.Filter
import spark.Request
import spark.Response
import spark.kotlin.Http
import spark.kotlin.ignite

/**
 * A class representing a message in the system.
 *
 * @param from the name of the person who sent the message
 * @param to the name of the person who received the message
 * @param message the contents of the message
 */
data class Message(val from:String, var to:String, var message:String)


/**
 * Entry point to the application. It initializes the web framework (Spark), and creates a Jackson object mapper to
 * support serving JSON data.
 */
fun main(args: Array<String>) {
    val (http, json) = initialize()
    setupMessagesEndpoint(http, json)
}


/**
 * Initialize the JSON mapper and the Spark web framework. It also configures the application to accept CORS request.
 *
 * @returns Pair<Http, ObjectMapper> a tuple containing the configured Spark http object and a JSON mapper
 */
fun initialize(): Pair<Http, ObjectMapper> {
    val mapper = initializeJSONMapper()
    val http: Http = initializeHTTPProvider()
    enableCORS(http)
    return Pair(http, mapper)
}


/**
 * Initialize the Jackson object mapper.
 *
 * @returns ObjectMapper the JSON object mapper
 */
fun initializeJSONMapper(): ObjectMapper {
    return ObjectMapper().registerModule(KotlinModule())
}


/**
 * Initialize the Spark framework.
 *
 * @return Http the Spark Http object used to service web requests
 */
fun initializeHTTPProvider(): Http {
    return ignite()
}


/**
 * Enable cross-origin access. Sets up the /options endpoint to respond with the appropriate headers, and ensures that
 * requests from all callers will be serviced.
 *
 * @param http the Spark Http object used to service web requests
 */
fun enableCORS(http:Http) {
    setupOptions(http)
    setupActionsBeforeServicingRequests(http)
}


/**
 * Return Access-Control-Request-Headers and Access-Control-Request-Method that the caller sends
 *
 * @param http the Spark Http object used to service web requests
 */
fun setupOptions(http:Http) {
    http.options("/*") {
        setHeader("Access-Control-Request-Headers", request, response)
        setHeader("Access-Control-Request-Method", request, response)
        "OK"
    }
}


/**
 * Sets a header in the response if it has been set by the caller
 *
 * @param header the header that the caller is requesting
 * @param request an object representing the web request
 * @param response an object representing the web response
 */
fun setHeader(header: String, request: Request, response: Response) {
    val accessControlRequest = request.headers(header)
    if (accessControlRequest != null) {
        response.header(header, accessControlRequest)
    }
}


/**
 * Ensures that all callers can be serviced by setting Access-Control-Allow-Origin to all (*)
 *
 * @param http the Spark Http object used to service web requests
 */
fun setupActionsBeforeServicingRequests(http:Http) {
    http.before(Filter { _, response ->
        response.header("Access-Control-Allow-Origin", "*")
        response.type("application/json")
    })
}


/**
 * Creates a static list of messages to send to the caller. A great exercise is to expand this call to dynamically
 * manage this list via additional REST operations
 *
 * @param http the Spark Http object used to service web requests
 * @param json the JSON object mapper that serializes the result
 */
fun setupMessagesEndpoint(http: Http, json: ObjectMapper) {
    http.get("/messages") {
        val result = arrayListOf<Message>(
                Message(from = "Alice", to = "Bob", message = "Hello"),
                Message(from = "John", to = "Doe", message = "World")
        )
        json.writeValueAsString(result)
    }
}