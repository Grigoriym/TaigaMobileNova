package com.grappim.taigamobile.feature.login.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.grappim.taigamobile.feature.login.domain.model.AuthType
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.theme.TaigaMobileTheme
import com.grappim.taigamobile.uikit.utils.PreviewTaigaDarkLight
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.dialog.ConfirmActionDialog
import com.grappim.taigamobile.utils.ui.NativeText

@Composable
fun LoginScreen(
    onShowSnackbar: (message: NativeText) -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isLoginSuccessful by viewModel.loginSuccessful.collectAsStateWithLifecycle(false)

    LaunchedEffect(state.error) {
        if (state.error.isNotEmpty()) {
            onShowSnackbar(state.error)
        }
    }
    LaunchedEffect(isLoginSuccessful) {
        if (isLoginSuccessful) {
            onLoginSuccess()
        }
    }

    ConfirmActionDialog(
        title = stringResource(RString.login_alert_title),
        description = stringResource(RString.login_alert_text),
        onConfirm = state.onActionDialogConfirm,
        onDismiss = { state.setIsAlertVisible(false) },
        iconId = RDrawable.ic_insecure,
        isVisible = state.isAlertVisible
    )

    LoginScreenContent(
        state = state,
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun LoginScreenContent(state: LoginState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Image(
            painter = painterResource(RDrawable.taiga_mobile_logo),
            contentDescription = null,
            modifier = Modifier
                .size(130.dp)
                .padding(bottom = 8.dp)
        )

        Text(
            text = stringResource(RString.app_name),
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(50.dp))

        LoginTextField(
            value = state.server,
            labelId = RString.login_taiga_server,
            onValueChange = state.onServerValueChange,
            isError = state.isServerInputError
        )

        LoginTextField(
            value = state.login,
            labelId = RString.login_username,
            onValueChange = state.onLoginValueChange,
            isError = state.isLoginInputError,
            contentType = ContentType.Username
        )

        LoginTextField(
            value = state.password,
            labelId = RString.login_password,
            onValueChange = state.onPasswordValueChange,
            visualTransformation = if (state.isPasswordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardType = KeyboardType.Password,
            isError = state.isPasswordInputError,
            contentType = ContentType.Password,
            trailingIcon = {
                if (state.isPasswordVisible) {
                    IconButton(onClick = { state.setIsPasswordVisible(false) }) {
                        Icon(
                            imageVector = Icons.Filled.Visibility,
                            contentDescription = "hide password"
                        )
                    }
                } else {
                    IconButton(
                        onClick = { state.setIsPasswordVisible(true) }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.VisibilityOff,
                            contentDescription = "show password"
                        )
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            Button(
                onClick = {
                    state.validateAuthData(AuthType.NORMAL)
                },
                contentPadding = PaddingValues(horizontal = 40.dp)
            ) {
                Text(stringResource(RString.login_continue))
            }

            Spacer(Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    state.validateAuthData(AuthType.LDAP)
                }
            ) {
                Text(stringResource(RString.login_ldap))
            }
        }
    }
}

@Composable
fun LoginTextField(
    value: TextFieldValue,
    @StringRes labelId: Int,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null,
    contentType: ContentType? = null
) {
    val focusManager = LocalFocusManager.current

    val textStyle = MaterialTheme.typography.titleMedium.merge(
        TextStyle(fontWeight = FontWeight.Normal)
    )

    val autofillModifier = if (contentType != null) {
        Modifier.semantics { this.contentType = contentType }
    } else {
        Modifier
    }

    OutlinedTextField(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp)
            .padding(bottom = 6.dp)
            .then(autofillModifier),
        value = value,
        onValueChange = onValueChange,
        textStyle = textStyle,
        singleLine = true,
        label = { Text(text = stringResource(labelId), style = textStyle) },
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        isError = isError,
        trailingIcon = trailingIcon,
        shape = MaterialTheme.shapes.small,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedTextColor = LocalContentColor.current,
            focusedTextColor = LocalContentColor.current,
            cursorColor = MaterialTheme.colorScheme.primary,
            errorCursorColor = MaterialTheme.colorScheme.error,
            unfocusedContainerColor = MaterialTheme.colorScheme.background,
            focusedContainerColor = MaterialTheme.colorScheme.background,
            errorContainerColor = MaterialTheme.colorScheme.background,
            focusedBorderColor = MaterialTheme.colorScheme.primary.copy(
                alpha = MaterialTheme.colorScheme.onSurface.alpha
            ),
            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(
                alpha = MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.38f
                ).alpha
            ),
            errorBorderColor = MaterialTheme.colorScheme.error,
            errorTrailingIconColor = MaterialTheme.colorScheme.error,
            focusedLabelColor = MaterialTheme.colorScheme.primary.copy(
                alpha = MaterialTheme.colorScheme.onSurface.alpha
            ),
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(
                alpha = MaterialTheme.colorScheme.onSurfaceVariant.alpha
            ),
            errorLabelColor = MaterialTheme.colorScheme.error,
            focusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(
                alpha = MaterialTheme.colorScheme.onSurfaceVariant.alpha
            ),
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(
                alpha = MaterialTheme.colorScheme.onSurfaceVariant.alpha
            )
        )
    )
}

@[Composable PreviewTaigaDarkLight]
private fun LoginScreenPreview() {
    TaigaMobileTheme {
        LoginScreenContent(
            state = LoginState(
                server = TextFieldValue("server"),
                onServerValueChange = {},
                isServerInputError = false,
                login = TextFieldValue(),
                onLoginValueChange = {},
                isLoginInputError = false,
                password = TextFieldValue(),
                onPasswordValueChange = {},
                isPasswordInputError = false,
                isAlertVisible = false,
                setIsAlertVisible = {},
                onActionDialogConfirm = {},
                validateAuthData = {},
                authType = AuthType.NORMAL,
                onAuthTypeChange = {},
                setIsPasswordVisible = {}
            )
        )
    }
}

@[Composable PreviewTaigaDarkLight]
private fun LoginScreenErrorsPreview() {
    TaigaMobileTheme {
        LoginScreenContent(
            state = LoginState(
                server = TextFieldValue("server"),
                onServerValueChange = {},
                isServerInputError = true,
                login = TextFieldValue(),
                onLoginValueChange = {},
                isLoginInputError = true,
                password = TextFieldValue(),
                onPasswordValueChange = {},
                isPasswordInputError = true,
                isAlertVisible = false,
                setIsAlertVisible = {},
                onActionDialogConfirm = {},
                validateAuthData = {},
                authType = AuthType.NORMAL,
                onAuthTypeChange = {},
                setIsPasswordVisible = {}
            )
        )
    }
}

@[Composable PreviewTaigaDarkLight]
private fun LoginScreenAlertPreview() {
    TaigaMobileTheme {
        LoginScreenContent(
            state = LoginState(
                server = TextFieldValue("server"),
                onServerValueChange = {},
                isServerInputError = true,
                login = TextFieldValue(),
                onLoginValueChange = {},
                isLoginInputError = true,
                password = TextFieldValue(),
                onPasswordValueChange = {},
                isPasswordInputError = true,
                isAlertVisible = true,
                setIsAlertVisible = {},
                onActionDialogConfirm = {},
                validateAuthData = {},
                authType = AuthType.NORMAL,
                onAuthTypeChange = {},
                setIsPasswordVisible = {}
            )
        )
    }
}
