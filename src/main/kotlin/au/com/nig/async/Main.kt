package au.com.nig.async

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

private val httpClient: HttpClient = HttpClient.newBuilder()
    .connectTimeout(Duration.ofSeconds(10))
    .build()

fun main(args: Array<String>) {
    val times = 1
    val httpCallInterval = Duration.of(2, ChronoUnit.SECONDS)

    (0 until times).map { i ->
        println(i)
        doSomething(httpCallInterval, i)
    }.toTypedArray()
        .let { CompletableFuture.allOf(*it) }
        .get()
}

private fun doSomething(
    delay: Duration,
    i: Int
): CompletableFuture<Unit> {

    val request = buildRequest(buildBody(i))
    return sendRequest(request, delay)
}

private fun buildBody(i: Int): String = TODO()

private fun buildRequest(
    body: String
): HttpRequest = HttpRequest.newBuilder()
    .POST(HttpRequest.BodyPublishers.ofString(body))
    .uri(URI.create(TODO()))
    .setHeader("Authorization", "Some Bearer ?")
    .setHeader("Content-Type", "application/json")
    .build()

private fun sendRequest(httpRequest: HttpRequest, delay: Duration): CompletableFuture<Unit> {

    TimeUnit.SECONDS.sleep(TimeUnit.SECONDS.convert(delay))
    val startTime = LocalDateTime.now()

    return httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString())
        .thenApply { response: HttpResponse<String> ->
            val endTime = LocalDateTime.now()
            val requestProcessingTime = Duration.between(startTime, endTime)
            println("$requestProcessingTime ${response.statusCode()} ${response.body()}")
        }
}

