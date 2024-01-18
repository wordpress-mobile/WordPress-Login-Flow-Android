package org.wordpress.android.login.webauthn

import android.content.Context
import android.os.CancellationSignal
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.credentials.CredentialManager
import androidx.credentials.CredentialManagerCallback
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.GetPasswordOption
import androidx.credentials.GetPublicKeyCredentialOption
import androidx.credentials.exceptions.GetCredentialException
import java.util.concurrent.Executors

class CredentialManagerHandler(
    private val context: Context
) {
    private val credentialManager = CredentialManager.create(context)
    private val executor = Executors.newSingleThreadExecutor()

    @RequiresApi(34)
    fun fetchPasskey(
        context: Context,
        requestJson: String,
        onResult: (Result<GetCredentialResponse>) -> Unit
    ) {
        val password = GetPasswordOption()
        val publicKeyCred = GetPublicKeyCredentialOption(requestJson)
        val getCredRequest = GetCredentialRequest(
                listOf(password, publicKeyCred)
        )

        val signal = CancellationSignal()

        try {
            credentialManager.getCredentialAsync(
                    request = getCredRequest,
                    context = context,
                    cancellationSignal = signal,
                    executor = executor,
                    callback = object : CredentialManagerCallback<GetCredentialResponse, GetCredentialException> {
                        override fun onError(e: GetCredentialException) {
                            onResult(Result.failure(e))
                        }

                        override fun onResult(result: GetCredentialResponse) {
                            onResult(Result.success(result))
                        }
                    }
            )
        } catch (e: GetCredentialException) {
            Log.e("Error", e.stackTraceToString())
            onResult(Result.failure(e))
        }
    }
}
