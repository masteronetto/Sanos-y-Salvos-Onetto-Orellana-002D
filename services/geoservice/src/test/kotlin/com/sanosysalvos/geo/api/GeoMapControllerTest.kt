package com.sanosysalvos.geo.api

import com.sanosysalvos.contracts.NearbyReportsResponse
import com.sanosysalvos.contracts.NavigationLinkResponse
import com.sanosysalvos.contracts.ReportType
import com.sanosysalvos.contracts.TileProviderConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GeoMapControllerTest {
    private val controller = GeoMapController()

    @Test
    fun `provider returns default openstreetmap provider`() {
        val expected = TileProviderConfig(
            provider = "openstreetmap",
            tileUrlTemplate = "https://tile.openstreetmap.org/{z}/{x}/{y}.png",
            attribution = "© OpenStreetMap contributors",
            termsUrl = "https://www.openstreetmap.org/copyright",
        )

        assertEquals(expected, controller.provider())
    }

    @Test
    fun `layers returns defined map layers`() {
        val layers = controller.layers()
        assertEquals(3, layers.size)
    }

    @Test
    fun `nearbyReports filters by report type and radius`() {
        val response = controller.nearbyReports(0.0, 0.0, 10000, ReportType.LOST)
        assertEquals(2, response.total)
    }

    @Test
    fun `navigationLink returns a google maps link`() {
        val result = controller.navigationLink(1.0, 2.0, 3.0, 4.0)
        assertEquals("google-maps", result.provider)
    }
}
