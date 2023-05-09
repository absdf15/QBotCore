package io.github.absdf15.qbot.core.utils

import io.github.absdf15.qbot.core.QBotCore.save
import io.github.absdf15.qbot.core.annotation.Command
import io.github.absdf15.qbot.core.config.CommandConfig
import io.github.absdf15.qbot.core.config.ExampleCommand
import io.github.absdf15.qbot.core.module.common.CommandData
import io.github.absdf15.qbot.core.module.common.Params
import io.github.absdf15.qbot.core.module.common.PointPair
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
                        val fullFunctionName = "${clazz.jvmName}.${function.name}"
                        ExampleCommand.command[fullFunctionName] = commandData
                        if (CommandConfig.command.containsKey(fullFunctionName).not())
                            CommandConfig.command[fullFunctionName] = commandData
                    }
                }
            }
            CommandConfig.save()
            ExampleCommand.save()
        }

        fun save(config : ReadOnlyPluginConfig){
            config.save()
        }

        fun put(pointPair: PointPair,pair: Pair<String,Int>){
            Params.POINT_MAP.put(pointPair,pair)
        }
    }
}