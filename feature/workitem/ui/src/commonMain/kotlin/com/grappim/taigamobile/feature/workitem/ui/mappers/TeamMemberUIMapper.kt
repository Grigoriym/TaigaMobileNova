package com.grappim.taigamobile.feature.workitem.ui.mappers

import com.grappim.taigamobile.feature.users.domain.TeamMember
import com.grappim.taigamobile.feature.workitem.ui.models.TeamMemberUI
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import org.koin.core.annotation.Factory

@Factory
class TeamMemberUIMapper {
    fun toUI(teamMember: TeamMember): TeamMemberUI = TeamMemberUI(
        id = teamMember.id,
        name = teamMember.name,
        avatarUrl = teamMember.avatarUrl
    )

    fun toUI(list: ImmutableList<TeamMember>): PersistentList<TeamMemberUI> = list.map {
        toUI(it)
    }.toPersistentList()
}
