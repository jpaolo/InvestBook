package org.home.prac.invest.book.util

import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.UnsupportedFlavorException
import java.io.IOException

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