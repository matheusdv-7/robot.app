package tech.pd7.wellbot.data.mqtt

/**
 * Contrato provisório. Ajuste somente esta classe quando os tópicos oficiais forem aprovados.
 */
class TopicLayout(private val root: String = "wellbot/v1") {
    val discovery: String = "$root/robots/availability"
    val mapTransfers: String = "$root/maps/transfers"

    fun navigationPoints(serialNumber: String): String =
        "$root/robots/${safeSn(serialNumber)}/navigation/points"

    fun navigationRequests(serialNumber: String): String =
        "$root/robots/${safeSn(serialNumber)}/navigation/requests"

    fun maps(serialNumber: String): String = "$root/robots/${safeSn(serialNumber)}/maps"
    fun state(serialNumber: String): String = "$root/robots/${safeSn(serialNumber)}/state"
    fun acknowledgements(serialNumber: String): String = "$root/robots/${safeSn(serialNumber)}/acks"

    private fun safeSn(value: String): String {
        require(value.matches(Regex("[A-Za-z0-9._-]{1,64}"))) { "SN inválido para tópico MQTT." }
        return value
    }
}
