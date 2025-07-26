package org.home.prac.invest.book

import org.home.prac.invest.book.models.Activity
import org.home.prac.invest.book.models.ActivityType
import org.home.prac.invest.book.util.toAmount
import org.home.prac.invest.book.util.writeToClipboard
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class FiToBookConvertor {

    private companion object {
        const val DATE_COL = "A"
        const val SHARE_COL = "B"
        const val OP_COL = "C"
        const val PRICE_COL = "D"
        const val FEE_COL = "E"
        const val AMOUNT_COL = "F"
        const val NEWLINE = '\n'
        const val TAB = '\t'
        val TRADE_TYPES = setOf(ActivityType.BOUGHT, ActivityType.SOLD)
    }

    private val dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")

    /**
     * Processes activities and converts them based on the specified mode
     * @param activities List of activities to process
     * @param mode Processing mode (0 or 1)
     * @param startingRow Starting row number for processing
     */
    fun processActivities(
        activities: List<Activity>,
        mode: String,
        startingRow: Int,
        summRow: Int? = null,
        summAmountCol: String? = null,
        summBalCol: String? = null
    ) {
        val trades = activities.filter { it.type in TRADE_TYPES }
        val toClipboard = buildString { // Inline string building: Used buildString for better memory allocation
            when (mode) {
                "0" -> processForExeTab(trades, startingRow)
                "1" -> processForSummary(activities, startingRow, trades.size, summRow, summAmountCol, summBalCol)
            }
            // Remove trailing newline
            if (isNotEmpty() && last() == NEWLINE) deleteCharAt(length - 1)
        }

        println("New clipboard:")
        println(toClipboard)
        writeToClipboard(toClipboard)
        printDiscrepancies(trades)
    }

    /**
     * Mode 0: from Ally Activities page to InvestBook current year tab
     */
    private fun StringBuilder.processForExeTab(trades: List<Activity>, startingRow: Int) {
        trades.forEachIndexed { index, trade ->
            append(toInvestBookExecutionFromActivity(trade, startingRow + index))
            append(NEWLINE)
        }
    }

    /**
     * Mode 1: from InvestBook current year tab to InvestBook summary activities
     */
    private fun StringBuilder.processForSummary(
        activities: List<Activity>,
        startingRow: Int,
        tradesSize: Int,
        summRow: Int?,
        summAmountCol: String?,
        summBalCol: String?
    ) {
        var row = startingRow + tradesSize - 1
        var currentSummRow = summRow

        activities.asReversed().forEach { activity ->
            when (activity.type) {
                in TRADE_TYPES -> append(toInvestBookSummaryTrade(row--, LocalDate.now().year))
                else -> append(toInvestBookSummaryNonTradeActivity(activity))
            }

            if (summRow != null && summAmountCol != null && summBalCol != null) {
                append(TAB).append("=$summBalCol$currentSummRow+$summAmountCol${++currentSummRow}")
            }
            append(NEWLINE)
        }
    }

    /**
     * Converts Ally activity to InvestBook execution tab
     */
    private fun toInvestBookExecutionFromActivity(activity: Activity, row: Int): String {
        val amountFormula = when (activity.type) {
            ActivityType.BOUGHT -> "=ROUND(-$SHARE_COL$row*$PRICE_COL$row-$FEE_COL$row,2)"
            ActivityType.SOLD -> "=ROUND($SHARE_COL$row*$PRICE_COL$row-$FEE_COL$row,2)"
            else -> throw IllegalStateException("Unsupported trade type: ${activity.type}")
        }

        val priceToUse = getDiscrepantCalculatedAmount(activity)?.let {
            calculateSuggestedPrice(activity)
        } ?: activity.price

        return buildString {
            append(activity.date.format(dateFormatter)).append(TAB)
            append(activity.shares).append(TAB)
            append("${activity.type.sourceName}: ${activity.symbol}").append(TAB)
            append(priceToUse).append(TAB)
            append(activity.fee!!.formattedAmount).append(TAB)
            append(amountFormula)
        }
    }

    /**
     * Convert to InvestBook summary for trades
     */
    private fun toInvestBookSummaryTrade(row: Int, year: Int) = buildString {
        append("='executions $year'!$DATE_COL$row").append(TAB)
        append("=CONCATENATE(SUBSTITUTE('executions $year'!$OP_COL$row,\":\",CONCATENATE(\" \",'executions $year'!$SHARE_COL$row),1),\" @ \",TEXT('executions $year'!$PRICE_COL$row,\"$0.00\"))").append(TAB)
        append("='executions $year'!$AMOUNT_COL$row")
    }

    private fun toInvestBookSummaryNonTradeActivity(activity: Activity) = buildString {
        append(activity.date.format(dateFormatter)).append(TAB)
        append("${activity.type.sourceName}: ${activity.description}").append(TAB)
        append(activity.amount.formattedAmount)
    }

    private fun getDiscrepantCalculatedAmount(trade: Activity): BigDecimal? {
        val calculatedAmount = calculateExpectedAmount(trade)
        return if (trade.amount.value.compareTo(calculatedAmount) != 0) {
            calculatedAmount
        } else {
            null
        }
    }

    private fun calculateExpectedAmount(trade: Activity): BigDecimal {
        val baseAmount = trade.price!!.value * trade.shares!!.toBigDecimal()
        return when (trade.type) {
            ActivityType.BOUGHT -> baseAmount + trade.fee!!.value
            ActivityType.SOLD -> baseAmount - trade.fee!!.value
            else -> throw IllegalArgumentException("Unsupported trade type: ${trade.type}")
        }
    }

    private fun calculateSuggestedPrice(trade: Activity): BigDecimal {
        val adjustedAmount = when (trade.type) {
            ActivityType.BOUGHT -> trade.amount.value - trade.fee!!.value
            ActivityType.SOLD -> trade.amount.value + trade.fee!!.value
            else -> throw IllegalArgumentException("Unsupported trade type: ${trade.type}")
        }
        return adjustedAmount.setScale(6, RoundingMode.HALF_UP) / trade.shares!!.toBigDecimal()
    }

    private fun printDiscrepancies(trades: List<Activity>) {
        val discrepancies = trades.mapNotNull { trade ->
            val calculatedAmount = getDiscrepantCalculatedAmount(trade)
            if (calculatedAmount != null) {
                val suggestedPrice = calculateSuggestedPrice(trade)
                "${trade.date.format(dateFormatter)}$TAB" +
                        "${trade.type.sourceName}: ${trade.shares} ${trade.symbol}$TAB" +
                        "actual amount = [${trade.amount.formattedAmount}]; calculated amount = [${toAmount(calculatedAmount)}]; " +
                        "suggested share price = [$suggestedPrice]"
            } else null
        }

        if (discrepancies.isNotEmpty()) {
            println("Discrepancies:")
            discrepancies.forEach(::println)
        }
    }
}