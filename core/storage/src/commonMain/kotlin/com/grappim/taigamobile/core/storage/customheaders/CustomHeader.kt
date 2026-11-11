package com.grappim.taigamobile.core.storage.customheaders

import kotlinx.serialization.Serializable

/** One user-defined request header for an authenticating reverse proxy in front of the Taiga server. */
@Serializable
data class CustomHeader(val name: String, val value: String)

/** An unfilled row: nothing typed into either field. */
fun CustomHeader.isBlank(): Boolean = name.isBlank() && value.isBlank()
