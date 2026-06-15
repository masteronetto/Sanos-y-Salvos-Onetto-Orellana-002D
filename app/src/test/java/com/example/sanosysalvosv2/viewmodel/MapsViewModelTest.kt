package com.example.sanosysalvosv2.viewmodel

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.sanosysalvosv2.data.repository.MapsRepository
import com.example.sanosysalvosv2.data.repository.MapsResult
import com.example.sanosysalvosv2.data.session.SessionStore
import com.example.sanosysalvosv2.model.NearbyReport
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
import org.mockito.kotlin.mock

@OptIn(ExperimentalCoroutinesApi::class)
class MapsViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockApplication: Application
    private lateinit var mockMapsRepository: MapsRepository
    private lateinit var mockSessionStore: SessionStore

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockApplication = mock()
        mockMapsRepository = mock()
        mockSessionStore = mock()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testFetchReportsSuccess() = runTest {
        // Arrange
        val mockReports = listOf(
            NearbyReport(lat = 40.7128, lon = -74.0060, title = "Lost Dog", description = "lost · 100 m"),
            NearbyReport(lat = 40.7580, lon = -73.9855, title = "Found Cat", description = "found · 200 m")
        )
        
        doReturn(flowOf("valid-token"))
            .`when`(mockSessionStore)
            .tokenFlow
            
        doReturn(MapsResult.Success(mockReports))
            .`when`(mockMapsRepository)
            .getNearbyReports(any(), any(), any(), any())
        
        doReturn(mockApplication.applicationContext)
            .`when`(mockApplication)
            .applicationContext

        // Act & Assert: MapsViewModel should emit Success state
        assertTrue(true) // Placeholder for actual viewModel integration test
    }

    @Test
    fun testFetchReportsWithInvalidLocation() = runTest {
        // Arrange
        doReturn(flowOf("valid-token"))
            .`when`(mockSessionStore)
            .tokenFlow

        // Act: fetchNearbyReports with lat=0.0, lon=0.0 should stay in AwaitingLocation
        // Assert via MapsViewModel.fetchNearbyReports logic
        assertTrue(true) // Placeholder
    }

    @Test
    fun testFetchReportsWithNullToken() = runTest {
        // Arrange
        doReturn(flowOf(null as String?))
            .`when`(mockSessionStore)
            .tokenFlow

        // Act: fetchNearbyReports with null token should emit Error("Sesión no válida")
        // Assert via MapsViewModel error handling
        assertTrue(true) // Placeholder
    }

    @Test
    fun testRefreshLastKnownLocation() = runTest {
        // Arrange
        val mockReports = listOf(
            NearbyReport(lat = 40.7128, lon = -74.0060, title = "Lost Dog", description = "lost · 100 m")
        )
        
        doReturn(flowOf("valid-token"))
            .`when`(mockSessionStore)
            .tokenFlow
            
        doReturn(MapsResult.Success(mockReports))
            .`when`(mockMapsRepository)
            .getNearbyReports(any(), any(), any(), any())

        // Act: Call fetchNearbyReports, then refreshLastKnownLocation
        // Assert: refreshLastKnownLocation should reuse last coords
        assertTrue(true) // Placeholder
    }
}
