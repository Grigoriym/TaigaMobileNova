package com.grappim.taigamobile.core.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath
import java.io.File

/**
 * A **real** [DataStore] backed by a fresh temp file, following the precedent set by
 * `TrustedCertStorageImplTest`.
 *
 * These tests therefore assert the behaviour of the storage classes against the *JVM* DataStore
 * actual — which is the same `datastore-preferences-core` implementation Android uses, but is
 * **not** the iOS one. Nothing platform-specific is being verified here; the storage classes
 * themselves live entirely in `commonMain`. The reason for `jvmTest` over `commonTest` is simply
 * that `PreferenceDataStoreFactory.createWithPath` needs a filesystem path, and a hand-written
 * in-memory `DataStore` would test the fake instead of the serialization/read-modify-write
 * behaviour these classes actually depend on.
 */
internal fun createTestDataStore(name: String): DataStore<Preferences> {
    val file = File.createTempFile(name, ".preferences_pb")
    file.deleteOnExit()
    return PreferenceDataStoreFactory.createWithPath(produceFile = { file.absolutePath.toPath() })
}
