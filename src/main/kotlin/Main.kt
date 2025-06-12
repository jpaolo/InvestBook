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
        convertor.processActivities(activities, args[0], args[1].toInt())
    } else {
        println("No text found on the clipboard or an error occurred.")
    }

    println("Duration: ${timeSource.markNow() - startTime}")
}