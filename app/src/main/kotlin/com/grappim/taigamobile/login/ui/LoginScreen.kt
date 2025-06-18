package com.grappim.taigamobile.login.ui

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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.grappim.taigamobile.R
import com.grappim.taigamobile.core.ui.NativeText
import com.grappim.taigamobile.core.ui.asString
import com.grappim.taigamobile.core.ui.collectSnackbarMessage
import com.grappim.taigamobile.domain.entities.AuthType
import com.grappim.taigamobile.ui.components.dialogs.ConfirmActionDialog
import com.grappim.taigamobile.ui.theme.TaigaMobileTheme
import com.grappim.taigamobile.ui.utils.ThemePreviews

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onShowSnackbar: (message: String) -> Unit,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.loginState.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackBarMessage.collectSnackbarMessage()
    val isLoginSuccessful by viewModel.loginSuccessful.collectAsStateWithLifecycle(false)

    LaunchedEffect(snackbarMessage) {
        if (snackbarMessage !is NativeText.Empty) {
            onShowSnackbar(snackbarMessage.asString(context))
        }
    }
    LaunchedEffect(isLoginSuccessful) {
        if (isLoginSuccessful) {
            onLoginSuccess()
        }
    }

    LoginScreenContent(
        state = state,
        isLoadingValue = state.isLoading,
    )
}

@Composable
fun LoginScreenContent(
    state: LoginState,
    isLoadingValue: Boolean = false,
) = ConstraintLayout(
    modifier = Modifier.fillMaxSize(),
) {
    val (logo, loginForm, button) = createRefs()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .constrainAs(logo) {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                top.linkTo(parent.top)
                bottom.linkTo(loginForm.top)
            }
            .imePadding()
            .padding(bottom = 24.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.ic_taiga_tree),
            contentDescription = null,
            modifier = Modifier
                .size(130.dp)
                .padding(bottom = 8.dp)
        )

        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineSmall,
        )
    }

    Column(
        modifier = Modifier
            .constrainAs(loginForm) {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
            }
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LoginTextField(
            value = state.server,
            labelId = R.string.login_taiga_server,
            onValueChange = state.onServerValueChange,
            isError = state.isServerInputError
        )

        LoginTextField(
            value = state.login,
            labelId = R.string.login_username,
            onValueChange = state.onLoginValueChange,
            isError = state.isLoginInputError
        )

        LoginTextField(
            value = state.password,
            labelId = R.string.login_password,
            onValueChange = state.onPasswordValueChange,
            visualTransformation = PasswordVisualTransformation(),
            keyboardType = KeyboardType.Password,
            isError = state.isPasswordInputError
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.constrainAs(button) {
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            top.linkTo(loginForm.bottom, 24.dp)
        }
    ) {
        if (state.isAlertVisible) {
            ConfirmActionDialog(
                title = stringResource(R.string.login_alert_title),
                text = stringResource(R.string.login_alert_text),
                onConfirm = state.onActionDialogConfirm,
                onDismiss = { state.setIsAlertVisible(false) },
                iconId = R.drawable.ic_insecure
            )
        }

        if (isLoadingValue) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            Button(
                onClick = {
                    state.onLoginContinue(AuthType.NORMAL)
                },
                contentPadding = PaddingValues(horizontal = 40.dp)
            ) {
                Text(stringResource(R.string.login_continue))
            }

            Spacer(Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    state.onLoginContinue(AuthType.LDAP)
                }
            ) {
                Text(stringResource(R.string.login_ldap))
            }
        }
    }
}

@Composable
fun LoginTextField(
    value: TextFieldValue,
    @StringRes labelId: Int,
    onValueChange: (TextFieldValue) -> Unit,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false
) {
    val focusManager = LocalFocusManager.current

    val textStyle = MaterialTheme.typography.titleMedium.merge(
        TextStyle(fontWeight = FontWeight.Normal)
    )
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp)
            .padding(bottom = 6.dp),
        textStyle = textStyle,
        singleLine = true,
        label = { Text(text = stringResource(labelId), style = textStyle) },
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        isError = isError,
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
            ),
        )
    )
}

@[Composable ThemePreviews]
fun LoginScreenPreview() = TaigaMobileTheme {
    LoginScreenContent(
        state = LoginState(
            server = TextFieldValue(),
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
            onLoginContinue = {},
            authType = AuthType.NORMAL,
            onAuthTypeChange = {},
            onLogin = {},
        ),
        isLoadingValue = false,
    )
}
