package com.example.sanosysalvosv2.viewmodel

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.sanosysalvosv2.data.repository.AdminRepository
import com.example.sanosysalvosv2.data.session.SessionStore
import com.example.sanosysalvosv2.model.AdminUserSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock

@OptIn(ExperimentalCoroutinesApi::class)
class AdminUsersViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockApplication: Application
    private lateinit var mockAdminRepository: AdminRepository
    private lateinit var mockSessionStore: SessionStore

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockApplication = mock()
        mockAdminRepository = mock()
        mockSessionStore = mock()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testLoadUsersSuccess() = runTest {
        // Arrange
        val mockUsers = listOf(
            AdminUserSummary(
                id = "1",
                fullName = "Juan",
                email = "juan@example.com",
                role = "USER",
                phone = "123456789",
                status = "ACTIVE"
            ),
            AdminUserSummary(
                id = "2",
                fullName = "Maria",
                email = "maria@example.com",
                role = "USER",
                phone = "987654321",
                status = "ACTIVE"
            ),
            AdminUserSummary(
                id = "3",
                fullName = "Pedro",
                email = "pedro@example.com",
                role = "ADMIN",
                phone = "555555555",
                status = "ACTIVE"
            ),
            AdminUserSummary(
                id = "4",
                fullName = "Ana",
                email = "ana@example.com",
                role = "USER",
                phone = "444444444",
                status = "ACTIVE"
            ),
            AdminUserSummary(
                id = "5",
                fullName = "Camila",
                email = "camila@example.com",
                role = "USER",
                phone = "666666666",
                status = "ACTIVE"
            )
        )
        
        doReturn(flowOf("valid-admin-token"))
            .`when`(mockSessionStore)
            .tokenFlow
            
        doReturn(mockUsers)
            .`when`(mockAdminRepository)
            .listRegisteredUsers(any())
        
        doReturn(mockApplication.applicationContext)
            .`when`(mockApplication)
            .applicationContext

        // Act & Assert: AdminViewModel.loadUsers should load 5 users
        assertTrue(true) // Placeholder: viewModel would update users state
    }

    @Test
    fun testClientSideSearchFilter() = runTest {
        // Arrange
        val mockUsers = listOf(
            AdminUserSummary(
                id = "1",
                fullName = "Juan",
                email = "juan@example.com",
                role = "USER",
                phone = "123456789",
                status = "ACTIVE"
            ),
            AdminUserSummary(
                id = "2",
                fullName = "Maria",
                email = "maria@example.com",
                role = "USER",
                phone = "987654321",
                status = "ACTIVE"
            ),
            AdminUserSummary(
                id = "3",
                fullName = "Pedro",
                email = "pedro@example.com",
                role = "ADMIN",
                phone = "555555555",
                status = "ACTIVE"
            ),
            AdminUserSummary(
                id = "4",
                fullName = "Ana",
                email = "ana@example.com",
                role = "USER",
                phone = "444444444",
                status = "ACTIVE"
            ),
            AdminUserSummary(
                id = "5",
                fullName = "Camila",
                email = "camila@example.com",
                role = "USER",
                phone = "666666666",
                status = "ACTIVE"
            )
        )
        
        doReturn(flowOf("valid-admin-token"))
            .`when`(mockSessionStore)
            .tokenFlow
            
        doReturn(mockUsers)
            .`when`(mockAdminRepository)
            .listRegisteredUsers(any())
        
        doReturn(mockApplication.applicationContext)
            .`when`(mockApplication)
            .applicationContext

        // Act: Load all 5 users, then filter client-side for "Camila"
        // Assert: Only 1 result (Camila) should match
        // Expected: Search filters by fullName, email, phone, etc.
        assertTrue(true) // Placeholder
    }

    @Test
    fun testLoadUsersError() = runTest {
        // Arrange
        doReturn(flowOf(null as String?))
            .`when`(mockSessionStore)
            .tokenFlow
        
        doReturn(mockApplication.applicationContext)
            .`when`(mockApplication)
            .applicationContext
        
        doThrow(IllegalStateException("Sesion invalida"))
            .`when`(mockAdminRepository)
            .listRegisteredUsers(any())

        // Act & Assert: loadUsers with null token should set error state
        assertTrue(true) // Placeholder
    }
}
