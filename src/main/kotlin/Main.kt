package org.home.prac.invest.book

import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.UnsupportedFlavorException
import java.io.IOException

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main() {
    val clipboardContent = try {
        readClipboardText()
    } catch (e: Exception) {
        e.printStackTrace()
    }

    if (clipboardContent != null) {
        println("Clipboard content: $clipboardContent")
        val charCount = clipboardContent.toString().length
        val charList = clipboardContent.toString().toCharArray()
        println("$charCount characters")
        for ((i, char) in charList.withIndex()) {
            println("$i: $char; ascii: ${char.code}")
        }
    } else {
        println("No text found on the clipboard or an error occurred.")
    }
}

fun readClipboardText(): String? {
    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
    return try {
        if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
            clipboard.getData(DataFlavor.stringFlavor) as String
        } else {
            null // No plain text available on the clipboard
        }
    } catch (e: UnsupportedFlavorException) {
        e.printStackTrace()
        null
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}