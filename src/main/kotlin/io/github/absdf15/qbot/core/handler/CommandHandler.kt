package io.github.absdf15.qbot.core.handler


import io.github.absdf15.qbot.core.QBotCore
import io.github.absdf15.qbot.core.annotation.Command
import io.github.absdf15.qbot.core.annotation.PointedBy
import io.github.absdf15.qbot.core.annotation.PointsTo
import io.github.absdf15.qbot.core.config.CommandConfig
import io.github.absdf15.qbot.core.module.common.ActionParams
import io.github.absdf15.qbot.core.module.common.Params
import io.github.absdf15.qbot.core.module.common.Permission
import io.github.absdf15.qbot.core.module.common.PointPair
import io.github.absdf15.qbot.core.utils.PermissionUtils.Companion.getPermission
import io.github.absdf15.qbot.core.utils.PermissionUtils.Companion.hasPermission
import io.github.absdf15.qbot.core.utils.TextUtils.Companion.getUnformattedCommand
import io.github.absdf15.qbot.core.utils.TextUtils.Companion.match
import io.github.absdf15.qbot.core.utils.TextUtils.Companion.parseCommand
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.ListenerHost
import net.mamoe.mirai.event.events.MessageEvent
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.memberExtensionFunctions
import kotlin.reflect.jvm.jvmName

class CommandHandler {
    companion object {
        private fun Permission.permissionLog(command: String, permission: Permission) {
            QBotCore.logger.info(
                "当前方法:$command 用户权限:$this 所需权限:$permission 是否有拥有执行权限: ${
                    this.hasPermission(
                        permission
                    )
                }"
            )
        }


        /**
         * 解析并执行指令
         */
        suspend fun MessageEvent.executeCommandFunction(): Boolean {
            val classes = Params.REGISTRY_COMMAND_CLASSES
            val rawCommand: String = getUnformattedCommand(message)
            val pointPair = PointPair(sender.id, subject.id)
            val pointSource = Params.POINT_MAP.getIfPresent(pointPair)
            val pointIndex = rawCommand.toIntOrNull()
            val commands = rawCommand.split("\\s+".toRegex(), limit = 2)
            var isRootCommand = false
            var sendText: String? = null
            // 判断是否属于有前置命令
            for (clazz in classes) {
                clazz.annotations.filterIsInstance<Command>().forEach { annotation ->
                    if (commands[0].match(annotation.searchTerm, annotation.matchType)) {
                        QBotCore
                        isRootCommand = true
                        sendText = annotation.sendText
                        return@forEach
                    }
                }
                if (isRootCommand) break
            }
            // 在有前置命令的情况下，并且没有默认实现方式时，执行该方法
            if ((commands.size < 2 || commands[1].isEmpty()) && isRootCommand) {
                if (sendText?.isNotEmpty() == true)
                    subject.sendMessage(sendText ?: "")
                return true
            }

            val (command, args) = if (isRootCommand) commands[1].parseCommand() else rawCommand.parseCommand()


            // 遍历类查找PointBy并执行
            if (pointIndex != null && pointIndex > -1 && pointSource != null)
                executePointedByAnnotation(classes, pointIndex,rawCommand ,pointSource, pointPair).takeIf { it }?.let {
                    return true
                }

            // 遍历指令列表并执行
            CommandConfig.command.forEach { (fullName, commandData) ->
                if (command.match(commandData.context, commandData.matchType)) {
                    val result = processCommandAliasAnnotation(fullName, command, args, rawCommand)
                    if (result) return true
                }
            }

            // 遍历类查找PointBy并执行
            if (pointSource != null)
                executePointedByAnnotation(classes, pointIndex, rawCommand,pointSource, pointPair).takeIf { it }?.let {
                    return true
                }


            // 遍历消息事件
            Params.REGISTRY_EVENT_CLASSES.forEach {
                processMessageEvent(it)
            }

            return false
        }

        private suspend fun MessageEvent.executePointedByAnnotation(
            classes: List<KClass<*>>,
            pointIndex: Int?,
            rawCommand: String,
            pointSource: Pair<String, Int>,
            pointPair: PointPair
        ): Boolean {
            for (clazz in classes) {
                clazz.memberExtensionFunctions.forEach { function ->
                    val result = processPointedByAnnotation(function, pointIndex, pointSource, rawCommand,pointPair, clazz)
                    if (result) return true
                }
            }
            return false
        }

        /**
         * 执行[EventHandler]注释的[MessageEvent]的扩展函数
         * @param clazz 继承了[ListenerHost]的类
         */
        private suspend fun MessageEvent.processMessageEvent(clazz: KClass<*>) {
            //QBotCore.logger.info(clazz.jvmName)

            clazz.memberExtensionFunctions.filter {
                it.annotations.any { annotation ->
                    annotation is EventHandler
                }
            }.forEach { function ->
                function.isSuspend.takeIf { it }?.let {
                    // 传参并执行suspend方法，获取object类单例对象的对象
                    function.callSuspend(clazz.objectInstance, this)
                }
            }
        }

        /**
         * 解析参数列表并调用方法执行
         * @param fullMethodName 完整方法名
         * @param command 解析后的参数
         * @param args 拆分后的参数
         * @param rawData 元数据
         *
         * @return 执行成功返回true
         */
        private suspend fun MessageEvent.processCommandAliasAnnotation(
            fullMethodName: String,
            command: String,
            args: List<String>,
            rawData: String,
        ): Boolean {
            // 分割类名和方法名
            val lastDotIndex = fullMethodName.lastIndexOf('.')
            val className = fullMethodName.substring(0, lastDotIndex)
            val methodName = fullMethodName.substring(lastDotIndex + 1)
            // 获取 KClass 实例
            val clazz = Class.forName(className).kotlin
            // 找到需要的方法
            val function = clazz.memberExtensionFunctions.firstOrNull {
                it.name == methodName
            }
            //QBotCore.logger.info("$clazz:$function")
            function?.annotations?.filterIsInstance<Command>()?.forEach { annotation ->
                val actionParams = ActionParams(
                    command, args, rawData, this.sender,
                    sender.getPermission(subject.id), this
                )
                return actionParams.processCommand(this, annotation, function, clazz)
            }
            return false
        }


        /**
         * 解析 Command 注解并调用方法执行
         * @param messageEvent 消息事件
         * @param command 解析后的参数
         * @param clazz 当前执行方法所在的类
         *
         * @return 执行成功返回true
         */
        private suspend fun ActionParams.processCommand(
            messageEvent: MessageEvent,
            command: Command,
            function: KFunction<*>,
            clazz: KClass<*>
        ): Boolean {
            if (permission.hasPermission(command.permission).not()) {
                QBotCore.logger.info("该用户没有权限！")
                permission.permissionLog(this.command, command.permission)
                return false
            }
//            QBotCore.logger.info("匹配成功！")
            QBotCore.logger.info("该方法名为:${function.name}")
            if (function.isSuspend) {
                CoroutineScope(Dispatchers.Default).launch {
                    function.callSuspend(clazz.objectInstance, this@processCommand)
                    function.annotations.filterIsInstance<PointsTo>().forEach {
                        Params.POINT_MAP.put(
                            PointPair(messageEvent.sender.id, messageEvent.subject.id),
                            command.searchTerm to it.count
                        )
                        return@forEach
                    }
                }
                return true
            }
            return false
        }


        /**
         * 根据[PointedBy]上的[PointedBy.source]和[PointedBy.index]来判断是否执行
         * @param function 待执行方法
         * @param index 需要对比的下标
         * @param source 包括[PointedBy.source]和[PointsTo.count]
         * @param pointPair 该Pair包括发送人QQ号和来源群号，若皆为发送人QQ代表私聊消息
         * @param clazz 当前待执行方法的类
         */
        private suspend fun MessageEvent.processPointedByAnnotation(
            function: KFunction<*>,
            index: Int?,
            source: Pair<String, Int>,
            rawCommand: String,
            pointPair: PointPair,
            clazz: KClass<*>
        ): Boolean {
            // 过滤出PointedBy并执行
            function.annotations.filterIsInstance<PointedBy>().forEach { annotation ->
                val fullFunctionName = "${clazz.jvmName}.${function.name}"
                if ((annotation.index == -1 || index == annotation.index) &&
                    (annotation.source == source.first || annotation.source == fullFunctionName)
                ) {
                    //判断当前所在节点的权限
                    if (sender.hasPermission(annotation.permission).not()) return false

                    val actionParams = ActionParams(
                        index.toString(), emptyList(), rawCommand, this.sender,
                        sender.getPermission(subject.id), this
                    )
                    var commandAnnotation: Command? = null

                    // 查找 Command 并判断是否有执行权限
                    function.annotations.filterIsInstance<Command>().forEach { command ->
                        if (actionParams.permission.hasPermission(command.permission).not()) {
                            QBotCore.logger.info("该用户没有权限！")
                            actionParams.permission.permissionLog(index.toString(), command.permission)
                            return false
                        } else {
                            commandAnnotation = command
                        }
                    }
                    // 获取匹配字符串或完整方法名
                    val searchTerm = commandAnnotation?.searchTerm ?: fullFunctionName

                    if (function.isSuspend) {
                        QBotCore.logger.info("匹配成功！")
                        CoroutineScope(Dispatchers.Default).launch {
                            // 执行suspend方法
                            function.callSuspend(clazz.objectInstance, actionParams)
                            val pair = Params.POINT_MAP.getIfPresent(pointPair) ?: return@launch
                            // 查找是否有PointsTo，若有替换当前指向链
                            function.annotations.filterIsInstance<PointsTo>().forEach {
                                Params.POINT_MAP.put(
                                    PointPair(sender.id, subject.id),
                                    searchTerm to it.count
                                )
                                return@launch
                            }
                            QBotCore.logger.info("pointPair:$pointPair")
                            // 判断当前次数
                            if (pair.second - 1 == 0) {
                                Params.POINT_MAP.invalidate(pointPair)
                            } else if (pair.second != -1) {
                                Params.POINT_MAP.put(pointPair, pair.first to pair.second - 1)
                            }
                        }
                        return true
                    }
                }
            }
            return false
        }
    }
}