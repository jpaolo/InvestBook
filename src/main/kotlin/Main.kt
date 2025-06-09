package org.home.prac.invest.book

import org.home.prac.invest.book.mapper.ActivityMapper
import org.home.prac.invest.book.models.Activity
import org.home.prac.invest.book.models.ActivityType
import org.home.prac.invest.book.util.getSplitsWithTrimming
import org.home.prac.invest.book.util.readClipboardText
import org.home.prac.invest.book.util.toInvestBookExecutionFromActivity
import org.home.prac.invest.book.util.toInvestBookSummaryTrade
import java.time.LocalDate
import kotlin.time.TimeSource

/***
 * mode=0: from Ally Activities page to InvestBook current year tab
 * mode=1: from InvestBook current year tab to InvestBook summary activities
 */
fun main(args: Array<String>) {
    val timeSource = TimeSource.Monotonic
    val startTime = timeSource.markNow()

    val clipboardContent = try {
        readClipboardText()
    } catch (e: Exception) {
        e.printStackTrace()
    }

    val lines = getSplitsWithTrimming(clipboardContent.toString(), 10.toChar())  // ascii-10 = NL
    val activityMapper = ActivityMapper()
    val activities = mutableListOf<Activity>()
    for (line in lines) {
        try {
            val activity = activityMapper.fromAllyActivity(line)
            activity?.let {
                activities.add(activity)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    if (clipboardContent != null) {
        val toClipboard = StringBuilder()
        val trades = activities.filter { it.type == ActivityType.SOLD || it.type == ActivityType.BOUGHT }

        if (args[0] == "0") {   // mode=0: from Ally Activities page to InvestBook current year tab
            var row = args[1].toInt()
            trades.forEach {
                toClipboard.append(toInvestBookExecutionFromActivity(it, row++))
                toClipboard.append(10.toChar()) // ascii-10 = NL
            }
        } else if (args[0] == "1") {    // mode=1: from InvestBook current year tab to InvestBook summary activities
            var row = args[1].toInt() + trades.size - 1
            activities.reversed().forEach {
                when (it.type) {
                    ActivityType.BOUGHT, ActivityType.SOLD -> {
                        toClipboard.append(toInvestBookSummaryTrade(
                            row--,
                            LocalDate.now().year)
                        )
                        toClipboard.append(10.toChar())
                    } else -> {
                        // TODO: handle other types
                        toClipboard.append("// TODO: handle other types")
                        toClipboard.append(10.toChar())
                    }
                }
            }
        }
        toClipboard.deleteCharAt(toClipboard.length - 1)

        // TODO: to clipboard
    } else {
        println("No text found on the clipboard or an error occurred.")
    }

    println("Duration: ${timeSource.markNow() - startTime}")
}
