package com.egm.ksenml

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RecordTest {

    @Test
    fun `it should parse a single datapoint`() {
        val datapoint =
            """
                [
                  {"n":"urn:dev:ow:10e2073a01080063","u":"Cel","v":23.1}
                ]
            """.trimIndent()

        val records = Json.decodeFromString<List<Record>>(datapoint)
        assertEquals(1, records.size)
        val record = records[0]
        assertEquals("urn:dev:ow:10e2073a01080063", record.n)
        assertEquals("Cel", record.u)
        assertEquals(23.1, record.v)
    }

    @Test
    fun `it should parse multiple datapoints`() {
        val datapoints =
            """
                [
                  {"bn":"urn:dev:ow:10e2073a01080063:","n":"voltage","u":"V","v":120.1},
                  {"n":"current","u":"A","v":1.2}
                ]
            """.trimIndent()

        val records = Json.decodeFromString<List<Record>>(datapoints)
        assertEquals(2, records.size)

        val firstRecord = records[0]
        assertEquals("urn:dev:ow:10e2073a01080063:", firstRecord.bn)
        assertEquals("voltage", firstRecord.n)
        assertEquals("V", firstRecord.u)
        assertEquals(120.1, firstRecord.v)

        val secondRecord = records[1]
        assertEquals("current", secondRecord.n)
        assertEquals("A", secondRecord.u)
        assertEquals(1.2, secondRecord.v)
    }

    @Test
    fun `it should parse multiple datapoints with relative time`() {
        val datapoints =
            """
                [
                  {"bn":"urn:dev:ow:10e2073a0108006:","bt":1.276020076001e+09,
                   "bu":"A","bver":5,
                   "n":"voltage","u":"V","v":120.1},
                  {"n":"current","t":-5,"v":1.2},
                  {"n":"current","t":-4,"v":1.3},
                  {"n":"current","t":-3,"v":1.4},
                  {"n":"current","t":-2,"v":1.5},
                  {"n":"current","t":-1,"v":1.6},
                  {"n":"current","v":1.7}
                ]
            """.trimIndent()

        val records = Json { ignoreUnknownKeys = true }.decodeFromString<List<Record>>(datapoints)
        assertEquals(7, records.size)

        val firstRecord = records[0]
        assertEquals("urn:dev:ow:10e2073a0108006:", firstRecord.bn)
        assertEquals("voltage", firstRecord.n)
        assertEquals("V", firstRecord.u)
        assertEquals(120.1, firstRecord.v)
        assertEquals(1.276020076001e+09, firstRecord.bt)

        val secondRecord = records[1]
        assertEquals("current", secondRecord.n)
        assertEquals(-5.0, secondRecord.t)
        assertEquals(1.2, secondRecord.v)
    }

    @Test
    fun `it should parse multiple measurements`() {
        val datapoints =
            """
                [
                  {"bn":"urn:dev:ow:10e2073a01080063","bt":1.320067464e+09,
                   "bu":"%RH","v":20},
                  {"u":"lon","v":24.30621},
                  {"u":"lat","v":60.07965},
                  {"t":60,"v":20.3},
                  {"u":"lon","t":60,"v":24.30622},
                  {"u":"lat","t":60,"v":60.07965},
                  {"t":120,"v":20.7},
                  {"u":"lon","t":120,"v":24.30623},
                  {"u":"lat","t":120,"v":60.07966},
                  {"u":"%EL","t":150,"v":98},
                  {"t":180,"v":21.2},
                  {"u":"lon","t":180,"v":24.30628},
                  {"u":"lat","t":180,"v":60.07967}
                ]
            """.trimIndent()

        val records = Json { ignoreUnknownKeys = true }.decodeFromString<List<Record>>(datapoints)
        assertEquals(13, records.size)

        val firstRecord = records[0]
        assertEquals("urn:dev:ow:10e2073a01080063", firstRecord.bn)
        assertEquals("%RH", firstRecord.bu)
        assertEquals(20.0, firstRecord.v)
        assertEquals(1.320067464e+09, firstRecord.bt)

        val secondRecord = records[1]
        assertEquals("lon", secondRecord.u)
        assertEquals(24.30621, secondRecord.v)
    }
}
