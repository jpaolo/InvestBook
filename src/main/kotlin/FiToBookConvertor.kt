package org.home.prac.invest.book

import org.home.prac.invest.book.models.Activity
import org.home.prac.invest.book.models.ActivityType
import org.home.prac.invest.book.util.toAmount
import org.home.prac.invest.book.util.writeToClipboard
import java.math.RoundingMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class FiToBookConvertor {

    private val dateCol = "A"
    private val shareCol = "B"
    private val opCol = "C"
    private val priceCol = "D"
    private val feeCol = "E"
    private val amountCol = "F"

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
        summRow: Int?,
        summAmountCol: String?,
        summBalCol: String?
    ) {
        val dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
        val toClipboard = StringBuilder()
        val trades = activities.filter { it.type == ActivityType.BOUGHT || it.type == ActivityType.SOLD }

        when (mode) {
            "0" -> processForExeTab(trades, dateFormatter, startingRow, toClipboard)
            "1" -> processForSummary(
                activities = activities,
                dateFormat = dateFormatter,
                startingRow = startingRow,
                tradesSize = trades.size,
                toClipboard = toClipboard,
                summTabStartingRow = summRow,
                summTabAmountCol = summAmountCol,
                summTabBalCol = summBalCol
            )
        }

        // Remove the last newline character
        if (toClipboard.isNotEmpty()) {
            toClipboard.deleteCharAt(toClipboard.length - 1)
        }

        println("New clipboard:")
        println(toClipboard)
        writeToClipboard(toClipboard.toString())

        findAndPrintDiscrepancy(trades, dateFormatter)
    }

    private fun findAndPrintDiscrepancy(trades: List<Activity>, dateFormat: DateTimeFormatter) {
        var discrepancyHeaderAlreadyPrinted = false
        trades.forEach {
            if (it.type == ActivityType.BOUGHT || it.type == ActivityType.SOLD) {
                val calculatedAmount = it.price!!.value * it.shares!!.toBigDecimal() - it.fee!!.value
                if (it.amount.value.compareTo(calculatedAmount) != 0) {
                    if (!discrepancyHeaderAlreadyPrinted) {
                        println("Discrepancies:")
                    }
                    discrepancyHeaderAlreadyPrinted = true
                    val suggestedPrice =
                        (it.amount.value + it.fee.value).setScale(6, RoundingMode.HALF_UP) / it.shares.toBigDecimal()
                    println("${it.date.format(dateFormat)}\t" +
                            "${it.type.sourceName}: ${it.shares} ${it.symbol}\t" +
                            "actual amount = [${it.amount.formattedAmount}]; calculated amount = [${toAmount(calculatedAmount)}]; " +
                            "suggested share price = [$suggestedPrice]")
                }
            }
        }
    }

    /**
     * Converts Ally activity to InvestBook execution tab
     */
    private fun toInvestBookExecutionFromActivity(activity: Activity, dateFormat: DateTimeFormatter, curRow: Int): String {

        val rowText = StringBuilder()
        rowText.append(activity.date.format(dateFormat)).append('\t')
        rowText.append(activity.shares).append('\t')
        rowText.append("${activity.type.sourceName}: ${activity.symbol}").append('\t')
        rowText.append(activity.price!!.formattedAmount).append('\t')
        rowText.append(activity.fee!!.formattedAmount).append('\t')

        when (activity.type) {
            ActivityType.BOUGHT -> {
                rowText.append("=ROUND(-$shareCol$curRow*$priceCol$curRow-$feeCol$curRow,2)")
            }
            ActivityType.SOLD -> {
                rowText.append("=ROUND($shareCol$curRow*$priceCol$curRow-$feeCol$curRow,2)")
            }
            else -> {
                throw IllegalStateException("${activity.type}")
            }
        }

        return rowText.toString()
    }

    /**
     * Convert to InvestBook summary for trades
     */
    private fun toInvestBookSummaryTrade(curRow: Int, curYear: Int): String {

        val rowText = StringBuilder()
        rowText.append("='executions $curYear'!$dateCol$curRow").append('\t')
        rowText.append("=CONCATENATE(SUBSTITUTE('executions $curYear'!$opCol$curRow,\":\",CONCATENATE(\" \",'executions $curYear'!$shareCol$curRow),1),\" @ \",TEXT('executions $curYear'!$priceCol$curRow,\"$0.00\"))").append('\t')
        rowText.append("='executions $curYear'!$amountCol$curRow")

        return rowText.toString()
    }

    private fun toInvestBookSummaryNonTradeActivity(activity: Activity, dateFormat: DateTimeFormatter): String {
        val rowText = StringBuilder()
        rowText.append(activity.date.format(dateFormat)).append('\t')
        rowText.append("${activity.type.sourceName}: ${activity.description}").append('\t')
        rowText.append(activity.amount.formattedAmount)

        return rowText.toString()
    }

    /**
     * Mode 0: from Ally Activities page to InvestBook current year tab
     */
    private fun processForExeTab(trades: List<Activity>, dateFormat: DateTimeFormatter, startingRow: Int, toClipboard: StringBuilder) {
        var row = startingRow
        trades.forEach {
            toClipboard.append(toInvestBookExecutionFromActivity(it, dateFormat,row++))
            toClipboard.append(10.toChar()) // ascii-10 = NL
        }
    }

    /**
     * Mode 1: from InvestBook current year tab to InvestBook summary activities
     */
    private fun processForSummary(
        activities: List<Activity>,
        dateFormat: DateTimeFormatter,
        startingRow: Int,
        tradesSize: Int,
        toClipboard: StringBuilder,
        summTabStartingRow: Int?,
        summTabAmountCol: String?,
        summTabBalCol: String?
    ) {
        var row = startingRow + tradesSize - 1
        var summRow = summTabStartingRow
        activities.reversed().forEach {
            when (it.type) {
                ActivityType.BOUGHT, ActivityType.SOLD -> {
                    toClipboard.append(toInvestBookSummaryTrade(row--, LocalDate.now().year))
                }
                else -> {
                    toClipboard.append(toInvestBookSummaryNonTradeActivity(it, dateFormat))
                }
            }
            if (summTabStartingRow != null && summTabAmountCol != null && summTabBalCol != null) {
                toClipboard.append('\t').append("=$summTabBalCol$summRow+$summTabAmountCol${++summRow}")
            }
            toClipboard.append(10.toChar())
        }
    }

}