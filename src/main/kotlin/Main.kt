package org.home.prac.invest.book

import org.home.prac.invest.book.mapper.ActivityMapper
import org.home.prac.invest.book.models.Activity
import org.home.prac.invest.book.util.getSplitsWithTrimming
import org.home.prac.invest.book.util.readClipboardText
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
        val convertor = FiToBookConvertor()
        var summRow: Int? = null
        var summAmountCol: String? = null
        var summBalCol: String? = null
        if (args.size == 5) {
            summRow = args[2].toInt()
            summAmountCol = args[3]
            summBalCol = args[4]
        }
        convertor.processActivities(
            activities = activities,
            mode = args[0],
            startingRow = args[1].toInt(),
            summRow = summRow,
            summAmountCol = summAmountCol,
            summBalCol = summBalCol
        )
    } else {
        println("No text found on the clipboard or an error occurred.")
    }

    println("Duration: ${timeSource.markNow() - startTime}")
}