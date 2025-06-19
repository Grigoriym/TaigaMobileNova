package com.grappim.taigamobile.core.api

/**
 * We don't need to provide the auth token for the requests with this annotation, like auth
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequestWithoutAuthToken
