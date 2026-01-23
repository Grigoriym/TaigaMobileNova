package com.grappim.taigamobile.feature.workitem.mapper

import com.grappim.taigamobile.core.async.IoDispatcher
import com.grappim.taigamobile.feature.workitem.domain.customfield.CustomField
import com.grappim.taigamobile.feature.workitem.domain.customfield.CustomFieldType
import com.grappim.taigamobile.feature.workitem.domain.customfield.CustomFieldValue
import com.grappim.taigamobile.feature.workitem.domain.customfield.CustomFields
import com.grappim.taigamobile.feature.workitem.dto.customattribute.CustomAttributeResponseDTO
import com.grappim.taigamobile.feature.workitem.dto.customattribute.CustomAttributesValuesResponseDTO
import com.grappim.taigamobile.feature.workitem.dto.customfield.CustomFieldTypeDTO
import com.grappim.taigamobile.utils.formatter.datetime.DateTimeUtils
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import javax.inject.Inject

class CustomFieldsMapper @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val dateTimeUtils: DateTimeUtils
) {

    suspend fun toDomain(
        attributes: List<CustomAttributeResponseDTO>,
        values: CustomAttributesValuesResponseDTO
    ): CustomFields = withContext(ioDispatcher) {
        CustomFields(
            version = values.version,
            fields = attributes.sortedBy { it.order }
                .map { toDomain(values = values, resp = it) }
                .toImmutableList()
        )
    }

    suspend fun toDomain(values: CustomAttributesValuesResponseDTO, resp: CustomAttributeResponseDTO): CustomField =
        withContext(ioDispatcher) {
            CustomField(
                id = resp.id,
                type = toDomainType(resp.type),
                name = resp.name,
                description = resp.description?.takeIf { it.isNotEmpty() },
                value = values.attributesValues[resp.id.toString()]?.let { jsonElement ->
                    val jsonPrimitive = jsonElement as? JsonPrimitive ?: return@let null
                    CustomFieldValue(
                        when (resp.type) {
                            CustomFieldTypeDTO.Date -> jsonPrimitive.contentOrNull?.takeIf {
                                it.isNotEmpty()
                            }?.let { dateTimeUtils.parseToLocalDate(it) }

                            CustomFieldTypeDTO.Checkbox -> jsonPrimitive.booleanOrNull
                            CustomFieldTypeDTO.Number -> jsonPrimitive.doubleOrNull
                            else -> jsonPrimitive.contentOrNull
                        } ?: return@let null
                    )
                },
                options = resp.extra.orEmpty().toImmutableList()
            )
        }

    private fun toDomainType(dto: CustomFieldTypeDTO): CustomFieldType = when (dto) {
        CustomFieldTypeDTO.Text -> CustomFieldType.Text
        CustomFieldTypeDTO.Multiline -> CustomFieldType.Multiline
        CustomFieldTypeDTO.RichText -> CustomFieldType.RichText
        CustomFieldTypeDTO.Date -> CustomFieldType.Date
        CustomFieldTypeDTO.Url -> CustomFieldType.Url
        CustomFieldTypeDTO.Dropdown -> CustomFieldType.Dropdown
        CustomFieldTypeDTO.Number -> CustomFieldType.Number
        CustomFieldTypeDTO.Checkbox -> CustomFieldType.Checkbox
    }
}
