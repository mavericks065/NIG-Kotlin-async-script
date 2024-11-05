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
    .connectTimeout(Duration.ofSeconds(30))
    .build()

fun main(args: Array<String>) {
    val times = 3
    val httpCallInterval = Duration.of(500, ChronoUnit.MILLIS)

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

    val request = if(i % 201 == 0) {
        TimeUnit.SECONDS.sleep(TimeUnit.SECONDS.convert(delay))
        buildRequest(buildMalformedBodyWithInvalidImos())
    } else if (i % 213 == 0) {
        TimeUnit.SECONDS.sleep(TimeUnit.SECONDS.convert(delay))
        buildRequest(buildBodyWithImoOutOfRange())
    } else {
        buildRequest(buildBody())
    }
    return sendRequest(request, delay)
}

private fun buildBody(): String = "{\"query\":\"query Vessels {    vessels(        first: 5,        where: { filters:         [            { field: \\\"imo\\\", op: IN, values: [                        \\\"9401403\\\"                        \\\"9113135\\\"                        \\\"9079183\\\"                        \\\"9343338\\\"                        \\\"9313113\\\"                        \\\"9425502\\\"                        \\\"9792864\\\"                        \\\"9953406\\\"                        \\\"8025472\\\"                        \\\"7728716\\\"                        \\\"9436707\\\"                        \\\"8919130\\\"] },           { field: \\\"management.beneficialOwner.name\\\" op: LIKE values: [\\\"Z\\\"] }                        ]             }    ) {        nodes {            imo            associatedCompanies {                shipBuilder {                    name                    country                }            }            management {                registeredOwner {                    name                    country                }            }        }        pageInfo { endCursor hasNextPage }    }}\",\"variables\":{}}".trimIndent()
private fun buildMalformedBodyWithInvalidImos(): String = "{\"query\":\"query Vessels {    vessels(        first: 1,        where: { filters:         [            { field: \\\"imo\\\", op: IN, values: [ \\\"940140.3\\\"] },           { field: \\\"management.beneficialOwner.name\\\" op: LIKE values: [\\\"Z\\\"] }                        ]             }    ) {        nodes {            imo            associatedCompanies {                shipBuilder {                    name                    country                }            }            management {                registeredOwner {                    name                    country                }            }        }        pageInfo { endCursor hasNextPage }    }}\",\"variables\":{}}".trimIndent()
private fun buildBodyWithImoOutOfRange(): String = "{\"query\":\"query Vessels {    vessels(        first: 1,        where: { filters:         [            { field: \\\"imo\\\", op: IN, values: [ \\\"11119401403\\\" ] },           { field: \\\"management.beneficialOwner.name\\\" op: LIKE values: [\\\"Z\\\"] }                        ]             }    ) {        nodes {            imo            associatedCompanies {                shipBuilder {                    name                    country                }            }            management {                registeredOwner {                    name                    country                }            }        }        pageInfo { endCursor hasNextPage }    }}\",\"variables\":{}}".trimIndent()

private fun buildRequest(
    body: String
): HttpRequest = HttpRequest.newBuilder()
    .POST(HttpRequest.BodyPublishers.ofString(body))
    .uri(URI.create("https://api.kpler.marinetraffic.com/v2/vessels/graphql"))
    .setHeader(
        "Authorization",
        "Basic N3JSRExQSThhdkh6Z3dzbEFRSk1uTDhvNFJ0Vmp4bmU6cFhadzVPMVVzaUx1Rm1heHFjcXl1MEt5VTBSa0xCVmxCN2ZmUlVuYUJJQnF0UHItaVViN3NtR3Bpc0EtbnI3SA="
    )
    .setHeader("Content-Type", "application/json; charset=utf-8")
    .setHeader("Accept-Encoding", "gzip, deflate, br")
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

