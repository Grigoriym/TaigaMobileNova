package com.grappim.taigamobile.core.api

import com.grappim.taigamobile.core.async.IoDispatcher
import com.grappim.taigamobile.core.domain.CustomAttributeResponseDTO
import com.grappim.taigamobile.core.domain.CustomAttributesValuesResponseDTO
import com.grappim.taigamobile.core.domain.CustomField
import com.grappim.taigamobile.core.domain.CustomFieldType
import com.grappim.taigamobile.core.domain.CustomFieldValue
import com.grappim.taigamobile.core.domain.CustomFields
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CustomFieldsMapper @Inject constructor(@IoDispatcher private val ioDispatcher: CoroutineDispatcher) {

    suspend fun toDomain(
        attributes: List<CustomAttributeResponseDTO>,
        values: CustomAttributesValuesResponseDTO
    ): CustomFields = withContext(ioDispatcher) {
        CustomFields(
            version = values.version,
            fields = attributes.sortedBy { it.order }
                .map { toDomain(values = values, resp = it) }
        )
    }

    suspend fun toDomain(values: CustomAttributesValuesResponseDTO, resp: CustomAttributeResponseDTO): CustomField =
        withContext(ioDispatcher) {
            CustomField(
                id = resp.id,
                type = resp.type,
                name = resp.name,
                description = resp.description?.takeIf { it.isNotEmpty() },
                value = values.attributesValues[resp.id]?.let { value ->
                    CustomFieldValue(
                        when (resp.type) {
                            CustomFieldType.Date -> (value as? String)?.takeIf {
                                it.isNotEmpty()
                            }?.toLocalDate()

                            CustomFieldType.Checkbox -> value as? Boolean
                            else -> value
                        } ?: return@let null
                    )
                },
                options = resp.extra.orEmpty()
            )
        }
}
