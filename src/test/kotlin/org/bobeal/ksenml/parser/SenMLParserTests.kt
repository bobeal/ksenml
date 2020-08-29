package org.bobeal.ksenml.parser

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import org.bobeal.ksenml.parser.SenMLParser.normalize
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class SenMLParserTests {

    @Test
    fun `it should normalize a single datapoint with only regular fields`() {
        val pack =
            """
                [
                  {"n":"urn:dev:ow:10e2073a01080063","u":"Cel","v":23.1, "t": 1.276020076001e+09}
                ]
            """.trimIndent()

        val resolvedRecords = SenMLParser.fromJson(pack).normalize()
        assertEquals(1, resolvedRecords.size)
        val resolvedRecord = resolvedRecords[0]
        assertEquals("urn:dev:ow:10e2073a01080063", resolvedRecord.n)
        assertEquals("Cel", resolvedRecord.u)
        assertEquals(23.1, resolvedRecord.v)
        assertEquals(1276020076, resolvedRecord.t.epochSecond)
        assertEquals(1276020076001, resolvedRecord.t.toEpochMilli())
        assertEquals("2010-06-08T18:01:16.001Z", resolvedRecord.t.toString())
    }

    @Test
    fun `it should parse a date with a nanosecond precision`() {
        val pack =
            """
                [
                  {"bn":"urn:dev:ow:10e2073a01080063:","n":"voltage","v":120.1, "t": 1596719815.117644}
                ]
            """.trimIndent()

        val resolvedRecords = SenMLParser.fromJson(pack).normalize()
        assertEquals(1, resolvedRecords.size)

        assertEquals("2020-08-06T13:16:55.117Z", resolvedRecords[0].t.toString())
    }

    @Test
    fun `it should normalize multiple datapoints using a bn`() {
        val pack =
            """
                [
                  {"bn":"urn:dev:ow:10e2073a01080063:","n":"voltage","u":"V","v":120.1},
                  {"n":"current","u":"A","v":1.2}
                ]
            """.trimIndent()

        val resolvedRecords = SenMLParser.fromJson(pack).normalize()
        assertEquals(2, resolvedRecords.size)

        assertEquals("urn:dev:ow:10e2073a01080063:voltage", resolvedRecords[0].n)
        assertEquals("urn:dev:ow:10e2073a01080063:current", resolvedRecords[1].n)
    }

    @Test
    fun `it should reject a record without a name`() {
        val pack =
            """
                [
                  {"t":"1234567890","u":"V","v":120.1}
                ]
            """.trimIndent()

        val exception = assertThrows<InvalidSenmlRecordException> {
            SenMLParser.fromJson(pack).normalize()
        }
        assertEquals("Record has no bn, nor n attribute", exception.message)
    }

    @Test
    fun `it should add a base value to a value found in a record`() {
        val pack =
            """
                [
                  {"t":"1234567890","u":"V","n":"voltage", "v": 2, "bv": 3},
                  {"t":"1234567890","u":"V","n":"voltage", "v": 5}
                ]
            """.trimIndent()

        val resolvedRecords = SenMLParser.fromJson(pack).normalize()
        assertEquals(2, resolvedRecords.size)

        assertEquals(5.0, resolvedRecords[0].v)
        assertEquals(8.0, resolvedRecords[1].v)
    }

    @Test
    fun `it should ignore a record without a value`() {
        val pack =
            """
                [
                  {"t":"1234567890","u":"V","n":"voltage"}
                ]
            """.trimIndent()

        val resolvedRecords = SenMLParser.fromJson(pack).normalize()
        resolvedRecords.shouldBeEmpty()
    }

    @Test
    fun `it should handle complex multi-measure`() {
        val pack =
            """
                [
                  {"bt": 1587664053.315175, "bn": "70b3d57050000958:Oyster4:", "v": 43.6361761, "u": "lat", "n": "latitude"}, 
                  {"v": 6.9132341, "u": "lon", "n": "longitude"}, 
                  {"v": 0, "u": "km/h", "n": "speed"}, 
                  {"v": 3775, "u": "%EL", "n": "batteryLevel"}, 
                  {"vs": "0", "n": "TripMode"}, 
                  {"vs": "0", "n": "LastFix"}
                ]
            """.trimIndent()

        val resolvedRecords = SenMLParser.fromJson(pack).normalize()

        resolvedRecords.size.shouldBe(6)

        resolvedRecords[0].should {
            it.n.shouldBe("70b3d57050000958:Oyster4:latitude")
            it.u.shouldBe("lat")
            it.v.shouldBe(43.6361761)
        }

        resolvedRecords[4].should {
            it.n.shouldBe("70b3d57050000958:Oyster4:TripMode")
            it.vs.shouldBe("0")
            it.u.shouldBeNull()
        }
    }

    @Test
    fun `it should handle a simple multi-datapoint pack`() {
        val pack =
            """
                 [
                    { "n": "urn:ngsi-ld:Sensor:Sample000001:incoming", "u": "count", "v": 1200 },
                    { "n": "urn:ngsi-ld:Sensor:Sample000002:outgoing", "u": "count", "v": 506 }    
                ]
            """.trimIndent()

        val resolvedRecords = SenMLParser.fromJson(pack).normalize()

        assertEquals(2, resolvedRecords.size)
        assertEquals("urn:ngsi-ld:Sensor:Sample000001:incoming", resolvedRecords[0].n)
        assertEquals("urn:ngsi-ld:Sensor:Sample000002:outgoing", resolvedRecords[1].n)
    }
}
