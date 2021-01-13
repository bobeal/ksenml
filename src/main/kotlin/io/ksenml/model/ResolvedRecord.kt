package io.ksenml.model

import java.time.Instant

data class ResolvedRecord(
    val n: String,
    val t: Instant,
    val u: String?,
    val v: Double?,
    val vs: String?,
    val vb: Boolean?,
    val vd: String?
)
