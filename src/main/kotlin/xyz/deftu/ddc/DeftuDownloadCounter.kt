package xyz.deftu.ddc

import dev.kord.common.entity.PresenceStatus
import dev.kord.core.*
import dev.kord.core.event.gateway.ReadyEvent
import kotlinx.coroutines.delay
import xyz.deftu.ddc.counters.BaseModCounter
import xyz.deftu.ddc.counters.curse.CurseCounter
import xyz.deftu.ddc.counters.modrinth.ModrinthCounter
import java.util.Timer

lateinit var kord: Kord
var counters = listOf<BaseModCounter>()
    private set

suspend fun main() {
    logger.info("Starting!")
    kord = Kord(Config.INSTANCE.token)

    registerCounter(ModrinthCounter)
    registerCounter(CurseCounter)

    kord.on<ReadyEvent> {
        logger.info("Ready!")
        val timer = Timer()
        timer.scheduleAtFixedRate(PerformCountCheckTask(), 0,900_000) // Every 15 minutes.
    }

    kord.login {
        presence {
            status = PresenceStatus.DoNotDisturb
            watching("downlaod counts!")
        }
    }
}

suspend fun shutdown(e: Exception? = null) {
    logger.warn("Shutting down...")
    e?.printStackTrace()
    kord.editPresence {
        playing("shutting down..." + (e?.let { "(${e::class.java.simpleName})" } ?: ""))
    }

    delay(10_000)
    kord.shutdown()
    e?.let {
        throw it
    }
}

private fun registerCounter(counter: BaseModCounter) {
    val list = mutableListOf<BaseModCounter>()
    list.addAll(counters)
    list.add(counter)
    counters = list
}
