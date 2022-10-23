package xyz.deftu.ddc.counters.curse

import dev.kord.common.Color
import dev.kord.common.entity.ChannelType
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.rest.builder.message.create.embed
import kotlinx.datetime.Clock
import xyz.deftu.ddc.Config
import xyz.deftu.ddc.counters.BaseModCounter
import xyz.deftu.ddc.logger
import xyz.deftu.ddc.utils.calcGoal
import xyz.deftu.ddc.utils.calcMilestone

object CurseCounter : BaseModCounter {
    private val color = 0xF86434
    private var cancel = false

    private val toggle by lazy {
        Config.INSTANCE.curse.toggle
    }
    private val apiKey by lazy {
        Config.INSTANCE.curse.apiKey
    }
    val projectIds by lazy {
        Config.INSTANCE.curse.projectIds
    }
    private val client by lazy {
        CurseClient()
    }

    override suspend fun handle(channel: MessageChannel) {
        if (!toggle || cancel)
            return

        if (apiKey.isBlank()) {
            cancel = true
            logger.error("Curse API key is not present!")
            return
        }

        CurseCounterCache.read()
        var projects = mutableListOf<CurseProject>()
        for (projectId in projectIds) client.fetchMod(projectId)?.let { projects.add(it) }
        projects = projects.filter {
            it.downloadCount > 100
        }.toMutableList()

        CurseCounterCache.write(projects)
        projects.forEach { project ->
            handleProject(project, channel)
        }
    }

    private suspend fun handleProject(project: CurseProject, channel: MessageChannel) {
        val cached = CurseCounterCache.getCache().firstOrNull {
            it.id == project.id
        } ?: return
        val cachedGoal = cached.downloads.calcGoal()
        val goal = project.downloadCount.calcGoal()
        if (goal > cachedGoal || project.downloadCount.toString().length > cached.downloads.toString().length) {
            val message = channel.createMessage {
                embed {
                    title = "${project.name} reached a new milestone!"
                    url = "https://curseforge.com/minecraft/mc-mods/${project.slug}"
                    color = Color(this@CurseCounter.color)
                    timestamp = Clock.System.now()

                    footer {
                        icon = project.logo.url
                        text = "Available on CurseForge!"
                    }

                    field {
                        name = "Milestone"
                        value = goal.calcMilestone(project.downloadCount).toString()
                        inline = true
                    }

                    field {
                        name = "Downloads"
                        value = project.downloadCount.toString()
                        inline = true
                    }
                }
            }

            if (channel.type == ChannelType.GuildNews) {
                message.publish()
            }
        }
    }

    data class CurseProject(
        val id: Long,
        val name: String,
        val slug: String,
        val downloadCount: Long,
        val logo: CurseProjectLogo
    ) {
        data class CurseProjectLogo(
            val url: String
        )
    }
}
