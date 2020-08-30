package io.ksenml.parser

import io.ksenml.model.ResolvedRecord
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import java.time.Instant
import kotlin.math.pow

private val logger = KotlinLogging.logger {}

object SenMLParser {

    fun fromJson(jsonPack: String): List<Record> =
        try {
            Json { ignoreUnknownKeys = true }.decodeFromString(jsonPack)
        } catch (e: kotlinx.serialization.SerializationException) {
            throw InvalidSenmlRecordException(e.message ?: "Unknown exception while deserializing SenML pack")
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

            val resolvedRecord = ResolvedRecord(
                n = calculateName(bn, it.n),
                t = calculateTime(bt ?: 0.0, it.t ?: 0.0),
                u = calculateUnit(bu, it.u),
                v = calculateValue(bv, it.v),
                vs = it.vs,
                vb = it.vb,
                vd = it.vd
            )

            if (resolvedRecord.hasNoValue()) {
                logger.warn("Ignoring record $resolvedRecord as it has no value")
            } else {
                resolvedRecords.add(resolvedRecord)
            }
        }

        return resolvedRecords
    }

    private fun calculateName(bn: String?, n: String?): String =
        if (bn != null) {
            if (n != null)
                bn + n
            else
                bn
        } else {
            n ?: throw InvalidSenmlRecordException("Record has no bn, nor n attribute")
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
}
