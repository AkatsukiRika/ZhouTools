package api

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
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
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import model.LoginRequest
import model.Memo
import model.MemoSyncRequest
import model.TimeCardRecords
import model.TimeCardSyncRequest

class NetworkApi {
    companion object {
        const val KEY_CODE = "code"
        const val KEY_MESSAGE = "message"
        const val KEY_DATA = "data"
        const val KEY_TOKEN = "token"
        const val KEY_USERNAME = "username"
        const val KEY_MEMOS = "memos"
        const val KEY_AUTH = "Authorization"
        const val CODE_SUCCESS = 0
    }

    @OptIn(ExperimentalSerializationApi::class)
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                encodeDefaults = true
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
        try {
            val bodyText = httpClient.post("https://www.tang-ping.top/api/login") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.bodyAsText()
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
        return try {
            val bodyText = httpClient.post("https://www.tang-ping.top/api/timeCard/sync") {
                contentType(ContentType.Application.Json)
                header(KEY_AUTH, token)
                setBody(request)
            }.bodyAsText()
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

    suspend fun getServerTimeCards(token: String, username: String): TimeCardRecords? {
        return try {
            val bodyText = httpClient.get("https://www.tang-ping.top/api/timeCard/get") {
                header(KEY_AUTH, token)
                parameter(KEY_USERNAME, username)
            }.bodyAsText()
            val jsonObject = Json.parseToJsonElement(bodyText) as JsonObject
            val code = jsonObject[KEY_CODE]?.jsonPrimitive?.intOrNull
            if (code == CODE_SUCCESS) {
                val json = Json { ignoreUnknownKeys = true }
                val data = jsonObject[KEY_DATA]?.jsonObject
                if (data != null) {
                    return json.decodeFromJsonElement<TimeCardRecords>(data)
                }
                null
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * @return isSuccess to errorMessage
     */
    suspend fun syncMemo(token: String, request: MemoSyncRequest): Pair<Boolean, String?> {
        return try {
            val bodyText = httpClient.post("https://www.tang-ping.top/api/memo/sync") {
                contentType(ContentType.Application.Json)
                header(KEY_AUTH, token)
                setBody(request)
            }.bodyAsText()
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

    suspend fun getServerMemos(token: String, username: String): List<Memo>? {
        return try {
            val bodyText = httpClient.get("https://www.tang-ping.top/api/memo/get") {
                header(KEY_AUTH, token)
                parameter(KEY_USERNAME, username)
            }.bodyAsText()
            val jsonObject = Json.parseToJsonElement(bodyText) as JsonObject
            val code = jsonObject[KEY_CODE]?.jsonPrimitive?.intOrNull
            if (code == CODE_SUCCESS) {
                val json = Json { ignoreUnknownKeys = true }
                val data = jsonObject[KEY_DATA]?.jsonObject
                if (data != null) {
                    val resultList = mutableListOf<Memo>()
                    val memosJsonArray = data[KEY_MEMOS]?.jsonArray
                    memosJsonArray?.forEach {
                        resultList.add(json.decodeFromJsonElement(it))
                    }
                    return resultList
                }
                null
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}