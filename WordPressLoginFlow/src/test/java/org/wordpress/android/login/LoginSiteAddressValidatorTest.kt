package org.wordpress.android.login

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import org.wordpress.android.util.helpers.Debouncer
import java.util.ArrayList
import java.util.Arrays
import java.util.Optional
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
class LoginSiteAddressValidatorTest {
    @Rule @JvmField var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var debouncer: Debouncer
    private lateinit var validator: LoginSiteAddressValidator

    @Before fun setUp() {
        debouncer = mock(Debouncer::class.java)
        doAnswer {
            it.getArgument<Runnable>(1).run()
            null
        }.`when`(debouncer).debounce(any(), any(Runnable::class.java), anyLong(), any(TimeUnit::class.java))
        validator = LoginSiteAddressValidator(debouncer)
    }

    @Test fun testAnErrorIsReturnedWhenGivenAnInvalidAddress() {
        // Arrange
        assertThat(validator.errorMessageResId.value).isNull()

        // Act
        validator.setAddress("invalid")

        // Assert
        assertThat(validator.errorMessageResId.value).isNotNull
        assertThat(validator.cleanedSiteAddress).isEqualTo("invalid")
        assertThat(validator.isValid.value).isFalse
    }

    @Test fun testNoErrorIsReturnedButIsInvalidWhenGivenAnEmptyAddress() {
        // Act
        validator.setAddress("")

        // Assert
        assertThat(validator.errorMessageResId.value).isNull()
        assertThat(validator.isValid.value).isFalse
        assertThat(validator.cleanedSiteAddress).isEqualTo("")
    }

    @Test fun testTheErrorIsImmediatelyClearedWhenANewAddressIsGiven() {
        // Arrange
        val resIdValues = ArrayList<Optional<Int>>()
        validator.errorMessageResId.observeForever { resId -> resIdValues.add(Optional.ofNullable(resId)) }

        // Act
        validator.setAddress("invalid")
        validator.setAddress("another-invalid")

        // Assert
        assertThat(resIdValues).hasSize(4)
        assertThat(resIdValues[0]).isEmpty
        assertThat(resIdValues[1]).isNotEmpty
        assertThat(resIdValues[2]).isEmpty
        assertThat(resIdValues[3]).isNotEmpty
    }

    @Test fun testItReturnsValidWhenGivenValidURLs() {
        // Arrange
        val validUrls = Arrays.asList(
                "http://subdomain.example.com",
                "http://example.ca",
                "example.ca",
                "subdomain.example.com",
                "  space-with-subdomain.example.net",
                "https://subdomain.example.com/folder",
                "http://subdomain.example.com/folder/over/there ",
                "7.7.7.7",
                "http://7.7.13.45",
                "http://47.147.43.45/folder   "
        )

        // Act and Assert
        assertThat(validUrls).allSatisfy { url ->
            validator.setAddress(url!!)
            assertThat(validator.errorMessageResId.value).isNull()
            assertThat(validator.isValid.value).isTrue
        }
    }
}
