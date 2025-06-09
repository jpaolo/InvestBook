package org.home.prac.invest.book.models

import java.time.LocalDate

data class Activity(val date: LocalDate,
                    val type: ActivityType,
                    val description: String,
                    val amount: Amount,
                    val symbol: String?,
                    val shares: Int?,
                    val price: Amount?,
                    val fee: Amount?
)

enum class ActivityType(val sourceName: String) {
    BOUGHT("Bought"),
    SOLD("Sold"),
    DIVIDEND("Dividend"),
    TRANSFER("Cash Movement"),
    UNDEFINED("Undefined");

    companion object {
        private val map = entries.associateBy(ActivityType::sourceName)
        fun fromAllyText(allyActivityText: String) = map[allyActivityText] ?: UNDEFINED
    }
}
