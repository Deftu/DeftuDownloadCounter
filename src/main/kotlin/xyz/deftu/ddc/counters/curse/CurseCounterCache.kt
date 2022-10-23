package xyz.deftu.ddc.counters.curse

import com.google.gson.JsonArray
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xyz.deftu.ddc.counters.modrinth.ModrinthCounterCache
import xyz.deftu.ddc.gson
import xyz.deftu.ddc.shutdown
import java.io.File
import java.io.FileNotFoundException
import java.io.FileWriter
import java.io.StringReader

internal object CurseCounterCache {
    private val file = File("curse.json")
    private val cache = mutableListOf<CurseCache>()

    suspend fun read() {
        if (!file.exists()) {
            if (!withContext(Dispatchers.IO) {
                    file.createNewFile()
                }) {
                shutdown(FileNotFoundException("Couldn't create Curse cache file."))
                return
            }

            file.writeText(gson.toJson(JsonArray()))
        }

        cache.clear()
        JsonReader(StringReader(file.readText())).use { reader ->
            val token = reader.peek()
            if (token != JsonToken.BEGIN_ARRAY)
                return

            reader.beginArray()

            while (reader.hasNext()) {
                val token = reader.peek()
                if (token != JsonToken.BEGIN_OBJECT)
                    return

                reader.beginObject()

                var id = -1L
                var downloads = -1L

                var currentName = ""
                while (reader.hasNext()) {
                    val token = reader.peek()
                    if (token == JsonToken.NAME) {
                        currentName = reader.nextName()
                        continue
                    }

                    when (currentName) {
                        "id" -> {
                            if (token != JsonToken.NUMBER)
                                return
                            id = reader.nextLong()
                        }
                        "downloads" -> {
                            if (token != JsonToken.NUMBER)
                                return
                            downloads = reader.nextLong()
                        }
                    }
                }

                reader.endObject()

                if (id != -1L && downloads != -1L) {
                    cache.add(CurseCache(id, downloads))
                }
            }

            reader.endArray()
        }
    }

    suspend fun write(projects: List<CurseCounter.CurseProject>) {
        withContext(Dispatchers.IO) {
            JsonWriter(FileWriter(file)).use { writer ->
                writer.beginArray()

                for (project in projects) {
                    writer.beginObject()
                    writer.name("id").value(project.id)
                    writer.name("downloads").value(project.downloadCount)
                    writer.endObject()
                }

                writer.endArray()
            }
        }
    }

    fun getCache() = cache.toList()
}

internal data class CurseCache(
    val id: Long,
    val downloads: Long
)
