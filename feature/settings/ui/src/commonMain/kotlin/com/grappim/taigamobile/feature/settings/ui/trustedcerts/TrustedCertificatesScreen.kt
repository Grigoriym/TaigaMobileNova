package com.grappim.taigamobile.feature.settings.ui.trustedcerts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.grappim.taigamobile.core.domain.PendingCertTrust
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.strings.generated.resources.revoke_cert_text
import com.grappim.taigamobile.strings.generated.resources.revoke_cert_title
import com.grappim.taigamobile.strings.generated.resources.settings_trusted_certificates
import com.grappim.taigamobile.strings.generated.resources.trusted_cert_fingerprint
import com.grappim.taigamobile.strings.generated.resources.trusted_cert_issuer
import com.grappim.taigamobile.strings.generated.resources.trusted_cert_valid_until
import com.grappim.taigamobile.uikit.generated.resources.ic_delete
import com.grappim.taigamobile.uikit.theme.TaigaMobilePreviewTheme
import com.grappim.taigamobile.uikit.utils.PreviewTaigaDarkLight
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.dialog.ConfirmActionDialog
import com.grappim.taigamobile.uikit.widgets.emptystate.EmptyStateWidget
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.NavigationIconConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.utils.ui.NativeText
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TrustedCertificatesScreen(viewModel: TrustedCertificatesViewModel = koinViewModel()) {
    val topBarController = LocalTopBarConfig.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        topBarController.update(
            TopBarConfig(
                title = NativeText.Resource(RString.settings_trusted_certificates),
                navigationIcon = NavigationIconConfig.Back()
            )
        )
    }

    state.entryToRevoke?.let { entryToRevoke ->
        ConfirmActionDialog(
            isVisible = state.isRevokeDialogVisible,
            title = stringResource(RString.revoke_cert_title),
            description = stringResource(RString.revoke_cert_text, entryToRevoke.host),
            onConfirm = { state.revokeEntry() },
            onDismiss = { state.closeRevokeDialog() }
        )
    }

    TrustedCertificatesScreenContent(state = state)
}

@Composable
private fun TrustedCertificatesScreenContent(state: TrustedCertificatesState) {
    if (state.entries.isEmpty()) {
        EmptyStateWidget()
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(state.entries) { entry ->
                TrustedCertItemWidget(entry = entry, onRevokeClick = state.onRevokeClick)
            }
        }
    }
}

@Composable
private fun TrustedCertItemWidget(entry: PendingCertTrust, onRevokeClick: (PendingCertTrust) -> Unit) {
    ListItem(
        headlineContent = { Text(entry.host) },
        supportingContent = {
            Column {
                Text(stringResource(RString.trusted_cert_issuer, entry.issuer))
                Text(stringResource(RString.trusted_cert_valid_until, entry.notAfter))
                Text(
                    text = stringResource(RString.trusted_cert_fingerprint, entry.sha256Fingerprint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        trailingContent = {
            IconButton(onClick = { onRevokeClick(entry) }) {
                Icon(
                    painter = painterResource(RDrawable.ic_delete),
                    contentDescription = "Revoke",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    )
}

private fun previewCertTrust(host: String, sha256Fingerprint: String) = PendingCertTrust(
    host = host,
    subject = "CN=$host",
    issuer = "CN=Home Lab Test CA",
    notBefore = "2026-01-01",
    notAfter = "2027-01-01",
    sha256Fingerprint = sha256Fingerprint
)

@PreviewTaigaDarkLight
@Composable
private fun TrustedCertificatesScreenContentPreview() {
    TaigaMobilePreviewTheme {
        TrustedCertificatesScreenContent(
            state = TrustedCertificatesState(
                entries = persistentListOf(
                    previewCertTrust(host = "taiga.example.com", sha256Fingerprint = "AA:BB:CC:DD"),
                    previewCertTrust(host = "192.168.1.50", sha256Fingerprint = "11:22:33:44")
                )
            )
        )
    }
}

@PreviewTaigaDarkLight
@Composable
private fun TrustedCertificatesScreenEmptyPreview() {
    TaigaMobilePreviewTheme {
        TrustedCertificatesScreenContent(state = TrustedCertificatesState())
    }
}
