package io.github.absdf15.qbot.core.module.common

import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import kotlin.reflect.KClass

data class QBotData (
    val description: JvmPluginDescription,
    val commandPath: String? = null,
    val classes: ArrayList<KClass<*>>? = null
)

