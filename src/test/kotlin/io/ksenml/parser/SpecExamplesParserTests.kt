package io.ksenml.parser

import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.date.shouldBeAfter
import io.kotest.matchers.date.shouldBeBefore
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import mu.KotlinLogging
import io.ksenml.parser.SenMLParser.normalize
import org.junit.jupiter.api.Test
import java.time.Instant

private val logger = KotlinLogging.logger {}

class SpecExamplesParserTests {

    @Test
    fun `it should parse example 1 from the spec`() {
        val pack =
            """
                [
                    { "n": "urn:dev:ow:10e2073a01080063", "v":23.1, "u":"Cel" }
                ]
            """.trimIndent()

        val resolvedRecords = SenMLParser.fromJson(pack).normalize()

        resolvedRecords.shouldBeSingleton {
            it.n.shouldBe("urn:dev:ow:10e2073a01080063")
            it.v.shouldBe(23.1)
            it.u.shouldBe("Cel")
            it.t.shouldBeBefore(Instant.now())
            it.t.shouldBeAfter(Instant.now().minusSeconds(5))
        }
    }

    @Test
    fun `it should parse example 2 from the spec`() {
        val pack =
            """
                [
                    { "bn": "urn:dev:ow:10e2073a01080063:", "n": "voltage", "t": 0, "u": "V", "v": 120.1 },
                    { "n": "current", "t": 0, "u": "A", "v": 1.2 } 
                ]
            """.trimIndent()

        val resolvedRecords = SenMLParser.fromJson(pack).normalize()

        resolvedRecords.size.shouldBe(2)
        resolvedRecords[0].should {
            it.n.shouldBe("urn:dev:ow:10e2073a01080063:voltage")
            it.v.shouldBe(120.1)
            it.u.shouldBe("V")
            it.t.shouldBeBefore(Instant.now())
            it.t.shouldBeAfter(Instant.now().minusSeconds(5))
        }
        resolvedRecords[1].should {
            it.n.shouldBe("urn:dev:ow:10e2073a01080063:current")
            it.v.shouldBe(1.2)
            it.u.shouldBe("A")
            it.t.shouldBeBefore(Instant.now())
            it.t.shouldBeAfter(Instant.now().minusSeconds(5))
        }
    }

    @Test
    fun `it should parse example 3 from the spec`() {
        val pack =
            """
                [
                    { "bn": "urn:dev:ow:10e2073a0108006:", "bt": 1276020076.001, "bu": "A", "bver": 5, "n": "voltage", "u": "V", "v": 120.1 },
                    { "n": "current", "t": -5, "v": 1.2 },
                    { "n": "current", "t": -4, "v": 1.30 },
                    { "n": "current", "t": -3, "v": 0.14e1 },
                    { "n": "current", "t": -2, "v": 1.5 },
                    { "n": "current", "t": -1, "v": 1.6 },
                    { "n": "current", "t": 0,  "v": 1.7 }                 
                ]
            """.trimIndent()

        val resolvedRecords = SenMLParser.fromJson(pack).normalize()

        resolvedRecords.size.shouldBe(7)
        resolvedRecords[0].should {
            it.n.shouldBe("urn:dev:ow:10e2073a0108006:voltage")
            it.v.shouldBe(120.1)
            it.u.shouldBe("V")
            it.t.toString().shouldBe("2010-06-08T18:01:16.001Z")
        }
        val baseTime = resolvedRecords[0].t

        resolvedRecords[1].should {
            it.n.shouldBe("urn:dev:ow:10e2073a0108006:current")
            it.v.shouldBe(1.2)
            it.u.shouldBe("A")
            it.t.shouldBe(baseTime.minusSeconds(5))
        }
    }

    @Test
    fun `it should parse example 4 from the spec`() {
        val pack =
            """
                [ 
                    { "bn": "urn:dev:ow:10e2073a01080063", "bt": 1320067464, "bu": "%RH", "v": 21.2, "t": 0 },
                    { "v": 21.3, "t": 10 },
                    { "v": 21.4, "t": 20 },
                    { "v": 21.4, "t": 30 },
                    { "v": 21.5, "t": 40 },
                    { "v": 21.5, "t": 50 },
                    { "v": 21.5, "t": 60 },
                    { "v": 21.6, "t": 70 },
                    { "v": 21.7, "t": 80 },
                    { "v": 21.5, "t": 90 },
                    { "v": 21.5, "t": 91 },
                    { "v": 21.5, "t": 92 }
                 ]

            """.trimIndent()

        val resolvedRecords = SenMLParser.fromJson(pack).normalize()

        resolvedRecords.size.shouldBe(12)

        resolvedRecords[0].should {
            it.n.shouldBe("urn:dev:ow:10e2073a01080063")
            it.u.shouldBe("%RH")
            it.v.shouldBe(21.2)
            it.t.toString().shouldBe("2011-10-31T13:24:24Z")
        }

        resolvedRecords[1].should {
            it.n.shouldBe("urn:dev:ow:10e2073a01080063")
            it.u.shouldBe("%RH")
            it.v.shouldBe(21.3)
            it.t.toString().shouldBe("2011-10-31T13:24:34Z")
        }

        resolvedRecords[11].should {
            it.n.shouldBe("urn:dev:ow:10e2073a01080063")
            it.u.shouldBe("%RH")
            it.v.shouldBe(21.5)
            it.t.toString().shouldBe("2011-10-31T13:25:56Z")
        }
    }

    @Test
    fun `it should parse example 5 from the spec`() {
        val pack =
            """
                [
                    { "bn": "urn:dev:ow:10e2073a01080063", "bt": 1320067464, "bu": "%RH", "v": 20.0, "t": 0 },
                    { "v": 24.30621, "u": "lon", "t": 0 },
                    { "v": 60.07965, "u": "lat", "t": 0 },  
                    { "v": 20.3, "t": 60 },
                    { "v": 24.30622, "u": "lon", "t": 60 },
                    { "v": 60.07965, "u": "lat", "t": 60 },
                    { "v": 20.7, "t": 120 },
                    { "v": 24.30623, "u": "lon", "t": 120 },
                    { "v": 60.07966, "u": "lat", "t": 120 },
                    { "v": 98.0, "u": "%EL", "t": 150 },
                    { "v": 21.2, "t": 180 },
                    { "v": 24.30628, "u": "lon", "t": 180 },
                    { "v": 60.07967, "u": "lat", "t": 180 } 
                ]
            """.trimIndent()

        val resolvedRecords = SenMLParser.fromJson(pack).normalize()

        logger.debug { resolvedRecords }
    }
}
