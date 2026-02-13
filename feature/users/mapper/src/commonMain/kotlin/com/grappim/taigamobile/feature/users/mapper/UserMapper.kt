package com.grappim.taigamobile.feature.users.mapper

import com.grappim.taigamobile.feature.users.domain.User
import com.grappim.taigamobile.feature.users.dto.UserDTO
import org.koin.core.annotation.Factory

@Factory
class UserMapper {
    fun toUser(dto: UserDTO): User = User(
        id = dto.id,
        fullName = dto.fullName,
        photo = dto.photo,
        bigPhoto = dto.bigPhoto,
        username = dto.username,
        name = dto.name,
        pk = dto.pk
    )
}
