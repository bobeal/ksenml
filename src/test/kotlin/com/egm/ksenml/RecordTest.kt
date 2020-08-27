package com.egm.ksenml

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RecordTest {

    @Test
    fun `it should parse a record with a single bn`() {
        val bn =
            """
                { "bn": "blop" }
            """.trimIndent()

        val record = Json.decodeFromString<Record>(bn)
        assertEquals("blop", record.bn)
    }
}
