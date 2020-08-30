package io.ksenml.parser

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.comparables.shouldBeEqualComparingTo
import io.kotest.matchers.date.shouldBeAfter
import io.kotest.matchers.date.shouldBeBefore
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.Instant

class SpecExamplesParserTests {

    @Test
    fun `it should parse example 1 from the spec`() {
        val pack =
            """
                [
                    { "n": "urn:dev:ow:10e2073a01080063", "v":23.1, "u":"Cel" }
                ]
            """.trimIndent()

        val resolvedRecords = pack.toSenMLRecords().normalize()

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

        val resolvedRecords = pack.toSenMLRecords().normalize()

        resolvedRecords.shouldHaveSize(2)

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

        val resolvedRecords = pack.toSenMLRecords().normalize()

        resolvedRecords.shouldHaveSize(7)

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

        val resolvedRecords = pack.toSenMLRecords().normalize()

        resolvedRecords.shouldHaveSize(12)

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

        val resolvedRecords = pack.toSenMLRecords().normalize()
        resolvedRecords.shouldHaveSize(13)

        resolvedRecords.should {
            it.forEach { record ->
                record.n.shouldBe("urn:dev:ow:10e2073a01080063")
            }
        }

        resolvedRecords[0].should {
            it.u.shouldBe("%RH")
        }
        val baseTime = resolvedRecords[0].t

        resolvedRecords[1].should {
            it.u.shouldBe("lon")
            it.v.shouldBe(24.30621)
            it.t.shouldBe(baseTime)
        }

        resolvedRecords[2].should {
            it.t.shouldBe(baseTime)
        }

        resolvedRecords[3].should {
            it.u.shouldBe("%RH")
            it.t.shouldBe(baseTime.plusSeconds(60))
        }

        resolvedRecords[9].should {
            it.u.shouldBe("%EL")
            it.t.shouldBe(baseTime.plusSeconds(150))
        }

        resolvedRecords[10].should {
            it.u.shouldBe("%RH")
            it.t.shouldBe(baseTime.plusSeconds(180))
        }
    }

    @Test
    fun `it should parse example 6 from the spec`() {
        val pack =
            """
                [
                    { "bn": "2001:db8::2/", "bt": 1320078429, "n": "temperature", "v": 25.2, "u": "Cel" },
                    { "n": "humidity", "v": 30, "u": "%RH" },
                    { "bn": "2001:db8::1/", "n": "temperature", "v": 12.3, "u": "Cel" },
                    { "n": "humidity", "v": 67, "u": "%RH" } 
                ]
            """.trimIndent()

        val resolvedRecords = pack.toSenMLRecords().normalize()
        resolvedRecords.shouldHaveSize(4)

        resolvedRecords.should {
            it.forEach { record ->
                record.t.epochSecond.shouldBe(1320078429)
            }
        }

        resolvedRecords[0].should {
            it.n.shouldBe("2001:db8::2/temperature")
            it.u.shouldBe("Cel")
            it.v.shouldBe(25.2)
        }

        resolvedRecords[1].should {
            it.n.shouldBe("2001:db8::2/humidity")
            it.u.shouldBe("%RH")
            it.v.shouldBe(30)
        }

        resolvedRecords[2].should {
            it.n.shouldBe("2001:db8::1/temperature")
            it.u.shouldBe("Cel")
            it.v.shouldBe(12.3)
        }

        resolvedRecords[3].should {
            it.n.shouldBe("2001:db8::1/humidity")
            it.u.shouldBe("%RH")
            it.v.shouldBe(67)
        }
    }

    @Test
    fun `it should parse example 7 from the spec`() {
        val pack =
            """
                [
                    { "bn": "urn:dev:ow:10e2073a01080063:", "n":"temp", "v":23.1, "u":"Cel" },
                    { "n":"label", "vs":"Machine Room" },
                    { "n":"open", "vb": false },
                    { "n":"nfv-reader", "vd":"aGkgCg" }
                ]
            """.trimIndent()

        val resolvedRecords = pack.toSenMLRecords().normalize()
        resolvedRecords.shouldHaveSize(4)

        resolvedRecords[0].should {
            it.v.shouldBe(23.1)
            it.vs.shouldBeNull()
            it.vb.shouldBeNull()
            it.vd.shouldBeNull()
        }

        resolvedRecords[1].should {
            it.v.shouldBeNull()
            it.vs.shouldBe("Machine Room")
            it.vb.shouldBeNull()
            it.vd.shouldBeNull()
        }

        resolvedRecords[2].should {
            it.v.shouldBeNull()
            it.vs.shouldBeNull()
            it.vb?.shouldBeFalse()
            it.vd.shouldBeNull()
        }

        resolvedRecords[3].should {
            it.v.shouldBeNull()
            it.vs.shouldBeNull()
            it.vb.shouldBeNull()
            it.vd.shouldBe("aGkgCg")
        }
    }

    @Test
    fun `it should parse example 9 from the spec`() {
        val pack =
            """
                [
                    {"bn":"urn:dev:ow:10e2073a01080063:"},
                    {"n":"temp","u":"Cel","v":23.1},
                    {"n":"heat","u":"/","v":1},
                    {"n":"fan","u":"/","v":0}
                ]
            """.trimIndent()

        val resolvedRecords = pack.toSenMLRecords().normalize()
        resolvedRecords.shouldHaveSize(3)

        resolvedRecords[0].should {
            it.n.shouldBe("urn:dev:ow:10e2073a01080063:temp")
            it.u.shouldBe("Cel")
            it.v.shouldBe(23.1)
        }

        resolvedRecords[1].should {
            it.n.shouldBe("urn:dev:ow:10e2073a01080063:heat")
            it.u.shouldBe("/")
            it.v.shouldBe(1.0)
        }

        resolvedRecords[2].should {
            it.n.shouldBe("urn:dev:ow:10e2073a01080063:fan")
            it.u.shouldBe("/")
            it.v.shouldBe(0.0)
        }
    }

    @Test
    fun `it should parse example 10 from the spec`() {
        val pack =
            """
                [
                    { "n": "urn:dev:ow:10e2073a01080063", "t": 1276020076, "v":23.5, "u":"Cel" },
                    { "n": "urn:dev:ow:10e2073a01080063", "t": 1276020091, "v":23.6, "u":"Cel" }
                ]
            """.trimIndent()

        val resolvedRecords = pack.toSenMLRecords().normalize()
        resolvedRecords.shouldHaveSize(2)

        resolvedRecords[1].t.shouldBe(resolvedRecords[0].t.plusSeconds(15))
        resolvedRecords[1].v.shouldBe(23.6)
    }

    @Test
    fun `it should parse example 11 from the spec`() {
        val pack =
            """
                [
                    { "bn": "urn:dev:ow:10e2073a01080063", "t": 1276020076, "v":23.5, "u":"Cel" },
                    { "t": 1276020091, "v":23.6, "u":"Cel" }
                ]
            """.trimIndent()

        val resolvedRecords = pack.toSenMLRecords().normalize()
        resolvedRecords.shouldHaveSize(2)

        resolvedRecords[0].n.shouldBe("urn:dev:ow:10e2073a01080063")
        resolvedRecords[1].n.shouldBe("urn:dev:ow:10e2073a01080063")
    }

    @Test
    fun `it should parse example 12 from the spec`() {
        val pack =
            """
               [
                    {"bt":1.320078429e+09, "bu":"/", "n":"2001:db8::3", "v":1 },
                    {"n":"2001:db8::4", "v":1 }
               ]
            """.trimIndent()

        val resolvedRecords = pack.toSenMLRecords().normalize()
        resolvedRecords.shouldHaveSize(2)

        resolvedRecords[0].t.shouldBeEqualComparingTo(resolvedRecords[1].t)
    }

    @Test
    fun `it should parse example 13 from the spec`() {
        val pack =
            """
                [
                    { "bt":1.320078429e+09,"bu":"/", "n":"2001:db8::3","v":0.5},
                    { "n":"2001:db8::4","v":0.5},
                    { "n":"2001:db8::3","t":0.1,"v":0},
                    { "n":"2001:db8::4","t":0.1,"v":0}
                ]
            """.trimIndent()

        val resolvedRecords = pack.toSenMLRecords().normalize()
        resolvedRecords.shouldHaveSize(4)

        resolvedRecords.should {
            it.forEach { record ->
                record.u.shouldBe("/")
            }
        }

        resolvedRecords[0].t.shouldBeEqualComparingTo(resolvedRecords[1].t)
        resolvedRecords[2].t.shouldBe(resolvedRecords[1].t.plusMillis(100))
        resolvedRecords[3].t.shouldBe(resolvedRecords[1].t.plusMillis(100))
    }
}
