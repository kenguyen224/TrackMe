package com.example.kenv.trackme

import com.example.kenv.lazyinit.resettableLazy
import com.example.kenv.lazyinit.resettableManager
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import org.junit.Test

/**
 * Created by KeNV on 05,January,2021
 * VNG company,
 * HCM, Viet Nam
 */
class LazyInitTest {
    @Test
    fun testResetTableLazy() {
        class Something {
            var seed = 1
            val lazyMgr = resettableManager()
            val x: String by resettableLazy(lazyMgr) { "x $seed" }
            val y: String by resettableLazy(lazyMgr) { "y $seed" }
            val z: String by resettableLazy(lazyMgr) { "z $x $y"}
        }

        val s = Something()
        val x1 = s.x
        val y1 = s.y
        val z1 = s.z

        assertEquals(x1, s.x)
        assertEquals(y1, s.y)
        assertEquals(z1, s.z)

        s.seed++ // without reset nothing should change

        assertSame(x1, s.x)
        assertSame(y1, s.y)
        assertSame(z1, s.z)

        s.lazyMgr.reset()

        s.seed++ // because of reset the values should change

        val x2 = s.x
        val y2 = s.y
        val z2 = s.z

        assertEquals(x2, s.x)
        assertEquals(y2, s.y)
        assertEquals(z2, s.z)

        assertNotEquals(x1, x2)
        assertNotEquals(y1, y2)
        assertNotEquals(z1, z2)

        s.seed++ // but without reset, nothing should change

        assertSame(x2, s.x)
        assertSame(y2, s.y)
        assertSame(z2, s.z)
    }
}
