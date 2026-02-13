package com.grappim.taigamobile.composeapp.data

//@Factory
//class ImageLoaderProvider(
//    private val appInfoProvider: AppInfoProvider,
//    private val debugLocalHostImageManager: DebugLocalHostImageManager,
//    private val context: Context
//) {
//
//    fun provide(): ImageLoader = ImageLoader.Builder(context)
//        .apply {
//            if (appInfoProvider.isDebug()) {
//                components {
//                    if (appInfoProvider.getDebugLocalHost().isNotEmpty()) {
//                        add(
//                            OkHttpNetworkFetcherFactory(
//                                OkHttpClient.Builder()
//                                    .addInterceptor(debugLocalHostImageManager)
//                                    .build()
//                            )
//                        )
//                    }
//                }
//                logger(DebugLogger())
//            }
//        }
//        .crossfade(true)
//        .build()
//}
