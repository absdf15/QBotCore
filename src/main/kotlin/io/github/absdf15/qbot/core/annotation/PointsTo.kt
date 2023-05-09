package io.github.absdf15.qbot.core.annotation

import io.github.absdf15.qbot.core.module.common.Permission
import java.util.concurrent.TimeUnit

/**
 * 标记指向的方法
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class PointsTo(
    // 0 不添加，-1 永久指向
    val count: Int = 1
)