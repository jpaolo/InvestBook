package org.home.prac.invest.book.util

fun getSplitsWithTrimming(input: String, separator: Char): List<String> {
    val list = input.split(separator)
    return list.map { it.trim() }
}