package io.github.absdf15.qbot.core.utils

import io.github.absdf15.qbot.core.QBotCore.save
import io.github.absdf15.qbot.core.annotation.Command
import io.github.absdf15.qbot.core.config.CommandConfig
import io.github.absdf15.qbot.core.config.ExampleCommand
import io.github.absdf15.qbot.core.module.common.CommandData
import io.github.absdf15.qbot.core.module.common.Params
import net.mamoe.mirai.console.data.ReadOnlyPluginConfig
import kotlin.reflect.full.memberExtensionFunctions
import kotlin.reflect.jvm.jvmName

public class ConfigUtils {
    companion object {
        fun loadCommandConfig() {
            Params.REGISTRY_COMMAND_CLASSES.forEach { clazz ->
                clazz.memberExtensionFunctions.forEach { function ->
                    function.annotations.filterIsInstance<Command>().forEach { command ->
                        val commandData = CommandData(context = command.searchTerm, matchType = command.matchType)
                        val functionName = "${clazz.jvmName}.${function.name}"
                        ExampleCommand.command[functionName] = commandData
                        if (CommandConfig.command.containsKey(functionName).not())
                            CommandConfig.command[functionName] = commandData
                    }
                }
            }
            CommandConfig.save()
            ExampleCommand.save()
        }

        fun save(config : ReadOnlyPluginConfig){
            config.save()
        }
    }
}