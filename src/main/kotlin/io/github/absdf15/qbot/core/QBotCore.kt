package io.github.absdf15.qbot.core

import io.github.absdf15.qbot.core.QBotCore.reload
import io.github.absdf15.qbot.core.QBotCore.save
import io.github.absdf15.qbot.core.config.CommandConfig
import io.github.absdf15.qbot.core.config.CoreConfig
import io.github.absdf15.qbot.core.config.ExampleCommand
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.console.util.ConsoleInput
import net.mamoe.mirai.utils.info

object QBotCore : KotlinPlugin(
    JvmPluginDescription(
        id = "io.github.absdf15.qbot.core",
        name = "QBotCore",
        version = "0.1.1",
    ) {
        author("absdf15")
    }
) {
    override fun onEnable() {
        logger.info { "Plugin loaded" }
        ExampleCommand.save()
        CommandConfig.reload()
        CoreConfig.reload()
        loadCoreConfig()
    }

    private fun loadCoreConfig() {
        CoreConfig.reload()
        if (CoreConfig.botOwners.isEmpty()) {
            runBlocking {
                var code: Long? = null
                while (code == null) {
                    code = ConsoleInput.requestInput("请输入你（机器人主人）的QQ号码:").trim().toLongOrNull()
                }
                CoreConfig.botOwners.add(code)
            }
            CoreConfig.save()
        }
    }
}