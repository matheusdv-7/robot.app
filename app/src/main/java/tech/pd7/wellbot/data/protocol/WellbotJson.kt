package tech.pd7.wellbot.data.protocol

import org.json.JSONObject
import tech.pd7.wellbot.models.MapTransferRequest
import tech.pd7.wellbot.models.NavigationPoint
import tech.pd7.wellbot.models.NavigationRequest
import tech.pd7.wellbot.models.Robot
import tech.pd7.wellbot.models.RobotMap

object WellbotJson {
    fun encodeNavigationRequest(request: NavigationRequest): String = buildString {
        append('{')
        append("\"version\":").append(request.version).append(',')
        append("\"requestId\":\"").append(escape(request.requestId)).append("\",")
        append("\"sn\":\"").append(escape(request.serialNumber)).append("\",")
        append("\"pointId\":\"").append(escape(request.pointId)).append("\",")
        append("\"timestamp\":\"").append(escape(request.timestamp)).append('"')
        append('}')
    }

    fun encodeMapTransfer(request: MapTransferRequest): String = buildString {
        append('{')
        append("\"version\":").append(request.version).append(',')
        append("\"requestId\":\"").append(escape(request.requestId)).append("\",")
        append("\"sourceSn\":\"").append(escape(request.sourceSerialNumber)).append("\",")
        append("\"targetSn\":\"").append(escape(request.targetSerialNumber)).append("\",")
        append("\"mapId\":\"").append(escape(request.mapId)).append("\",")
        append("\"timestamp\":\"").append(escape(request.timestamp)).append('"')
        append('}')
    }

    fun decodeRobots(payload: String): List<Robot> {
        val robots = JSONObject(payload).getJSONArray("robots")
        return buildList {
            for (index in 0 until robots.length()) {
                val item = robots.getJSONObject(index)
                add(
                    Robot(
                        serialNumber = item.getString("sn"),
                        displayName = item.optString("displayName", item.getString("sn")),
                        online = item.optBoolean("online", true),
                    ),
                )
            }
        }
    }

    fun decodeNavigationPoints(payload: String): List<NavigationPoint> {
        val points = JSONObject(payload).getJSONArray("points")
        return buildList {
            for (index in 0 until points.length()) {
                val item = points.getJSONObject(index)
                add(NavigationPoint(id = item.getString("id"), label = item.getString("label")))
            }
        }
    }

    fun decodeMaps(payload: String): List<RobotMap> {
        val maps = JSONObject(payload).getJSONArray("maps")
        return buildList {
            for (index in 0 until maps.length()) {
                val item = maps.getJSONObject(index)
                add(
                    RobotMap(
                        id = item.getString("id"),
                        name = item.getString("name"),
                        version = item.optString("version").ifBlank { null },
                    ),
                )
            }
        }
    }

    private fun escape(value: String): String = buildString(value.length) {
        value.forEach { character ->
            when (character) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> append(character)
            }
        }
    }
}
