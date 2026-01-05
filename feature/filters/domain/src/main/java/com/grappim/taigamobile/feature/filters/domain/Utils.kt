package com.grappim.taigamobile.feature.filters.domain

import com.grappim.taigamobile.feature.filters.domain.model.filters.Filters
import com.grappim.taigamobile.feature.filters.domain.model.filters.TagFilters

fun List<Filters>.commaString() = map { it.id }
    .joinToString(separator = ",")
    .takeIf { it.isNotEmpty() }

fun List<TagFilters>.tagsCommaString() = joinToString(separator = ",") { it.name.replace(" ", "+") }
    .takeIf { it.isNotEmpty() }

fun List<Filters>.hasData() = any { it.count > 0 }
