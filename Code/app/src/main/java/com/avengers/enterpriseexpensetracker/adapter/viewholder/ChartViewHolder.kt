package com.avengers.enterpriseexpensetracker.adapter.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.avengers.enterpriseexpensetracker.R
import com.github.mikephil.charting.charts.PieChart

class ChartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private var chart: PieChart = itemView.findViewById(R.id.expenseChart)

    fun getChartView(): PieChart {
        return chart
    }
}
