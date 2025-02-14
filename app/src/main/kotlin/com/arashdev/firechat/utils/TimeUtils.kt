package com.arashdev.firechat.utils

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * used in chat
 */
fun formatUtcToHoursAndMinutes(epochSeconds: Long): String {
	val instant = Instant.ofEpochSecond(epochSeconds)
	val localTime = LocalTime.ofInstant(instant, ZoneId.systemDefault())
	val formatter = DateTimeFormatter.ofPattern("HH:mm")
		.withLocale(Locale.getDefault())

	return localTime.format(formatter)
}

/**
 * used in Conversation last message time preview
 */
fun formatUtcToLocalTime(epochSeconds: Long): String {
	val instant = Instant.ofEpochSecond(epochSeconds)
	val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
	val localDate = localDateTime.toLocalDate()

	// Get today's date and yesterday's date in the local time zone for comparison
	val today = LocalDate.now(ZoneId.systemDefault())
	val yesterday = today.minusDays(1)

	return when {
		// 1. Today: Format as hours and minutes (e.g., 14:30)
		localDate.isEqual(today) -> localDateTime.format(DateTimeFormatter.ofPattern("HH:mm"))

		// 2. Yesterday: Format as "Yesterday"
		localDate.isEqual(yesterday) -> "Yesterday"

		// 3. Older than yesterday but within the last month: Format as month and day (e.g., Oct 11)
		localDate.isAfter(today.minusMonths(1)) -> localDateTime.format(DateTimeFormatter.ofPattern("MMM dd"))

		// 4. Older than a month: Format as year/month/day (e.g., 2023/10/11)
		else -> localDateTime.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
	}
}