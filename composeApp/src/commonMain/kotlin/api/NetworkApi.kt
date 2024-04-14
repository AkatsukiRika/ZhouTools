package api

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import model.LoginRequest

class NetworkApi {
    companion object {
        const val KEY_CODE = "code"
        const val KEY_MESSAGE = "message"
        const val KEY_DATA = "data"
        const val KEY_TOKEN = "token"
        const val CODE_SUCCESS = 0
    }

    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                useAlternativeNames = false
            })
        }
    }

    /**
     * @return First in pair indicates whether login is performed successfully.
     * Second in pair is error message when login failed; JWT token when login succeeded.
     */
    suspend fun login(request: LoginRequest): Pair<Boolean, String?> {
        val bodyText = httpClient.post("https://www.tang-ping.top/api/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.bodyAsText()
        try {
            val jsonObject = Json.parseToJsonElement(bodyText) as JsonObject
            val code = jsonObject[KEY_CODE]?.jsonPrimitive?.intOrNull
            if (code == CODE_SUCCESS) {
                val data = jsonObject[KEY_DATA]?.jsonObject
                val token = data?.get(KEY_TOKEN)?.jsonPrimitive?.content
                return true to token
            } else {
                val message = jsonObject[KEY_MESSAGE]?.jsonPrimitive?.content
                return false to message
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false to null
        }
    }
}