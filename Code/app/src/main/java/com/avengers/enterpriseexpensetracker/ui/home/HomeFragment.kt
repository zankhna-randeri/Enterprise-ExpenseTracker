package com.avengers.enterpriseexpensetracker.ui.home

import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.avengers.enterpriseexpensetracker.R
import com.avengers.enterpriseexpensetracker.modal.tracking.TrackScreenData
import com.avengers.enterpriseexpensetracker.util.AnalyticsHelper
import com.avengers.enterpriseexpensetracker.util.CurrencyFormatter
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.MPPointF

class HomeFragment : Fragment() {
    private lateinit var homeViewModel: HomeViewModel
    private var chart: PieChart? = null

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (activity != null) {
            AnalyticsHelper.getInstance().trackViewScreenEvent(activity!!, TrackScreenData("Home"))
        }

        initView(view)
    }

    private fun initView(view: View) {
        val textView: TextView = view.findViewById(R.id.text_home)
        homeViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        initChart(view)
    }

    private fun initChart(view: View) {
        chart = view.findViewById(R.id.expenseChart)
        chart?.setUsePercentValues(false)
        chart?.description?.isEnabled = false
        chart?.setExtraOffsets(5f, 10f, 5f, 5f)
        chart?.dragDecelerationFrictionCoef = 0.95f

        chart?.centerText = generateSpannableText(resources.getString(R.string.chart_dataset_label))

        chart?.isDrawHoleEnabled = true
        chart?.setHoleColor(Color.WHITE)

        chart?.setTransparentCircleColor(Color.WHITE);
        chart?.setTransparentCircleAlpha(110);

        chart?.holeRadius = 50f
        chart?.transparentCircleRadius = 55f

        chart?.setDrawCenterText(true)

        chart?.rotationAngle = 0f
        // enable rotation of the chart by touch
        chart?.isRotationEnabled = true
        chart?.isHighlightPerTapEnabled = true
        // add a selection listener
        //chart?.setOnChartValueSelectedListener(this);
        chart?.animateY(1400, Easing.EaseInOutQuad)
        setLegend(chart)
        chart?.setEntryLabelTypeface(context?.let { ResourcesCompat.getFont(it, R.font.app_font) })
        val textSizeInSp = resources.getDimension(R.dimen.font_5)
        chart?.setEntryLabelTextSize(textSizeInSp)
        //rounded
        chart?.setDrawRoundedSlices(true)
        setData(3, 10f)
    }

    private fun generateSpannableText(source: String): CharSequence? {
        val spannableString = SpannableString(source)
        val typeface = context?.let { ResourcesCompat.getFont(it, R.font.app_font) }
        spannableString.setSpan(typeface?.style?.let { StyleSpan(typeface.style) },
                0,
                source.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannableString
    }

    private fun setLegend(chart: PieChart?) {
        val l = chart?.legend
        l?.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        l?.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        l?.orientation = Legend.LegendOrientation.HORIZONTAL
        l?.setDrawInside(false)
        l?.xEntrySpace = 20f
        l?.yEntrySpace = 0f
        l?.yOffset = 6f
        val typeFace = context?.let { ResourcesCompat.getFont(it, R.font.app_font) }
        l?.typeface = typeFace
        // affects legend text size
        l?.textSize = resources.getDimension(R.dimen.font_5)
        // this affects legend square shape
        l?.formSize = 10f
        context?.let { ContextCompat.getColor(it, android.R.color.white) }
                ?.let { chart?.setEntryLabelColor(it) }
    }

    private fun setData(count: Int, range: Float) {
        val entries = ArrayList<PieEntry>()
        for (i in 0 until count) {
            entries.add(PieEntry((Math.random() * range + range / 5).toFloat(),
                    "Food",
                    ContextCompat.getDrawable(activity!!.applicationContext, R.mipmap.ic_launcher_round)))
        }
        val dataSet = PieDataSet(entries, "")
        setDataSetStyle(dataSet)
        val data = PieData(dataSet)
        chart?.let { setPieDataStyle(data, it) }
        chart?.data = data
        // undo all highlights
        chart?.highlightValues(null)
        chart?.invalidate()
    }

    private fun setPieDataStyle(data: PieData,
                                chart: PieChart) {
        data.setValueFormatter(CurrencyFormatter(chart))
        val textSizeInSp = resources.getDimension(R.dimen.font_5)
        data.setValueTextSize(textSizeInSp)
        context?.let { ContextCompat.getColor(it, android.R.color.white) }?.let { data.setValueTextColor(it) }
        val typeFace = context?.let { ResourcesCompat.getFont(it, R.font.app_font) }
        data.setValueTypeface(typeFace)
    }

    private fun setDataSetStyle(dataSet: PieDataSet) {
        dataSet.setDrawIcons(false)
        dataSet.sliceSpace = 3f
        dataSet.iconsOffset = MPPointF(0f, 40f)
        dataSet.selectionShift = 5f
        // add a lot of colors
        val colors: ArrayList<Int> = ArrayList()
        context?.let { ContextCompat.getColor(it, R.color.color_chart_1) }?.let { colors.add(it) }
        context?.let { ContextCompat.getColor(it, R.color.color_chart_2) }?.let { colors.add(it) }
        context?.let { ContextCompat.getColor(it, R.color.color_chart_3) }?.let { colors.add(it) }
        dataSet.colors = colors
    }
}