package org.home.prac.invest.book.util

fun getSplitsWithTrimming(input: String, separator: Char): List<String> {
    return input.split(separator).map { it.trim() }
}