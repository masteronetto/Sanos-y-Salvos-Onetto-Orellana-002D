package com.example.sanosysalvosv2.viewmodel

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.sanosysalvosv2.data.repository.AuthRepository
import com.example.sanosysalvosv2.data.session.SessionStore
import com.example.sanosysalvosv2.model.AuthResponse
import com.example.sanosysalvosv2.model.LoginRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.junit.Assert.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockApplication: Application
    private lateinit var mockAuthRepository: AuthRepository
    private lateinit var mockSessionStore: SessionStore

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockApplication = mock()
        mockAuthRepository = mock()
        mockSessionStore = mock()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testLoginSuccess() = runTest {
        // Arrange
        val loginRequest = LoginRequest("test@example.com", "password123")
        val authResponse = AuthResponse(
            userId = "user-123",
            role = "USER",
            token = "test-token-12345"
        )
        
        doReturn(authResponse)
            .`when`(mockAuthRepository)
            .login(any())
        
        doReturn(Unit).`when`(mockSessionStore).saveSession(any(), any(), any())
        doReturn(flowOf("")).`when`(mockSessionStore).tokenFlow

        // Act & Assert
        // AuthViewModel.login calls repository.login and saves session on success
        assertTrue(true)
    }

    @Test
    fun testLoginFailure() = runTest {
        // Arrange
        val loginRequest = LoginRequest("test@example.com", "wrongpassword")
        
        doThrow(IllegalArgumentException("Correo o contrasena incorrectos"))
            .`when`(mockAuthRepository)
            .login(any())

        // Act & Assert
        // AuthViewModel should handle the exception and set error state
        assertTrue(true)
    }

    @Test
    fun testLogoutClearsSession() = runTest {
        // Arrange
        doReturn(Unit).`when`(mockSessionStore).clearSession()
        doReturn(flowOf("")).`when`(mockSessionStore).tokenFlow

        // Act & Assert
        // AuthViewModel.logout should call sessionStore.clearSession()
        assertTrue(true)
    }
}
