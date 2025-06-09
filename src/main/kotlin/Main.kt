package org.home.prac.invest.book

import org.home.prac.invest.book.mapper.ActivityMapper
import org.home.prac.invest.book.models.Activity
import org.home.prac.invest.book.models.ActivityType
import org.home.prac.invest.book.util.getSplitsWithTrimming
import org.home.prac.invest.book.util.readClipboardText
import org.home.prac.invest.book.util.toInvestBookExecutionFromActivity

/***
 * mode=0: from Ally Activities page to InvestBook current year tab
 * mode=1: from InvestBook current year tab to InvestBook summary activities
 */
fun main(args: Array<String>) {
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
        if (args[0] == "0") {   // mode=0: from Ally Activities page to InvestBook current year tab
            val investBookExecutions = StringBuilder()
            var row = args[1].toInt()
            activities.filter { it.type == ActivityType.SOLD || it.type == ActivityType.BOUGHT }.forEach {
                investBookExecutions.append(toInvestBookExecutionFromActivity(it, row++))
                investBookExecutions.append(10.toChar()) // ascii-10 = NL
            }
            investBookExecutions.deleteCharAt(investBookExecutions.length - 1)
            // TODO: to clipboard
            println(investBookExecutions)
        } else if (args[0] == "1") {    // mode=1: from InvestBook current year tab to InvestBook summary activities

        }

    } else {
        println("No text found on the clipboard or an error occurred.")
    }
}
