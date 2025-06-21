package com.grappim.taigamobile.core.api

import javax.inject.Qualifier

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class AuthRetrofit

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class AuthOkHttp

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class CommonRetrofit

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class CommonOkHttp
