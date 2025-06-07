package org.home.prac.invest.book

import org.home.prac.invest.book.mapper.ActivityMapper
import org.home.prac.invest.book.models.Activity
import org.home.prac.invest.book.util.getSplitsWithTrimming
import org.home.prac.invest.book.util.readClipboardText

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main() {
    val clipboardContent = try {
        readClipboardText()
    } catch (e: Exception) {
        e.printStackTrace()
    }

    if (clipboardContent != null) {
        println("Clipboard content:")
        println(clipboardContent)
        val charCount = clipboardContent.toString().length
        val charList = clipboardContent.toString().toCharArray()
        println("$charCount characters")
//        for ((i, char) in charList.withIndex()) {
//            println("$i: $char; ascii: ${char.code}")
//        }

        // do real work (ascii-10 = NL)
        val lines = getSplitsWithTrimming(clipboardContent.toString(), 10.toChar())
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

    } else {
        println("No text found on the clipboard or an error occurred.")
    }
}
