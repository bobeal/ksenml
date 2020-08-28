package com.egm.ksenml

import kotlinx.serialization.Serializable

@Serializable
data class Record(
    val bn: String? = null,
    val bt: Double? = 0.0,
    val bu: String? = null,
    val bv: Double? = 0.0,
    val n: String? = null,
    val t: Double? = 0.0,
    val u: String? = null,
    val v: Double? = 0.0
)
