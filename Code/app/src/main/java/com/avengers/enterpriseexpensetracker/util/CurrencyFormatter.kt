package com.avengers.enterpriseexpensetracker.util

import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.NumberFormat
import java.util.*

class CurrencyFormatter : ValueFormatter {
    private var format: NumberFormat? = null
    private var pieChart: PieChart? = null

    constructor() : super() {
        format = NumberFormat.getCurrencyInstance(Locale.US)
        format?.minimumFractionDigits = 2
    }

    constructor(pieChart: PieChart) : this() {
        this.pieChart = pieChart
    }

    override fun getFormattedValue(value: Float): String {
        return format?.format(value).toString()
    }

    override fun getPieLabel(value: Float, pieEntry: PieEntry?): String {
        return format?.format(value).toString()
    }
}
