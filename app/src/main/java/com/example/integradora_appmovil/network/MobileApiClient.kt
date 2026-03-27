package com.example.integradora_appmovil.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class ApiException(
    val statusCode: Int,
    override val message: String
) : Exception(message)

object MobileApiClient {
    // Emulador Android -> backend local de la laptop.
    const val BASE_URL = "http://10.0.2.2:8080"

    suspend fun postJson(path: String, payload: JSONObject, token: String? = null): JSONObject =
        withContext(Dispatchers.IO) {
            val connection = openConnection(path, "POST", token)
            writeJsonBody(connection, payload)
            val responseBody = readResponseBody(connection)
            if (connection.responseCode !in 200..299) {
                throw ApiException(connection.responseCode, extractErrorMessage(responseBody))
            }
            JSONObject(responseBody.ifBlank { "{}" })
        }

    suspend fun getJsonArray(path: String, token: String? = null): JSONArray =
        withContext(Dispatchers.IO) {
            val connection = openConnection(path, "GET", token)
            val responseBody = readResponseBody(connection)
            if (connection.responseCode !in 200..299) {
                throw ApiException(connection.responseCode, extractErrorMessage(responseBody))
            }
            JSONArray(responseBody.ifBlank { "[]" })
        }

    private fun openConnection(path: String, method: String, token: String?): HttpURLConnection {
        val url = URL("$BASE_URL$path")
        return (url.openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 15000
            readTimeout = 15000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            token?.takeIf { it.isNotBlank() }?.let {
                setRequestProperty("Authorization", "Bearer $it")
            }
            doInput = true
            if (method != "GET") {
                doOutput = true
            }
        }
    }

    private fun writeJsonBody(connection: HttpURLConnection, payload: JSONObject) {
        OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
            writer.write(payload.toString())
            writer.flush()
        }
    }

    private fun readResponseBody(connection: HttpURLConnection): String {
        val stream = if (connection.responseCode in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream ?: connection.inputStream
        }

        return stream?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).readText()
        }.orEmpty()
    }

    private fun extractErrorMessage(responseBody: String): String {
        if (responseBody.isBlank()) {
            return "No se pudo completar la solicitud"
        }

        return runCatching {
            val body = JSONObject(responseBody)
            body.optString("error")
                .ifBlank { body.optString("message") }
                .ifBlank { "No se pudo completar la solicitud" }
        }.getOrDefault("No se pudo completar la solicitud")
    }
}
