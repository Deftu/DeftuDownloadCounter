package xyz.deftu.ddc

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import org.apache.logging.log4j.LogManager

const val NAME = "@NAME@"
const val VERSION = "@VERSION@"
val logger = LogManager.getLogger(NAME)
val gson = GsonBuilder()
    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
    .setPrettyPrinting()
    .setLenient()
    .create()
