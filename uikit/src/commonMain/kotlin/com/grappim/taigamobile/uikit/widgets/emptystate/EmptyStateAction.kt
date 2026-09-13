package com.grappim.taigamobile.uikit.widgets.emptystate

import com.grappim.kit.uikit.NativeText
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.strings.generated.resources.refresh

data class EmptyStateAction(val label: NativeText = NativeText.Resource(RString.refresh), val onClick: () -> Unit)
