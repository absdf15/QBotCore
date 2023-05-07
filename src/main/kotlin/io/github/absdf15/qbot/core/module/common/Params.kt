package io.github.absdf15.qbot.core.module.common

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass

class Params {
    companion object{
        /**
         * Point 指向列表
         */
        val POINT_MAP: Cache<PointPair, Pair<String, Int>> = CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(50000)
            .build()

        /**
         * 注册队列
         */
        val REGISTRY_COMMAND_CLASSES = arrayListOf<KClass<*>>()
        /**
         * 注册队列
         */
        val REGISTRY_EVENT_CLASSES = arrayListOf<KClass<*>>()
    }
}