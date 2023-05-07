package io.github.absdf15.qbot.core.module.common

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.event.events.MessageEvent

data class ActionParams(
    val command: String,
    val args: List<String>,
    val rawCommand: String,
    val contact: Contact,
    val permission: Permission,
    val messageEvent: MessageEvent
)
