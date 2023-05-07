package io.github.absdf15.qbot.core.utils



import io.github.absdf15.qbot.core.module.common.MatchType
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.AtAll
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.QuoteReply

public class TextUtils {


    companion object {

        /**
         * 解析参数列表
         * @return 返回 Command指令和后续参数列表
         */
        fun String.parseCommand(): Pair<String, List<String>> {
            val tokens = this.split("\\s+".toRegex())
            val command = tokens.first()
            val arguments = mutableListOf<String>()
            var i = 1
            while (i < tokens.size) {
                val token = tokens[i]
                if (token.startsWith("{")) {
                    val argument = extractArgument(tokens, i, "{", "}")
                    if (argument == null) {
                        return command to emptyList() // ERROR
                    } else {
                        arguments.add("{$argument}")
                        i += argument.split("\\s+".toRegex()).size
                    }
                } else if (token.startsWith("[")) {
                    val argument = extractArgument(tokens, i, "[", "]")
                    if (argument == null) {
                        return command to emptyList<String>() // ERROR
                    } else {
                        arguments.add("[$argument]")
                        i += argument.split("\\s+".toRegex()).size
                    }
                } else {
                    arguments.add(token)
                    i++
                }
            }
            return Pair(command, arguments)
        }

        /**
         * 解析消息来源的 [MessageChain]，并拼接合成字符串
         *
         * @return 拼接后的字符串
         */
        fun getUnformattedCommand(message: MessageChain): String {
            return if (message.size > 2) {
                buildString {
                    val dropNumber = if (message[1] is QuoteReply) 2 else 1
                    //drop跳过第一个，dropLast跳过最后一个
                    message.drop(dropNumber).dropLast(1).forEach { it ->
                        val content = if (it is At && it !is AtAll) it.contentToString()
                            .substringAfter("@") else it.contentToString()
                        append("$content ")
                    }
                    val last = message.last()
                    if (last is At && last !is AtAll) append(last.contentToString().substringAfter("@"))
                    else append(last.contentToString())
                }
            } else message.getOrNull(1)?.contentToString() ?: ""
        }

        private fun extractArgument(tokens: List<String>, startIndex: Int, opening: String, closing: String): String? {
            val argumentTokens = mutableListOf<String>()
            var i = startIndex
            while (i < tokens.size) {
                val token = tokens[i]
                if (token.endsWith(closing)) {
                    argumentTokens.add(token.removeSuffix(closing))
                    break
                } else {
                    argumentTokens.add(token)
                    i++
                }
            }
            if (i == tokens.size) {
                return null // ERROR
            }
            val argument = argumentTokens.joinToString(" ")
            return argument.removePrefix(opening).replace("\\{", "{").replace("\\}", "}")
        }


        /**
         * 使用指定的匹配方式对给定的字符串进行匹配
         *
         * @param target 用于匹配的字段
         * @param matchType 匹配方式
         *
         * @return 如果匹配成功则返回true，否则返回false
         */
        fun String.match(target: String, matchType: MatchType): Boolean {
            return when (matchType) {
                MatchType.EXACT_MATCH -> this == target
                MatchType.PARTIAL_MATCH -> this.contains(target)
                MatchType.REGEX_MATCH -> Regex(target).matches(this)
            }
        }

    }
}


