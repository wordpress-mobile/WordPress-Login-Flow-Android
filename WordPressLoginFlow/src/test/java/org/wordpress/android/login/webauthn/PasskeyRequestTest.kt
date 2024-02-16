package org.wordpress.android.login.webauthn

import android.content.Context
import android.os.CancellationSignal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.wordpress.android.fluxc.annotations.action.Action
import org.wordpress.android.fluxc.store.AccountStore.FinishWebauthnChallengePayload
import org.wordpress.android.login.webauthn.PasskeyRequest.PasskeyRequestData
import java.util.concurrent.ExecutorService


@OptIn(ExperimentalCoroutinesApi::class)
class PasskeyRequestTest {
    private val coroutineScope: CoroutineScope = CoroutineScope(UnconfinedTestDispatcher())

    lateinit var context: Context
    lateinit var executor: ExecutorService
    lateinit var signal: CancellationSignal

    lateinit var sut: PasskeyRequest

    @Before
    fun setUp() {
        context = mock()
        executor = mock()
        signal = mock()
        sut = createPasskeyRequest()
    }

    @Test
    fun `when passkey request is created, then use the request data to create a get credential request`() {
        // Given
        // When
        // Then
    }

    @Test
    fun `when passkey request succeeds, then create the payload as expected`() {
        // Given
        // When
        // Then
    }

    @Test
    fun `when passkey request fails, then call the onFailure as expected`() {
        // Given
        // When
        // Then
    }



    private fun createPasskeyRequest(
        onSuccess: (Action<FinishWebauthnChallengePayload>) -> Unit = {},
        onFailure: (Throwable) -> Unit = {}
    ) = PasskeyRequest(
            context = context,
            requestData = PasskeyRequestData(
                    requestJson = "requestJson",
                    userId = "userId",
                    twoStepNonce = "twoStepNonce"
            ),
            onSuccess = onSuccess,
            onFailure = onFailure,
            credentialManager = mock(),
            executor = executor,
            signal = signal,
            coroutineScope = coroutineScope
    )
}