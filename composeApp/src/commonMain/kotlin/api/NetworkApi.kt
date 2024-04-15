package api

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import model.LoginRequest
import model.TimeCardSyncRequest

class NetworkApi {
    companion object {
        const val KEY_CODE = "code"
        const val KEY_MESSAGE = "message"
        const val KEY_DATA = "data"
        const val KEY_TOKEN = "token"
        const val KEY_AUTH = "Authorization"
        const val CODE_SUCCESS = 0
    }

    @OptIn(ExperimentalSerializationApi::class)
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                namingStrategy = JsonNamingStrategy.SnakeCase
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

    /**
     * @return isSuccess to errorMessage
     */
    suspend fun sync(token: String, request: TimeCardSyncRequest): Pair<Boolean, String?> {
        val bodyText = httpClient.post("https://www.tang-ping.top/api/timeCard/sync") {
            contentType(ContentType.Application.Json)
            header(KEY_AUTH, token)
            setBody(request)
        }.bodyAsText()
        return try {
            val jsonObject = Json.parseToJsonElement(bodyText) as JsonObject
            val code = jsonObject[KEY_CODE]?.jsonPrimitive?.intOrNull
            if (code == CODE_SUCCESS) {
                true to null
            } else {
                val message = jsonObject[KEY_MESSAGE]?.jsonPrimitive?.content
                false to message
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false to null
        }
    }
}