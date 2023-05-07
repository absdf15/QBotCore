package io.github.absdf15.qbot.core.annotation

import io.github.absdf15.qbot.core.module.common.MatchType
import io.github.absdf15.qbot.core.module.common.Permission

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Command(
    val searchTerm: String= "",
    val matchType: MatchType = MatchType.EXACT_MATCH,
    val permission: Permission = Permission.MEMBER,
    // 在方法上该参数无用
    val sendText: String = ""
)
