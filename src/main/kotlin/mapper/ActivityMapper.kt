package org.home.prac.invest.book.mapper

import org.home.prac.invest.book.models.Activity
import org.home.prac.invest.book.models.ActivityType
import org.home.prac.invest.book.util.getSplitsWithTrimming
import java.lang.Math.abs
import java.time.LocalDate
import java.time.format.DateTimeFormatter



class ActivityMapper {

    fun fromAllyActivity(singleLineText: String): Activity? {
        val properties = getSplitsWithTrimming(singleLineText, 9.toChar()) // ascii-9 = tab
        val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
        val date: LocalDate = LocalDate.parse(properties[0], formatter)
        val type = ActivityType.fromAllyText(properties[1])
        val shares = properties[2].toIntOrNull()?.let {
            abs(properties[2].toInt())
        }
        val symbol = properties[3]
        val description = properties[4]
//        val price
        return null
    }
}