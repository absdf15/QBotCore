package io.github.absdf15.qbot.core.module

import com.google.common.reflect.ClassPath
import io.github.absdf15.qbot.core.annotation.Component
import io.github.absdf15.qbot.core.handler.CommandHandler.Companion.executeCommandFunction
import io.github.absdf15.qbot.core.module.common.Params
import io.github.absdf15.qbot.core.module.common.QBotData
import io.github.absdf15.qbot.core.utils.ConfigUtils
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.ListenerHost
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.globalEventChannel
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.reflect.full.superclasses

abstract class QBotPlugin(
    qBotData: QBotData,
    parentCoroutineContext: CoroutineContext = EmptyCoroutineContext
) : KotlinPlugin(qBotData.description, parentCoroutineContext) {
    init {
        // 判断CommandPath并注册
        if (qBotData.commandPath != null) {
            val classLoader = this::class.java.classLoader
            val classes = ClassPath.from(classLoader).getTopLevelClasses(qBotData.commandPath)
            val commandClasses = classes.filter { clazz ->
                clazz.load().isAnnotationPresent(Component::class.java)
            }
            val kClasses = commandClasses.map { it.load().kotlin }
            Params.REGISTRY_COMMAND_CLASSES.addAll(kClasses)

        }
        // 判断是否为空并注册
        if (qBotData.classes != null) {
            Params.REGISTRY_EVENT_CLASSES.addAll(qBotData.classes)
        }
        logger.info("${Params.REGISTRY_EVENT_CLASSES.size}:${Params.REGISTRY_COMMAND_CLASSES.size}")
        ConfigUtils.loadCommandConfig()
        this.globalEventChannel().subscribeAlways<MessageEvent> {
            executeCommandFunction()
        }
    }

}