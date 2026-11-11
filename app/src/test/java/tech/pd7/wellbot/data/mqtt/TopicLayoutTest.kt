package tech.pd7.wellbot.data.mqtt

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class TopicLayoutTest {
    private val topics = TopicLayout()

    @Test
    fun `gera topico de navegacao por SN`() {
        assertEquals(
            "wellbot/v1/robots/WB-001/navigation/requests",
            topics.navigationRequests("WB-001"),
        )
    }

    @Test
    fun `rejeita SN que injeta wildcard MQTT`() {
        assertThrows(IllegalArgumentException::class.java) { topics.navigationRequests("WB/#") }
    }
}
