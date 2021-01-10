package com.example.kenv.lazyinit

import kotlin.reflect.KProperty

/**
 * Created by KeNV on 05,January,2021
 * VNG company,
 * HCM, Viet Nam
 */
class ResettableLazy<T>(private val manager: ResettableLazyManager, val init: () -> T) : Resettable {
    @Volatile
    var lazyHolder = makeInitBlock()

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return lazyHolder.value
    }

    override fun reset() {
        lazyHolder = makeInitBlock()
    }

    private fun makeInitBlock(): Lazy<T> {
        return lazy {
            manager.register(this)
            init()
        }
    }
}
