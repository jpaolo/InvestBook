package org.home.prac.invest.book

import org.home.prac.invest.book.models.Activity
import org.home.prac.invest.book.models.ActivityType
import org.home.prac.invest.book.util.writeToClipboard
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
     * Converts activity to InvestBook execution format
     * Note: This method appears to be used but its implementation needs to be provided
     */
    fun toInvestBookExecutionFromActivity(activity: Activity, curRow: Int): String {

        val rowText = StringBuilder()
        val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
        rowText.append(activity.date.format(formatter)).append('\t')
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
     * Converts to InvestBook summary trade format
     * Note: This method appears to be used but its implementation needs to be provided
     */
    fun toInvestBookSummaryTrade(curRow: Int, curYear: Int): String {

        val rowText = StringBuilder()
        rowText.append("='executions $curYear'!$dateCol$curRow").append('\t')
        rowText.append("=CONCATENATE(SUBSTITUTE('executions $curYear'!$opCol$curRow,\":\",CONCATENATE(\" \",'executions $curYear'!$shareCol$curRow),1),\" @ \",TEXT('executions $curYear'!$priceCol$curRow,\"$0.00\"))").append('\t')
        rowText.append("='executions $curYear'!$amountCol$curRow")

        return rowText.toString()
    }

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
        val toClipboard = StringBuilder()
        val trades = activities.filter { it.type == ActivityType.SOLD || it.type == ActivityType.BOUGHT }

        when (mode) {
            "0" -> processForExeTab(trades, startingRow, toClipboard)
            "1" -> processForSummary(
                activities = activities,
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

        println(toClipboard)
        writeToClipboard(toClipboard.toString())

        // TODO: print verification summary
    }

    /**
     * Mode 0: from Ally Activities page to InvestBook current year tab
     */
    private fun processForExeTab(trades: List<Activity>, startingRow: Int, toClipboard: StringBuilder) {
        var row = startingRow
        trades.forEach { activity ->
            toClipboard.append(toInvestBookExecutionFromActivity(activity, row++))
            toClipboard.append(10.toChar()) // ascii-10 = NL
        }
    }

    /**
     * Mode 1: from InvestBook current year tab to InvestBook summary activities
     */
    private fun processForSummary(
        activities: List<Activity>,
        startingRow: Int,
        tradesSize: Int,
        toClipboard: StringBuilder,
        summTabStartingRow: Int?,
        summTabAmountCol: String?,
        summTabBalCol: String?
    ) {
        var row = startingRow + tradesSize - 1
        var summRow = summTabStartingRow
        activities.reversed().forEach { activity ->
            when (activity.type) {
                ActivityType.BOUGHT, ActivityType.SOLD -> {
                    toClipboard.append(toInvestBookSummaryTrade(row--, LocalDate.now().year))
                }
                else -> {
                    // TODO: handle other types
                    toClipboard.append("// TODO: handle other types")
                }
            }
            if (summTabStartingRow != null && summTabAmountCol != null && summTabBalCol != null) {
                toClipboard.append('\t').append("=$summTabBalCol$summRow+$summTabAmountCol${++summRow}")
            }
            toClipboard.append(10.toChar())
        }
    }

}