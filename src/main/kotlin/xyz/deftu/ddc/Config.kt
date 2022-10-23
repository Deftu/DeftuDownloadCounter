package xyz.deftu.ddc

import com.google.gson.JsonObject
import java.io.File

data class Config(
    val token: String,
    val channelId: Long,
    val modrinth: ModrinthConfig,
    val curse: CurseConfig
) {
    companion object {
        val INSTANCE by lazy {
            gson.fromJson(File("config.json").apply {
                if (!exists()) {
                    createNewFile()
                    writeText(gson.toJson(JsonObject()))
                }
            }.readText(), Config::class.java)
        }
    }
}

data class ModrinthConfig(
    val toggle: Boolean,
    val userId: String
)

data class CurseConfig(
    val toggle: Boolean,
    val apiKey: String,
    val projectIds: List<Long>
)

class InvalidConfigException(
    message: String
) : RuntimeException(message)
