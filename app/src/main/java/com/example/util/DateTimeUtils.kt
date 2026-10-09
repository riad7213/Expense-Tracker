package com.example.util

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateTimeUtils {
    private val dateFormat = SimpleDateFormat("dd-MMM-yy", Locale.US)
    private val dayFormat = SimpleDateFormat("EEE", Locale.US)
    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.US)
    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.US)

    fun getCurrentDate(): String = dateFormat.format(Date())

    fun getCurrentDay(): String = dayFormat.format(Date())

    fun getCurrentYearMonth(): Pair<Int, Int> {
        val cal = Calendar.getInstance()
        return Pair(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1) // 1-based month
    }

    fun formatYearMonth(year: Int, month1Based: Int): String {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month1Based - 1)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        return monthYearFormat.format(cal.time)
    }

    fun getPreviousYearMonth(year: Int, month1Based: Int): Pair<Int, Int> {
        return if (month1Based == 1) {
            Pair(year - 1, 12)
        } else {
            Pair(year, month1Based - 1)
        }
    }

    fun getNextYearMonth(year: Int, month1Based: Int): Pair<Int, Int> {
        return if (month1Based == 12) {
            Pair(year + 1, 1)
        } else {
            Pair(year, month1Based + 1)
        }
    }

    fun parseYearMonthFromDate(dateStr: String): Pair<Int, Int>? {
        return try {
            val date = dateFormat.parse(dateStr.trim()) ?: return null
            val cal = Calendar.getInstance().apply { time = date }
            Pair(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
        } catch (_: Exception) {
            null
        }
    }

    fun isDateInYearMonth(dateStr: String, year: Int, month1Based: Int): Boolean {
        val parsed = parseYearMonthFromDate(dateStr) ?: return false
        return parsed.first == year && parsed.second == month1Based
    }

    fun deriveDayFromDate(dateStr: String): String {
        return try {
            val date = dateFormat.parse(dateStr.trim())
            if (date != null) dayFormat.format(date) else getCurrentDay()
        } catch (_: Exception) {
            getCurrentDay()
        }
    }

    fun formatDate(millis: Long): String = dateFormat.format(Date(millis))

    fun formatTime(hour24: Int, minute: Int): String {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour24)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }
        return timeFormat.format(cal.time)
    }

    fun parseDateToCalendar(dateStr: String): Calendar {
        val cal = Calendar.getInstance()
        try {
            val date = dateFormat.parse(dateStr.trim())
            if (date != null) cal.time = date
        } catch (_: Exception) {}
        return cal
    }

    fun parseTimeToHourMinute(timeStr: String): Pair<Int, Int> {
        val cal = Calendar.getInstance()
        return try {
            val date = timeFormat.parse(timeStr.trim())
            if (date != null) cal.time = date
            Pair(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
        } catch (_: Exception) {
            Pair(17, 0) // default 05:00 PM
        }
    }

    fun parseTimeToMinutes(timeStr: String): Int {
        return try {
            val date = timeFormat.parse(timeStr.trim()) ?: return 0
            val cal = Calendar.getInstance().apply { time = date }
            cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        } catch (_: Exception) {
            0
        }
    }

    fun formatMinutesToDuration(totalMinutes: Int): String {
        val safeMinutes = if (totalMinutes < 0) totalMinutes + 24 * 60 else totalMinutes
        val hours = safeMinutes / 60
        val mins = safeMinutes % 60
        return String.format(Locale.US, "%02d:%02d", hours, mins)
    }

    fun formatMinutesToHoursAndMinutes(totalMinutes: Int): String {
        val safeMinutes = totalMinutes.coerceAtLeast(0)
        val hours = safeMinutes / 60
        val mins = safeMinutes % 60
        return String.format(Locale.US, "%dh %02dm", hours, mins)
    }

    fun calculateMinutesBetween(startTimeStr: String, endTimeStr: String): Int {
        val start = parseTimeToMinutes(startTimeStr)
        val end = parseTimeToMinutes(endTimeStr)
        val diff = end - start
        return if (diff < 0) diff + 24 * 60 else diff
    }

    fun calculateDurations(
        leavingTime: String,
        startingTime: String,
        endingTime: String,
        returnTime: String
    ): Triple<Int, Int, Int> {
        val classMinutes = calculateMinutesBetween(startingTime, endingTime)
        val totalMinutes = calculateMinutesBetween(leavingTime, returnTime)
        val transportMinutes = (totalMinutes - classMinutes).coerceAtLeast(0)
        return Triple(classMinutes, transportMinutes, totalMinutes)
    }

    fun formatCurrencyBDT(amount: Double): String {
        return String.format(Locale.US, "৳%,.0f", amount)
    }

    /**
     * Opens native Android DatePickerDialog.
     * onDateSelected receives formatted date "dd-MMM-yy" and derived day "ddd".
     */
    fun showNativeDatePicker(
        context: Context,
        initialDateStr: String = getCurrentDate(),
        onDateSelected: (formattedDate: String, derivedDay: String) -> Unit
    ) {
        val initialCal = parseDateToCalendar(initialDateStr)
        val dialog = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                val formatted = dateFormat.format(selectedCal.time)
                val day = dayFormat.format(selectedCal.time)
                onDateSelected(formatted, day)
            },
            initialCal.get(Calendar.YEAR),
            initialCal.get(Calendar.MONTH),
            initialCal.get(Calendar.DAY_OF_MONTH)
        )
        dialog.show()
    }

    /**
     * Opens native Android TimePickerDialog with 12-hour clock (AM/PM).
     * onTimeSelected receives formatted time "hh:mm a".
     */
    fun showNativeTimePicker(
        context: Context,
        initialTimeStr: String = "05:00 PM",
        onTimeSelected: (formattedTime: String) -> Unit
    ) {
        val (initialHour, initialMinute) = parseTimeToHourMinute(initialTimeStr)
        val dialog = TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val formatted = formatTime(hourOfDay, minute)
                onTimeSelected(formatted)
            },
            initialHour,
            initialMinute,
            false // 12-hour format with AM/PM
        )
        dialog.show()
    }
}
