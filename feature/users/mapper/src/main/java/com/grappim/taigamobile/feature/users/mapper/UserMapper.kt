package com.grappim.taigamobile.feature.users.mapper

import com.grappim.taigamobile.core.async.IoDispatcher
import com.grappim.taigamobile.feature.users.domain.User
import com.grappim.taigamobile.feature.users.dto.UserDTO
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UserMapper @Inject constructor(@IoDispatcher private val ioDispatcher: CoroutineDispatcher) {
    suspend fun toUser(dto: UserDTO): User = withContext(ioDispatcher) {
        User(
            id = dto.id,
            fullName = dto.fullName,
            photo = dto.photo,
            bigPhoto = dto.bigPhoto,
            username = dto.username,
            name = dto.name,
            pk = dto.pk
        )
    }
}
