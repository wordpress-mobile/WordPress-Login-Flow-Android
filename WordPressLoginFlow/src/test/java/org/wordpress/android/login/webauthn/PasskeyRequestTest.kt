package org.wordpress.android.login.webauthn

import android.content.Context
import android.os.CancellationSignal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import java.util.concurrent.ExecutorService


@OptIn(ExperimentalCoroutinesApi::class)
class PasskeyRequestTest {
    lateinit var context: Context
    lateinit var executor: ExecutorService
    lateinit var signal: CancellationSignal
    private val coroutineScope: CoroutineScope = CoroutineScope(UnconfinedTestDispatcher())

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
}