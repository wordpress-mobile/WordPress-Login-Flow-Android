package org.wordpress.android.login.webauthn

import android.content.Context
import android.os.CancellationSignal
import android.util.Log
import androidx.annotation.StringRes
import androidx.credentials.CredentialManager
import androidx.credentials.CredentialManagerCallback
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.GetPublicKeyCredentialOption
import androidx.credentials.PublicKeyCredential
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.wordpress.android.fluxc.annotations.action.Action
import org.wordpress.android.fluxc.generated.AuthenticationActionBuilder
import org.wordpress.android.fluxc.store.AccountStore.FinishWebauthnChallengePayload
import org.wordpress.android.login.R
import java.util.concurrent.Executors

class PasskeyRequest private constructor(
    context: Context,
    requestData: PasskeyRequestData,
    onSuccess: (Action<FinishWebauthnChallengePayload>) -> Unit,
    onFailure: (PasskeyError) -> Unit
) {
    init {
        val executor = Executors.newSingleThreadExecutor()
        val signal = CancellationSignal()
        val getCredRequest = GetCredentialRequest(
                listOf(GetPublicKeyCredentialOption(requestData.requestJson))
        )

        val passkeyRequestCallback = WPCredentialManagerCallback(
                onFailure = {
                    val error = handlePasskeyError(it)
                    CoroutineScope(Dispatchers.Main).launch { onFailure(error) }
                    Log.e(TAG, it.stackTraceToString())
                },
                onSuccess = { result ->
                    FinishWebauthnChallengePayload().apply {
                        mUserId = requestData.userId
                        mTwoStepNonce = requestData.twoStepNonce
                        mClientData = result.toJson().orEmpty()
                    }.let {
                        AuthenticationActionBuilder.newFinishSecurityKeyChallengeAction(it)
                    }.let(onSuccess)
                }
        )

        val credentialManager = CredentialManager.create(context)

        try {
            credentialManager.getCredentialAsync(
                    request = getCredRequest,
                    context = context,
                    cancellationSignal = signal,
                    executor = executor,
                    callback = passkeyRequestCallback
            )
        } catch (e: GetCredentialException) {
            Log.e(TAG, e.stackTraceToString())
            onFailure(handlePasskeyError(e))
        }
    }

    private fun GetCredentialResponse.toJson(): String? {
        return when (val credential = this.credential) {
            is PublicKeyCredential -> credential.authenticationResponseJson
            else -> {
                Log.e(TAG, "Unexpected type of credential")
                null
            }
        }
    }

    private fun handlePasskeyError(error: GetCredentialException): PasskeyError {
        return when (error) {
            is GetCredentialCancellationException -> PasskeyError(
                reason = ErrorType.USER_CANCELED,
                messageId = R.string.login_error_security_key
            )
            else -> PasskeyError(
                reason = ErrorType.KEY_NOT_FOUND,
                messageId = R.string.login_error_generic
            )
        }
    }

    class WPCredentialManagerCallback(
        private val onSuccess: (GetCredentialResponse) -> Unit,
        private val onFailure: (GetCredentialException) -> Unit
    ) : CredentialManagerCallback<GetCredentialResponse, GetCredentialException> {
        override fun onError(e: GetCredentialException) = onFailure(e)
        override fun onResult(result: GetCredentialResponse) = onSuccess(result)
    }

    data class PasskeyRequestData(
        val userId: String,
        val twoStepNonce: String,
        val requestJson: String
    )

    data class PasskeyError(
        val reason: ErrorType,
        @StringRes val messageId: Int
    )

    enum class ErrorType {
        USER_CANCELED,
        KEY_NOT_FOUND,
        TIMEOUT
    }

    companion object {
        private const val TAG = "PasskeyRequest"

        @JvmStatic
        fun create(
            context: Context,
            requestData: PasskeyRequestData,
            onSuccess: (Action<FinishWebauthnChallengePayload>) -> Unit,
            onFailure: (PasskeyError) -> Unit
        ) {
            PasskeyRequest(context, requestData, onSuccess, onFailure)
        }
    }
}
