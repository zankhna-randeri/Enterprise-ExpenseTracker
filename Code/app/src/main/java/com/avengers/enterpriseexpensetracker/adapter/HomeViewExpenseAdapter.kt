package com.avengers.enterpriseexpensetracker.adapter

import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.avengers.enterpriseexpensetracker.R
import com.avengers.enterpriseexpensetracker.adapter.viewholder.ChartViewHolder
import com.avengers.enterpriseexpensetracker.adapter.viewholder.ExpenseReportViewHolder
import com.avengers.enterpriseexpensetracker.modal.ExpenseReport
import com.avengers.enterpriseexpensetracker.modal.response.CategoryWiseTotalResponse
import com.avengers.enterpriseexpensetracker.util.Constants
import com.avengers.enterpriseexpensetracker.util.CurrencyFormatter
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.MPPointF

class HomeViewExpenseAdapter(private var context: Context,
                             private var categoryWiseTotal: CategoryWiseTotalResponse,
                             private var pendingExpenses: List<ExpenseReport>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_HEADER = 0
    private val VIEW_LIST = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        lateinit var viewHolder: RecyclerView.ViewHolder
        val inflater = LayoutInflater.from(parent.context)
        when (viewType) {
            VIEW_HEADER -> {
                val reqViewHolder = inflater.inflate(R.layout.item_pie_chart, parent, false)
                viewHolder = ChartViewHolder(reqViewHolder)
            }
            VIEW_LIST -> {
                val reqViewHolder = inflater.inflate(R.layout.item_expense_report, parent, false)
                viewHolder = ExpenseReportViewHolder(reqViewHolder)
            }
        }

        return viewHolder
    }

    override fun getItemCount(): Int {
        return pendingExpenses.size + 1
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ChartViewHolder) {
            val chart = holder.getChartView()
            initChart(chart)
        } else if (holder is ExpenseReportViewHolder) {
            holder.getDateView().text = pendingExpenses[position - 1].getCreationDate()
            holder.getDateView().setBackgroundResource(R.drawable.report_pending)
            holder.getNameView().text = pendingExpenses[position - 1].getName()
            holder.getAmountView().text = context.resources.getString(R.string.txt_currency_dollar_amount,
                    pendingExpenses[position - 1].getTotal())
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0) {
            return VIEW_HEADER
        }

        return VIEW_LIST
    }

    private fun initChart(chart: PieChart) {
        chart.setUsePercentValues(false)
        chart.description?.isEnabled = false
        chart.setExtraOffsets(5f, 10f, 5f, 5f)
        chart.dragDecelerationFrictionCoef = 0.95f

        chart.centerText = generateSpannableText(context.getString(R.string.chart_dataset_label))

        chart.isDrawHoleEnabled = true
        chart.setHoleColor(Color.WHITE)

        chart.setTransparentCircleColor(Color.WHITE);
        chart.setTransparentCircleAlpha(110);

        chart.holeRadius = 50f
        chart.transparentCircleRadius = 55f

        chart.setDrawCenterText(true)

        chart.rotationAngle = 0f
        // enable rotation of the chart by touch
        chart.isRotationEnabled = true
        chart.isHighlightPerTapEnabled = true
        // add a selection listener
        //chart?.setOnChartValueSelectedListener(this);
        chart.animateY(1400, Easing.EaseInOutQuad)
        setLegend(chart)
        chart.setEntryLabelTypeface(ResourcesCompat.getFont(context, R.font.app_font))
        val textSizeInSp = context.resources.getDimension(R.dimen.font_4)
        chart.setEntryLabelTextSize(textSizeInSp)
        //rounded
        chart.setDrawRoundedSlices(true)
        setData(chart)
    }

    private fun generateSpannableText(source: String): CharSequence? {
        val spannableString = SpannableString(source)
        val typeface = ResourcesCompat.getFont(context, R.font.app_font)
        spannableString.setSpan(typeface?.style?.let { StyleSpan(typeface.style) },
                0,
                source.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannableString
    }

    private fun setLegend(chart: PieChart) {
        val l = chart.legend
        l?.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        l?.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        l?.orientation = Legend.LegendOrientation.HORIZONTAL
        l?.setDrawInside(false)
        l?.xEntrySpace = 20f
        l?.yEntrySpace = 0f
        l?.yOffset = 6f
        val typeFace = ResourcesCompat.getFont(context, R.font.app_font)
        l?.typeface = typeFace
        // affects legend text size
        l?.textSize = context.resources.getDimension(R.dimen.font_4)
        // this affects legend square shape
        l?.formSize = 10f
        chart.setEntryLabelColor(ContextCompat.getColor(context, android.R.color.white))
    }

    private fun setData(chart: PieChart) {
        val entries = ArrayList<PieEntry>()
        if (categoryWiseTotal.getAccommodationExpense() > 0) {
            entries.add(PieEntry(categoryWiseTotal.getAccommodationExpense(),
                    Constants.Companion.ExpenseType.Accommodation.name))
        }

        if (categoryWiseTotal.getFoodExpense() > 0) {
            entries.add(PieEntry(categoryWiseTotal.getFoodExpense(),
                    Constants.Companion.ExpenseType.Food.name))
        }

        if (categoryWiseTotal.getOtherExpense() > 0) {
            entries.add(PieEntry(categoryWiseTotal.getOtherExpense(),
                    Constants.Companion.ExpenseType.Other.name))
        }

        if (categoryWiseTotal.getTravelExpense() > 0) {
            entries.add(PieEntry(categoryWiseTotal.getTravelExpense(),
                    Constants.Companion.ExpenseType.Travel.name))
        }

        val dataSet = PieDataSet(entries, "")
        setDataSetStyle(dataSet)
        val data = PieData(dataSet)
        setPieDataStyle(data, chart)
        chart.data = data
        // undo all highlights
        chart.highlightValues(null)
        chart.invalidate()
    }

    private fun setPieDataStyle(data: PieData,
                                chart: PieChart) {
        data.setValueFormatter(CurrencyFormatter(chart))
        val textSizeInSp = context.resources.getDimension(R.dimen.font_4)
        data.setValueTextSize(textSizeInSp)
        data.setValueTextColor(ContextCompat.getColor(context, android.R.color.white))
        val typeFace = ResourcesCompat.getFont(context, R.font.app_font)
        data.setValueTypeface(typeFace)
    }

    private fun setDataSetStyle(dataSet: PieDataSet) {
        dataSet.setDrawIcons(false)
        dataSet.sliceSpace = 3f
        dataSet.iconsOffset = MPPointF(0f, 40f)
        dataSet.selectionShift = 5f
        // add a lot of colors
        val colors: ArrayList<Int> = ArrayList()
        colors.add(ContextCompat.getColor(context, R.color.color_chart_1))
        colors.add(ContextCompat.getColor(context, R.color.color_chart_2))
        colors.add(ContextCompat.getColor(context, R.color.color_chart_3))
        colors.add(ContextCompat.getColor(context, R.color.color_chart_4))
        dataSet.colors = colors
    }
}