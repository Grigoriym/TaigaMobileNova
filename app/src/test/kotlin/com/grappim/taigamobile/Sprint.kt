package com.grappim.taigamobile

import java.time.LocalDate

class Sprint(
    val name: String,
    val start: LocalDate,
    val end: LocalDate,
    val creator: User,
    val tasks: List<Task> = emptyList()
)