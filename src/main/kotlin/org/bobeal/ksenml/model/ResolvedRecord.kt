package org.bobeal.ksenml.model

import java.time.Instant

data class ResolvedRecord(
    val n: String,
    val t: Instant,
    val u: String?,
    val v: Double?,
    val vs: String?,
    val vb: Boolean?,
    val vd: String?
) {
    fun hasNoValue(): Boolean =
        v == null && vs == null && vb == null && vd == null
}
