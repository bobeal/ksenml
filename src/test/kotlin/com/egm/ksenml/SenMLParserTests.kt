package com.egm.ksenml

import com.egm.ksenml.SenMLParser.normalize
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class SenMLParserTests {

    @Test
    fun `it should normalize a single datapoint with only regular fields`() {
        val datapoint =
            """
                [
                  {"n":"urn:dev:ow:10e2073a01080063","u":"Cel","v":23.1, "t": 1.320067464e+09}
                ]
            """.trimIndent()

        val resolvedRecords = SenMLParser.fromJson(datapoint).normalize()
        assertEquals(1, resolvedRecords.size)
        val resolvedRecord = resolvedRecords[0]
        assertEquals("urn:dev:ow:10e2073a01080063", resolvedRecord.n)
        assertEquals("Cel", resolvedRecord.u)
        assertEquals(23.1, resolvedRecord.v)
        assertEquals(1.320067464e+09, resolvedRecord.t)
    }

    @Test
    fun `it should normalize multiple datapoints using a bn`() {
        val datapoint =
            """
                [
                  {"bn":"urn:dev:ow:10e2073a01080063:","n":"voltage","u":"V","v":120.1},
                  {"n":"current","u":"A","v":1.2}
                ]
            """.trimIndent()

        val resolvedRecords = SenMLParser.fromJson(datapoint).normalize()
        assertEquals(2, resolvedRecords.size)

        assertEquals("urn:dev:ow:10e2073a01080063:voltage", resolvedRecords[0].n)
        assertEquals("urn:dev:ow:10e2073a01080063:current", resolvedRecords[1].n)
    }

    @Test
    fun `it should reject a record without a name`() {
        val datapoint =
            """
                [
                  {"t":"1234567890","u":"V","v":120.1}
                ]
            """.trimIndent()

        val exception = assertThrows<InvalidSenmlRecordException> {
            SenMLParser.fromJson(datapoint).normalize()
        }
        assertEquals("Record has no bn, nor n attribute", exception.message)
    }

    @Test
    fun `it should add a base value to a value found in a record`() {
        val datapoint =
            """
                [
                  {"t":"1234567890","u":"V","n":"voltage", "v": 2, "bv": 3},
                  {"t":"1234567890","u":"V","n":"voltage", "v": 5}
                ]
            """.trimIndent()

        val resolvedRecords = SenMLParser.fromJson(datapoint).normalize()
        assertEquals(2, resolvedRecords.size)

        assertEquals(5.0, resolvedRecords[0].v)
        assertEquals(8.0, resolvedRecords[1].v)
    }

    @Test
    fun `it should reject a record without a value`() {
        val datapoint =
            """
                [
                  {"t":"1234567890","u":"V","n":"voltage"}
                ]
            """.trimIndent()

        val exception = assertThrows<InvalidSenmlRecordException> {
            SenMLParser.fromJson(datapoint).normalize()
        }
        assertEquals("Field 'v' is required, but it was missing", exception.message)
    }
}
