package com.grappim.taigamobile.testing

import com.grappim.taigamobile.core.domain.User
import com.grappim.taigamobile.core.domain.UserDTO

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
