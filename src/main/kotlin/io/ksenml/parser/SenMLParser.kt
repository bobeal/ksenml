package io.ksenml.parser

import io.ksenml.model.ResolvedRecord
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.time.Instant
import kotlin.math.pow

private val logger = LoggerFactory.getLogger("SenMLParser")

private val json = Json {
    ignoreUnknownKeys = true
    allowSpecialFloatingPointValues = true
}

fun String.toSenMLRecords(): List<Record> =
    try {
        json.decodeFromString(this)
    } catch (e: kotlinx.serialization.SerializationException) {
        logger.error(e.message ?: "Unknown exception while deserializing SenML pack")
        emptyList()
    }

fun List<Record>.normalize(): List<ResolvedRecord> {
    val resolvedRecords = mutableListOf<ResolvedRecord>()

    var bn: String? = null
    var bt: Double? = null
    var bu: String? = null
    var bv: Double? = null
    this.forEach {
        bn = it.bn ?: bn
        bt = it.bt ?: bt
        bu = it.bu ?: bu
        bv = it.bv ?: bv

        val n = calculateName(bn, it.n)
        val v = calculateValue(bv, it.v)

        if (n == null || (v == null && it.vs == null && it.vb == null && it.vd == null)) {
            logger.warn("Ignoring record $it as its name or value is null")
        } else {
            val resolvedRecord = ResolvedRecord(
                    n,
                    t = calculateTime(bt ?: 0.0, it.t ?: 0.0),
                    u = calculateUnit(bu, it.u),
                    v,
                    vs = it.vs,
                    vb = it.vb,
                    vd = it.vd
            )
            resolvedRecords.add(resolvedRecord)
        }
    }

    return resolvedRecords
}

private fun calculateName(bn: String?, n: String?): String? =
    if (bn != null) {
        if (n != null) {
            val lastBnChar = bn.last()
            // the trailing separator in the bn often misses from the payloads,
            // so add it manually if last char in bn is not alphanumeric
            when {
                (lastBnChar in 'a'..'z' || lastBnChar in 'A'..'Z' || lastBnChar in '0'..'9') -> "$bn:$n"
                else -> "$bn$n"
            }
        }
        else
            bn
    } else {
        n
    }

private fun calculateUnit(bu: String?, u: String?): String? =
    u ?: bu

private fun calculateValue(bv: Double?, v: Double?): Double? =
    if (bv != null && v != null)
        bv + v
    else v

private fun calculateTime(bt: Double, t: Double): Instant {
    val totalTime = bt + t

    return if (totalTime > 2.0.pow(28))
        Instant.ofEpochMilli((totalTime * 1000).toLong())
    else
        Instant.now().plusSeconds(totalTime.toLong())
}
