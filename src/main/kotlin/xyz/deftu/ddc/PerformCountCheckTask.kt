package xyz.deftu.ddc

import dev.kord.common.entity.*
import dev.kord.core.entity.channel.MessageChannel
import kotlinx.coroutines.runBlocking
import java.util.TimerTask

class PerformCountCheckTask : TimerTask() {
    override fun run() {
        runBlocking {
            val channel = kord.getChannel(Snowflake(Config.INSTANCE.channelId)) ?: run {
                shutdown(InvalidConfigException("Invalid channel ID provided!"))
                return@runBlocking
            }

            if (channel.type != ChannelType.GuildText && channel.type != ChannelType.GuildNews) {
                shutdown(InvalidConfigException("Invalid channel ID provided!"))
                return@runBlocking
            }

            val messageChannel = channel as MessageChannel
            counters.forEach { counter ->
                counter.handle(messageChannel)
            }
        }
    }
}
