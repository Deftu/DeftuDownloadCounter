package xyz.deftu.ddc.counters.modrinth

import com.google.gson.JsonParser
import dev.kord.common.Color
import dev.kord.common.entity.ChannelType
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.entity.channel.TextChannel
import dev.kord.rest.builder.message.create.embed
import kotlinx.datetime.Clock
import okhttp3.OkHttpClient
import okhttp3.Request
import xyz.deftu.ddc.Config
import xyz.deftu.ddc.*
import xyz.deftu.ddc.counters.BaseModCounter
import xyz.deftu.ddc.utils.calcGoal
import xyz.deftu.ddc.utils.calcMilestone

object ModrinthCounter : BaseModCounter {
    private val color = 0x1bd96a
    private val httpClient = OkHttpClient.Builder()
        .addInterceptor {
            it.proceed(it.request().newBuilder()
                .addHeader("User-Agent", "${NAME}/${VERSION}")
                .build())
        }.build()
    private val toggle by lazy {
        Config.INSTANCE.modrinth.toggle
    }
    private val userId by lazy {
        Config.INSTANCE.modrinth.userId
    }

    override suspend fun handle(channel: MessageChannel) {
        if (!toggle)
            return

        val response = httpClient.newCall(
            Request.Builder()
                .get()
                .url("https://api.modrinth.com/v2/user/$userId/projects")
                .build()
        ).execute()
        val body = response.body?.string() ?: return

        val element = JsonParser.parseString(body)
        if (!element.isJsonArray)
            return

        ModrinthCounterCache.read()
        val projects = element.asJsonArray.map {
            gson.fromJson(it, ModrinthProject::class.java)
        }.filter {
            it.projectType == "mod" && it.status == "approved" && it.downloads > 100
        }

        ModrinthCounterCache.write(projects)
        projects.forEach { project ->
            handleProject(project, channel)
        }
    }

    private suspend fun handleProject(project: ModrinthProject, channel: MessageChannel) {
        val cached = ModrinthCounterCache.getCache().firstOrNull {
            it.id == project.id
        } ?: return
        val cachedGoal = cached.downloads.calcGoal()
        val goal = project.downloads.calcGoal()
        if (goal > cachedGoal || project.downloads.toString().length > cached.downloads.toString().length) {
            val message = channel.createMessage {
                embed {
                    title = "${project.title} reached a new milestone!"
                    url = "https://modrinth.com/mod/${project.id}"
                    color = Color(this@ModrinthCounter.color)
                    timestamp = Clock.System.now()

                    footer {
                        icon = project.iconUrl
                        text = "Available on Modrinth!"
                    }

                    field {
                        name = "Milestone"
                        value = goal.calcMilestone(project.downloads).toString()
                        inline = true
                    }

                    field {
                        name = "Downloads"
                        value = project.downloads.toString()
                        inline = true
                    }

                    field {
                        name = "Followers"
                        value = project.followers.toString()
                        inline = true
                    }
                }
            }

            if (channel.type == ChannelType.GuildNews) {
                message.publish()
            }
        }
    }

    internal data class ModrinthProject(
        val id: String,
        val projectType: String,
        val title: String,
        val status: String,
        val downloads: Long,
        val followers: Long,
        val iconUrl: String
    )
}
