package org.home.prac.invest.book.util

import org.home.prac.invest.book.models.Amount
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.*

fun isValidAmount(amountStr: String): Boolean {
    val regex = Regex("-?[$]?([0-9]{1,3}(,[0-9]{3})*|[0-9]+)(.{1}[0-9]+)?")
    return regex.matches(amountStr)
}

fun toAmount(amountStr: String): Amount {
    if (isValidAmount(amountStr)) {
        val value: BigDecimal = amountStr
            .replace("-", "")
            .replace("$", "")
            .replace(",", "").toBigDecimal()
        val formattedAmount = toAmount(value)
        return Amount(value, formattedAmount)
    }
    throw NumberFormatException("Value for Amount [$amountStr] is not comprehensible")
}

fun toAmount(value: BigDecimal): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.US)
    return formatter.format(value)
}