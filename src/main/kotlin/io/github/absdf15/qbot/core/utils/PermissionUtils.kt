package io.github.absdf15.qbot.core.utils

import io.github.absdf15.qbot.core.config.CoreConfig
import io.github.absdf15.qbot.core.module.common.Permission
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Member


class PermissionUtils {
    companion object {
        /**
         * 判断是否有权限
         */
        fun Contact.hasPermission(level: Permission, code: Long? = null): Boolean {
            val groupCode = code ?: if (this is Member) group.id else null
            return this.getPermission(groupCode).hasPermission(Permission.MEMBER)
        }

        /**
         * 获取用户权限
         */
        fun Contact.getPermission(executeGroupCode: Long? = null): Permission {
            if (CoreConfig.botOwners.contains(id)) return Permission.BOT_OWNER
            if (CoreConfig.botAdmins.contains(id)) return Permission.BOT_ADMIN
            executeGroupCode.takeIf { it != null }?.let { _ ->
                CoreConfig.enableGroup.forEach {
                    if (it == executeGroupCode) {
                        val group = bot.getGroup(it)
                        if (group?.owner?.id == id) return Permission.GROUP_OWNER
                        if (group?.get(id)?.permission?.level == 1) return Permission.GROUP_ADMIN
                        if (group?.get(id)?.permission?.level == 0) return Permission.MEMBER
                    }
                }
            }
            return Permission.VISITOR
        }
    }
}