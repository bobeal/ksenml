package io.ksenml.parser

import kotlinx.serialization.Serializable

@Serializable
data class Record(
    val bn: String? = null,
    val bt: Double? = null,
    val bu: String? = null,
    val bv: Double? = null,
    val n: String? = null,
    val t: Double? = null,
    val u: String? = null,
    val v: Double? = null,
    val vs: String? = null,
    val vb: Boolean? = null,
    val vd: String? = null
)
