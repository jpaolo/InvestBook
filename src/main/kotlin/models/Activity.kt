package org.home.prac.invest.book.models

import java.time.LocalDate
import java.util.*

data class Activity(val date: LocalDate,
                    val type: ActivityType,
                    val description: String,
                    val amount: Currency,
                    val symbol: String?,
                    val shares: Int,
                    val price: Currency?,
                    val fee: Currency?
)

enum class ActivityType(val sourceName: String) {
    BOUGHT("Bought"),
    SOLD("Sold"),
    DIVIDEND("Dividend"),
    TRANSFER("Cash Movement");

    companion object {
        private val map = entries.associateBy(ActivityType::sourceName)
        fun fromAllyText(allyActivityText: String) = map[allyActivityText]
    }
}
