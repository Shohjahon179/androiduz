package com.example.vagonboshqaruvi

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Locale

data class User(
    val id: String = "",
    val phoneNumber: String = "",
    val username: String = "",
    val password: String = "",
    val role: String = "Tanlanmagan"
)

data class WagonRecord(
    val id: String = "",
    val trackNumber: String = "1-yo'l",
    val wagonNumber: String = "",
    val arrivalDate: String = "",
    val arrivalTime: String = "",
    val trainInNumber: String = "",
    val givenToUnloadDate: String = "",
    val givenToUnloadTime: String = "",
    val takenFromUnloadDate: String = "",
    val takenFromUnloadTime: String = "",
    val departureDate: String = "",
    val departureTime: String = "",
    val trainOutNumber: String = "",
    val durationUntilGiven: String = "",
    val durationAfterTaken: String = "",
    val totalDuration: String = "",
    val createdBy: String = ""
)

data class ChatMessage(
    val id: String = "",
    val senderName: String = "",
    val senderRole: String = "",
    val text: String = "",
    val time: String = "",
    val isEdited: Boolean = false,
    val fileName: String? = null,
    val localFilePath: String? = null
)

object TimeCalculator {
    private val format = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    fun calculateDifference(date1: String, time1: String, date2: String, time2: String): String {
        if (date1.isBlank() || time1.isBlank() || date2.isBlank() || time2.isBlank()) {
            return "Kiritilmagan"
        }
        return try {
            val d1 = format.parse("${date1.trim()} ${time1.trim()}") ?: return "Xato sana/vaqt"
            val d2 = format.parse("${date2.trim()} ${time2.trim()}") ?: return "Xato sana/vaqt"
            val diffMillis = d2.time - d1.time
            if (diffMillis < 0) return "Vaqt xato (manfiy)"
            val totalMinutes = diffMillis / (1000 * 60)
            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60
            "$hours soat $minutes daq"
        } catch (e: Exception) {
            "Format: kk.oo.yyyy va ss:dd"
        }
    }

    fun splitWagonsAndCreateRecords(rawWagons: String, baseRecord: WagonRecord): List<WagonRecord> {
        val wagonList = rawWagons.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val untilGiven = calculateDifference(
            baseRecord.arrivalDate, baseRecord.arrivalTime,
            baseRecord.givenToUnloadDate, baseRecord.givenToUnloadTime
        )
        val afterTaken = calculateDifference(
            baseRecord.takenFromUnloadDate, baseRecord.takenFromUnloadTime,
            baseRecord.departureDate, baseRecord.departureTime
        )
        val total = calculateDifference(
            baseRecord.arrivalDate, baseRecord.arrivalTime,
            baseRecord.departureDate, baseRecord.departureTime
        )

        return wagonList.mapIndexed { index, wagonNum ->
            baseRecord.copy(
                id = "${System.currentTimeMillis()}_$index",
                trackNumber = "${index + 1}-yo'l",
                wagonNumber = wagonNum,
                durationUntilGiven = untilGiven,
                durationAfterTaken = afterTaken,
                totalDuration = total
            )
        }
    }
}

class LocalStorage(context: Context) {
    private val prefs = context.getSharedPreferences("vagon_boshqaruvi_v5_db", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveThemeMode(isNight: Boolean) {
        prefs.edit().putBoolean("is_night_glass", isNight).apply()
    }

    fun loadThemeMode(): Boolean {
        return prefs.getBoolean("is_night_glass", true)
    }

    fun saveUsers(list: List<User>) {
        prefs.edit().putString("users", gson.toJson(list)).apply()
    }

    fun loadUsers(): MutableList<User> {
        val json = prefs.getString("users", null) ?: return mutableListOf(
            User("1", "+998901234567", "xodim", "1234", "Xodim"),
            User("2", "+998907654321", "rahbar", "1234", "Rahbar")
        )
        val type = object : TypeToken<MutableList<User>>() {}.type
        return gson.fromJson(json, type)
    }

    fun saveRecords(list: List<WagonRecord>) {
        prefs.edit().putString("records", gson.toJson(list)).apply()
    }

    fun loadRecords(): MutableList<WagonRecord> {
        val json = prefs.getString("records", null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<WagonRecord>>() {}.type
        return gson.fromJson(json, type)
    }

    fun saveMessages(list: List<ChatMessage>) {
        prefs.edit().putString("messages", gson.toJson(list)).apply()
    }

    fun loadMessages(): MutableList<ChatMessage> {
        val json = prefs.getString("messages", null) ?: return mutableListOf(
            ChatMessage("1", "Rahbar", "Rahbar", "Assalomu alaykum! Vagonlar harakatini nazoratga olamiz.", "09:00")
        )
        val type = object : TypeToken<MutableList<ChatMessage>>() {}.type
        return gson.fromJson(json, type)
    }
}
