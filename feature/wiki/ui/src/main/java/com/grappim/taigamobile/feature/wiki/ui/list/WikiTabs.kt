package com.grappim.taigamobile.feature.wiki.ui.list

import androidx.annotation.StringRes
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.utils.Tab

internal enum class WikiTabs(@StringRes override val titleId: Int) : Tab {
    Bookmarks(RString.bookmarks),
    AllWikiPages(RString.all_wiki_pages)
}
