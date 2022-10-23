package xyz.deftu.ddc.counters.curse

import com.google.gson.JsonParser
import kotlinx.coroutines.delay
import okhttp3.OkHttpClient
import okhttp3.Request
import xyz.deftu.ddc.*

class CurseClient {
    private val httpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor {
                it.proceed(it.request().newBuilder()
                    .addHeader("User-Agent", "$NAME/$VERSION")
                    .addHeader("Accept", "application/json")
                    .addHeader("User-Agent", Config.INSTANCE.curse.apiKey)
                    .build())
            }.build()
    }
    private var requestCount = 0
    private var lastRequest = 0L

    suspend fun fetchMod(id: Long): CurseCounter.CurseProject? {
        if (requestCount >= 2 && System.currentTimeMillis() - lastRequest < 2500) {
            requestCount = 0
            delay(2500)
        }
        requestCount++
        lastRequest = System.currentTimeMillis()

        val response = httpClient.newCall(
            Request.Builder()
                .get()
                .url("https://api.curseforge.com/v1/mods/$id")
                .build()
        ).execute()
        val body = response.body?.string() ?: return null

        val element = JsonParser.parseString(body)
        if (!element.isJsonObject)
            return null

        return gson.fromJson(element.asJsonObject.getAsJsonObject("data"), CurseCounter.CurseProject::class.java)
    }
}
