package com.egm.ksenml

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

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
        var bt: Double?
        var bu: String? = null
        var bv: Double? = null
        this.forEach {
            bn = it.bn ?: bn
            bt = it.bt
            bu = it.bu ?: bu
            bv = it.bv ?: bv

            val resolvedRecord = ResolvedRecord(
                n = calculateName(bn, it.n),
                t = it.t ?: it.bt!!,
                u = calculateUnit(bu, it.u),
                v = calculateValue(bv, it.v)
            )

            resolvedRecords.add(resolvedRecord)
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

    private fun calculateValue(bv: Double?, v: Double): Double =
        if (bv != null)
            bv + v
        else
            v
}
