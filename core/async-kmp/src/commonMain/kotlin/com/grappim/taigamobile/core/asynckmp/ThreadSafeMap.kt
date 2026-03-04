package com.grappim.taigamobile.core.asynckmp

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized

class ThreadSafeMap<K, V> {
    private val map = mutableMapOf<K, V>()
    private val lock = SynchronizedObject()

    fun put(key: K, value: V) = synchronized(lock) {
        map[key] = value
    }

    fun get(key: K): V? = synchronized(lock) {
        map[key]
    }

    fun remove(key: K): V? = synchronized(lock) {
        map.remove(key)
    }

    fun getOrPut(key: K, defaultValue: () -> V): V = synchronized(lock) {
        map.getOrPut(key, defaultValue)
    }
}
