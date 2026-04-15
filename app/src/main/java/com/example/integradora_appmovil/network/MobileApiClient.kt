package com.example.integradora_appmovil.network

import com.example.integradora_appmovil.BuildConfig
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

data class BinaryResponse(
    val bytes: ByteArray,
    val contentType: String,
    val fileName: String
)

object MobileApiClient {
    val BASE_URL: String = BuildConfig.MOBILE_API_BASE_URL.trimEnd('/')

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

    suspend fun getJsonObject(path: String, token: String? = null): JSONObject =
        withContext(Dispatchers.IO) {
            val connection = openConnection(path, "GET", token)
            val responseBody = readResponseBody(connection)
            if (connection.responseCode !in 200..299) {
                throw ApiException(connection.responseCode, extractErrorMessage(responseBody))
            }
            JSONObject(responseBody.ifBlank { "{}" })
        }

    suspend fun putJson(path: String, payload: JSONObject, token: String? = null): JSONObject =
        withContext(Dispatchers.IO) {
            val connection = openConnection(path, "PUT", token)
            writeJsonBody(connection, payload)
            val responseBody = readResponseBody(connection)
            if (connection.responseCode !in 200..299) {
                throw ApiException(connection.responseCode, extractErrorMessage(responseBody))
            }
            JSONObject(responseBody.ifBlank { "{}" })
        }

    suspend fun postMultipart(
        path: String,
        formFields: Map<String, String>,
        fileFieldName: String? = null,
        fileName: String? = null,
        fileBytes: ByteArray? = null,
        fileContentType: String? = null,
        token: String? = null
    ): JSONObject = withContext(Dispatchers.IO) {
        val boundary = "----PermiAppBoundary${System.currentTimeMillis()}"
        val connection = openConnection(path, "POST", token, "multipart/form-data; boundary=$boundary")
        writeMultipartBody(
            connection = connection,
            boundary = boundary,
            formFields = formFields,
            fileFieldName = fileFieldName,
            fileName = fileName,
            fileBytes = fileBytes,
            fileContentType = fileContentType
        )
        val responseBody = readResponseBody(connection)
        if (connection.responseCode !in 200..299) {
            throw ApiException(connection.responseCode, extractErrorMessage(responseBody))
        }
        JSONObject(responseBody.ifBlank { "{}" })
    }

    suspend fun getBinary(path: String, token: String? = null): BinaryResponse =
        withContext(Dispatchers.IO) {
            val connection = openConnection(path, "GET", token)

            if (connection.responseCode !in 200..299) {
                val responseBody = readResponseBody(connection)
                throw ApiException(connection.responseCode, extractErrorMessage(responseBody))
            }

            val bytes = connection.inputStream.use(InputStream::readBytes)
            val contentDisposition = connection.getHeaderField("content-disposition").orEmpty()
            val filenameStarMatch = Regex("filename\\*=UTF-8''([^;]+)", RegexOption.IGNORE_CASE)
                .find(contentDisposition)
                ?.groupValues
                ?.getOrNull(1)
            val filenameMatch = Regex("filename=\"?([^\";]+)\"?", RegexOption.IGNORE_CASE)
                .find(contentDisposition)
                ?.groupValues
                ?.getOrNull(1)
            val rawFilename = filenameStarMatch ?: filenameMatch ?: "comprobante.pdf"

            BinaryResponse(
                bytes = bytes,
                contentType = connection.contentType ?: "application/pdf",
                fileName = decodeFilename(rawFilename)
            )
        }

    private fun openConnection(
        path: String,
        method: String,
        token: String?,
        contentType: String = "application/json; charset=UTF-8"
    ): HttpURLConnection {
        val url = URL("$BASE_URL$path")
        return (url.openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 15000
            readTimeout = 15000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Content-Type", contentType)
            token?.takeIf { it.isNotBlank() }?.let {
                setRequestProperty("Authorization", "Bearer $it")
            }
            doInput = true
            if (method != "GET") {
                doOutput = true
            }
        }
    }

    private fun writeMultipartBody(
        connection: HttpURLConnection,
        boundary: String,
        formFields: Map<String, String>,
        fileFieldName: String?,
        fileName: String?,
        fileBytes: ByteArray?,
        fileContentType: String?
    ) {
        val lineBreak = "\r\n"

        connection.outputStream.use { output ->
            formFields.forEach { (name, value) ->
                output.write("--$boundary$lineBreak".toByteArray(Charsets.UTF_8))
                output.write("Content-Disposition: form-data; name=\"$name\"$lineBreak$lineBreak".toByteArray(Charsets.UTF_8))
                output.write(value.toByteArray(Charsets.UTF_8))
                output.write(lineBreak.toByteArray(Charsets.UTF_8))
            }

            if (fileFieldName != null && fileName != null && fileBytes != null) {
                output.write("--$boundary$lineBreak".toByteArray(Charsets.UTF_8))
                output.write(
                    "Content-Disposition: form-data; name=\"$fileFieldName\"; filename=\"$fileName\"$lineBreak"
                        .toByteArray(Charsets.UTF_8)
                )
                output.write("Content-Type: ${fileContentType ?: "application/octet-stream"}$lineBreak$lineBreak".toByteArray(Charsets.UTF_8))
                output.write(fileBytes)
                output.write(lineBreak.toByteArray(Charsets.UTF_8))
            }

            output.write("--$boundary--$lineBreak".toByteArray(Charsets.UTF_8))
            output.flush()
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

    private fun decodeFilename(rawFilename: String): String =
        runCatching { java.net.URLDecoder.decode(rawFilename, Charsets.UTF_8.name()) }
            .getOrDefault(rawFilename)
}
