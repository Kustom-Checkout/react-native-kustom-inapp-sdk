package com.kustom.mobile.sdk.reactnative.common.util

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.junit.Test
import kotlin.test.assertNotNull

internal class ParserUtilTest {
    @Test
    fun `test gson is not null`() {
        assertNotNull(ParserUtil.json)
    }

    @Serializable
    private data class TestData(
        @SerialName("data") val data: String,
    )

    private data class NonSerializableData(
        val data: String,
    )
}
