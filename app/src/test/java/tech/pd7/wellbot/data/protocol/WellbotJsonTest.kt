package tech.pd7.wellbot.data.protocol

import org.junit.Assert.assertEquals
import org.junit.Test
import tech.pd7.wellbot.models.MapTransferRequest
import tech.pd7.wellbot.models.NavigationRequest

class WellbotJsonTest {
    @Test
    fun `codifica point de navegacao com contrato versionado`() {
        val json = WellbotJson.encodeNavigationRequest(
            NavigationRequest(
                requestId = "req-1",
                serialNumber = "WB-001",
                pointId = "palco",
                timestamp = "2026-11-05T12:00:00Z",
            ),
        )

        assertEquals(
            "{\"version\":1,\"requestId\":\"req-1\",\"sn\":\"WB-001\",\"pointId\":\"palco\",\"timestamp\":\"2026-11-05T12:00:00Z\"}",
            json,
        )
    }

    @Test
    fun `codifica solicitacao de transferencia de mapa`() {
        val json = WellbotJson.encodeMapTransfer(
            MapTransferRequest(
                requestId = "req-map-1",
                sourceSerialNumber = "WB-001",
                targetSerialNumber = "WB-002",
                mapId = "map-evento-a",
                timestamp = "2026-11-10T18:00:00Z",
            ),
        )

        assertEquals(
            "{\"version\":1,\"requestId\":\"req-map-1\",\"sourceSn\":\"WB-001\",\"targetSn\":\"WB-002\",\"mapId\":\"map-evento-a\",\"timestamp\":\"2026-11-10T18:00:00Z\"}",
            json,
        )
    }
}
