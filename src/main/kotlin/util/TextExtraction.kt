package org.home.prac.invest.book.util

import org.home.prac.invest.book.models.Activity
import org.home.prac.invest.book.models.ActivityType
import java.lang.IllegalStateException
import java.time.format.DateTimeFormatter

val dateCol = "A"
val shareCol = "B"
val opCol = "C"
val priceCol = "D"
val feeCol = "E"
val amountCol = "F"

fun getSplitsWithTrimming(input: String, separator: Char): List<String> {
    return input.split(separator).map { it.trim() }
}

fun toInvestBookExecutionFromActivity(activity: Activity, curRow: Int): String {

    val rowText = StringBuilder()
    val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
    rowText.append(activity.date.format(formatter)).append('\t')
    rowText.append(activity.shares).append('\t')
    rowText.append("${activity.type.sourceName}: ${activity.symbol}").append('\t')
    rowText.append(activity.price!!.formattedAmount).append('\t')
    rowText.append(activity.fee!!.formattedAmount).append('\t')

    when (activity.type) {
        ActivityType.BOUGHT -> {
            rowText.append("=ROUND(-$shareCol$curRow*$priceCol$curRow-$feeCol$curRow,2)")
        }
        ActivityType.SOLD -> {
            rowText.append("=ROUND($shareCol$curRow*$priceCol$curRow-$feeCol$curRow,2)")
        }
        else -> {
            throw IllegalStateException("${activity.type}")
        }
    }

    return rowText.toString()
}

fun toInvestBookSummaryTrade(curRow: Int, curYear: Int): String {

    val rowText = StringBuilder()
    rowText.append("='executions $curYear'!$dateCol$curRow").append('\t')
    rowText.append("=CONCATENATE(SUBSTITUTE('executions $curYear'!$opCol$curRow,\":\",CONCATENATE(\" \",'executions $curYear'!$shareCol$curRow),1),\" @ \",TEXT('executions $curYear'!$priceCol$curRow,\"\$0.00\"))")
    rowText.append("='executions $curYear'!$amountCol$curRow").append('\t')

    return rowText.toString()
}