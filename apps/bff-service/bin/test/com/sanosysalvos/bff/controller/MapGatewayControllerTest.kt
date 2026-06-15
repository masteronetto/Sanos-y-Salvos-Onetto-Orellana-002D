package com.sanosysalvos.bff.controller

import com.sanosysalvos.bff.client.GeoServiceClient
import com.sanosysalvos.contracts.MapLayer
import com.sanosysalvos.contracts.NavigationLinkResponse
import com.sanosysalvos.contracts.NearbyReportsResponse
import com.sanosysalvos.contracts.ReportType
import com.sanosysalvos.contracts.TileProviderConfig
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MapGatewayControllerTest {
    private val geoServiceClient = mockk<GeoServiceClient>()
    private val controller = MapGatewayController(geoServiceClient)

    @Test
    fun `provider returns provider config from geo service`() {
        val provider = TileProviderConfig("openstreetmap", "url", "attr", "terms")
        every { geoServiceClient.provider() } returns provider

        assertEquals(provider, controller.provider())
    }

    @Test
    fun `nearbyReports delegates to geo service`() {
        val response = NearbyReportsResponse(0.0, 0.0, 1000, 0, emptyList())
        every { geoServiceClient.nearbyReports(1.0, 2.0, 3000, null) } returns response

        assertEquals(response, controller.nearbyReports(1.0, 2.0, 3000, null))
    }

    @Test
    fun `navigationLink returns navigation response`() {
        val response = NavigationLinkResponse("google-maps", 1.0, 2.0, 3.0, 4.0, "url")
        every { geoServiceClient.navigationLink(1.0, 2.0, 3.0, 4.0) } returns response

        assertEquals(response, controller.navigationLink(1.0, 2.0, 3.0, 4.0))
    }
}
