package com.grappim.taigamobile.feature.wiki.domain

import com.grappim.taigamobile.core.domain.Attachment
import com.grappim.taigamobile.feature.users.domain.User
import com.grappim.taigamobile.feature.workitem.domain.wiki.WikiLink
import com.grappim.taigamobile.feature.workitem.domain.wiki.WikiPage
import kotlinx.collections.immutable.ImmutableList

data class WikiPageData(
    val page: WikiPage,
    val user: User,
    val wikiLink: WikiLink?,
    val attachments: ImmutableList<Attachment>
)
