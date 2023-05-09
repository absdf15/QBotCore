package io.github.absdf15.qbot.core.annotation

import io.github.absdf15.qbot.core.module.common.Permission
import kotlin.reflect.KFunction

/**
 * 标记被指向的方法
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class PointedBy(
    val source: String = "",
    val index: Int = -1,
    val permission: Permission = Permission.MEMBER
)