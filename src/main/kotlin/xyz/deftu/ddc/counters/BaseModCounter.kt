package xyz.deftu.ddc.counters

import dev.kord.core.entity.channel.MessageChannel

interface BaseModCounter {
    suspend fun handle(channel: MessageChannel)
}
