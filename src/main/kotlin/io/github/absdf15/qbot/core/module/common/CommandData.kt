package io.github.absdf15.qbot.core.module.common

import kotlinx.serialization.Serializable


@Serializable
data class CommandData (
    val context: String = "",
    val matchType: MatchType = MatchType.EXACT_MATCH
)