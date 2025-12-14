package com.grappim.taigamobile.testing

import com.grappim.taigamobile.core.domain.UserDTO
import com.grappim.taigamobile.feature.users.domain.User

fun getUserDTO(): UserDTO = UserDTO(
    id = getRandomLong(),
    fullName = getRandomString(),
    photo = getRandomString(),
    bigPhoto = getRandomString(),
    username = getRandomString(),
    name = getRandomString(),
    pk = getRandomLong()
)

fun getUser(): User = User(
    id = getRandomLong(),
    fullName = getRandomString(),
    photo = getRandomString(),
    bigPhoto = getRandomString(),
    username = getRandomString(),
    name = getRandomString(),
    pk = getRandomLong()
)
