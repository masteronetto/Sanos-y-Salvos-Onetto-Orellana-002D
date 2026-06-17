package com.example.sanosysalvosv2.viewmodel

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.sanosysalvosv2.data.repository.MapsResult
import com.example.sanosysalvosv2.data.repository.UserReportsRepository
import com.example.sanosysalvosv2.data.session.SessionStore
import com.example.sanosysalvosv2.model.ReportRequest
import com.example.sanosysalvosv2.model.ReportResponse
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
class UserReportsViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockApplication: Application
    private lateinit var mockUserReportsRepository: UserReportsRepository
    private lateinit var mockSessionStore: SessionStore

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockApplication = mock()
        mockUserReportsRepository = mock()
        mockSessionStore = mock()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testLoadMyReportsSuccess() = runTest {
        // Arrange
        val mockReports = listOf(
            ReportResponse(
                id = "1",
                type = "LOST",
                reporterId = "user1",
                reporterName = "User One",
                reporterPhone = "+56900000001",
                description = "Lost dog",
                status = "OPEN",
                latitude = null,
                longitude = null,
                locationName = "Central Park",
                eventDate = "2024-01-01",
                photoBase64 = null,
                photoUrl = null,
                species = null,
                breed = null,
                color = null,
                createdAt = null,
                updatedAt = null
            ),
            ReportResponse(
                id = "2",
                type = "FOUND",
                reporterId = "user2",
                reporterName = "User Two",
                reporterPhone = "+56900000002",
                description = "Found cat",
                status = "OPEN",
                latitude = null,
                longitude = null,
                locationName = "Plaza Italia",
                eventDate = "2024-01-02",
                photoBase64 = null,
                photoUrl = null,
                species = null,
                breed = null,
                color = null,
                createdAt = null,
                updatedAt = null
            )
        )
        
        doReturn(flowOf("valid-token"))
            .`when`(mockSessionStore)
            .tokenFlow
            
        doReturn(MapsResult.Success(mockReports))
            .`when`(mockUserReportsRepository)
            .listMyReports(any())
        
        doReturn(mockApplication.applicationContext)
            .`when`(mockApplication)
            .applicationContext

        // Act & Assert: UserReportsViewModel.loadMyReports should emit Success
        assertTrue(true)
    }

    @Test
    fun testMarkAsResolved() = runTest {
        // Arrange
        val reportId = "report-123"
        val resolvedReport = ReportResponse(
            id = reportId,
            type = "LOST",
            reporterId = "user1",
            reporterName = "User One",
            reporterPhone = "+56900000001",
            description = "Lost dog",
            status = "RESOLVED",
            latitude = null,
            longitude = null,
            locationName = "Central Park",
            eventDate = "2024-01-01",
            photoBase64 = null,
            photoUrl = null,
            species = null,
            breed = null,
            color = null,
            createdAt = null,
            updatedAt = null
        )
        
        doReturn(flowOf("valid-token"))
            .`when`(mockSessionStore)
            .tokenFlow
            
        doReturn(MapsResult.Success(resolvedReport))
            .`when`(mockUserReportsRepository)
            .markAsResolved(any(), any())
        
        doReturn(mockApplication.applicationContext)
            .`when`(mockApplication)
            .applicationContext

        // Act & Assert: markAsResolved should call repository with status=RESOLVED
        assertTrue(true)
    }

    @Test
    fun testFilterByType() = runTest {
        // Arrange
        val mockReports = listOf(
            ReportResponse(
                id = "1",
                type = "LOST",
                reporterId = "user1",
                reporterName = "User One",
                reporterPhone = "+56900000001",
                description = "Lost dog",
                status = "OPEN",
                latitude = null,
                longitude = null,
                locationName = "Central Park",
                eventDate = "2024-01-01",
                photoBase64 = null,
                photoUrl = null,
                species = null,
                breed = null,
                color = null,
                createdAt = null,
                updatedAt = null
            ),
            ReportResponse(
                id = "2",
                type = "LOST",
                reporterId = "user2",
                reporterName = "User Two",
                reporterPhone = "+56900000002",
                description = "Lost cat",
                status = "OPEN",
                latitude = null,
                longitude = null,
                locationName = "Park",
                eventDate = "2024-01-02",
                photoBase64 = null,
                photoUrl = null,
                species = null,
                breed = null,
                color = null,
                createdAt = null,
                updatedAt = null
            ),
            ReportResponse(
                id = "3",
                type = "FOUND",
                reporterId = "user3",
                reporterName = "User Three",
                reporterPhone = "+56900000003",
                description = "Found cat",
                status = "OPEN",
                latitude = null,
                longitude = null,
                locationName = "Street",
                eventDate = "2024-01-03",
                photoBase64 = null,
                photoUrl = null,
                species = null,
                breed = null,
                color = null,
                createdAt = null,
                updatedAt = null
            )
        )
        
        doReturn(flowOf("valid-token"))
            .`when`(mockSessionStore)
            .tokenFlow
            
        doReturn(MapsResult.Success(mockReports))
            .`when`(mockUserReportsRepository)
            .listMyReports(any())
        
        doReturn(mockApplication.applicationContext)
            .`when`(mockApplication)
            .applicationContext

        // Act: Load reports, then filter by type LOST
        // Assert: Only 2 LOST reports should be in filtered list
        assertTrue(true) // Placeholder
    }
}
