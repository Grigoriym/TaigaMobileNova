package com.grappim.taigamobile.commontask.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.grappim.taigamobile.core.domain.CustomField
import com.grappim.taigamobile.core.domain.CustomFieldValue
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.EditActions
import com.grappim.taigamobile.uikit.widgets.loader.DotsLoader
import com.grappim.taigamobile.uikit.widgets.text.SectionTitle

@Suppress("FunctionName")
fun LazyListScope.CommonTaskCustomFields(
    customFields: List<CustomField>,
    customFieldsValues: Map<Long, CustomFieldValue?>,
    onValueChange: (Long, CustomFieldValue?) -> Unit,
    editActions: EditActions
) {
    item {
        SectionTitle(text = stringResource(RString.custom_fields))
    }

    itemsIndexed(customFields) { index, item ->
        CustomField(
            customField = item,
            value = customFieldsValues[item.id],
            onValueChange = { onValueChange(item.id, it) },
            onSaveClick = {
                editActions.editCustomField.select(
                    Pair(
                        item,
                        customFieldsValues[item.id]
                    )
                )
            }
        )

        if (index < customFields.lastIndex) {
            HorizontalDivider(
                modifier = Modifier.padding(top = 16.dp, bottom = 12.dp),
                thickness = DividerDefaults.Thickness,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }

    item {
        if (editActions.editCustomField.isLoading) {
            Spacer(Modifier.height(8.dp))
            DotsLoader()
        }
    }
}
