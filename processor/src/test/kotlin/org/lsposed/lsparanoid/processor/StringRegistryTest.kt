package org.lsposed.lsparanoid.processor

import org.lsposed.lsparanoid.DeobfuscatorHelper
import kotlin.test.Test
import kotlin.test.assertEquals

class StringRegistryTest {
    @Test
    fun `registered strings decrypt to their original values`() {
        val registry = StringRegistryImpl(seed = 0x4c5350)
        val values = listOf(
            "",
            "plain ASCII",
            "quotes: \" and slash: \\",
            "Unicode: नमस्ते ☕ こんにちは",
            "line one\nline two\tend",
        )

        val ids = values.map(registry::registerString)
        val chunks = registry.getAllChunks().toTypedArray()

        values.zip(ids).forEach { (value, id) ->
            assertEquals(value, DeobfuscatorHelper.getString(id, chunks))
        }
    }
}
