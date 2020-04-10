package com.avengers.enterpriseexpensetracker.ui.home

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.avengers.enterpriseexpensetracker.R
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.MPPointF
import java.util.*
import kotlin.collections.ArrayList

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private var chart: PieChart? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        val textView: TextView = root.findViewById(R.id.text_home)

        homeViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chart = view.findViewById(R.id.expenseChart)
        chart?.setUsePercentValues(true)
        chart?.description?.isEnabled = false
        chart?.setExtraOffsets(5f, 10f, 5f, 5f)
        chart?.dragDecelerationFrictionCoef = 0.95f

        chart?.setCenterTextTypeface(Typeface.createFromAsset(activity?.assets,
                String.format(Locale.US, "font/%s", "avenir_roman.otf")));

        chart?.centerText = generateCenterSpannableText()

        chart?.isDrawHoleEnabled = true
        chart?.setHoleColor(Color.WHITE)

        chart?.setTransparentCircleColor(Color.WHITE);
        chart?.setTransparentCircleAlpha(110);

        chart?.holeRadius = 58f
        chart?.transparentCircleRadius = 61f

        chart?.setDrawCenterText(true)

        chart?.rotationAngle = 0f
        // enable rotation of the chart by touch
        chart?.isRotationEnabled = true
        chart?.isHighlightPerTapEnabled = true
    }

    private fun generateCenterSpannableText(): SpannableString {
        val s = SpannableString("MPAndroidChart\ndeveloped by Philipp Jahoda");
        s.setSpan(RelativeSizeSpan(1.7f), 0, 14, 0);
        s.setSpan(StyleSpan(Typeface.NORMAL), 14, s.length - 15, 0);
        s.setSpan(ForegroundColorSpan(Color.GRAY), 14, s.length - 15, 0);
        s.setSpan(RelativeSizeSpan(.8f), 14, s.length - 15, 0);
        s.setSpan(StyleSpan(Typeface.ITALIC), s.length - 14, s.length, 0);
        s.setSpan(ForegroundColorSpan(ColorTemplate.getHoloBlue()), s.length - 14, s.length, 0);
        return s
    }

    private fun setData(count: Int, range: Float) {
        val entries: ArrayList<PieEntry> = ArrayList()
        // NOTE: The order of the entries when being added to the entries array determines their position around the center of
        // the chart.
        for (i in 0 until count) {
            entries.add(PieEntry((Math.random() * range + range / 5).toFloat(),
                    "Food",
                    ContextCompat.getDrawable(activity!!.applicationContext,
                            R.mipmap.ic_launcher_round)))
        }
        val dataSet = PieDataSet(entries, "Election Results")
        dataSet.setDrawIcons(false)
        dataSet.sliceSpace = 3f
        dataSet.iconsOffset = MPPointF(0f, 40f)
        dataSet.selectionShift = 5f
        // add a lot of colors
        val colors: ArrayList<Int> = ArrayList()
        for (c in ColorTemplate.VORDIPLOM_COLORS) colors.add(c)
        for (c in ColorTemplate.JOYFUL_COLORS) colors.add(c)
        for (c in ColorTemplate.COLORFUL_COLORS) colors.add(c)
        for (c in ColorTemplate.LIBERTY_COLORS) colors.add(c)
        for (c in ColorTemplate.PASTEL_COLORS) colors.add(c)
        colors.add(ColorTemplate.getHoloBlue())
        dataSet.colors = colors
        //dataSet.setSelectionShift(0f);
        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter(chart))
        data.setValueTextSize(11f)
        data.setValueTextColor(Color.WHITE)
        data.setValueTypeface(Typeface.createFromAsset(activity?.assets,
                String.format(Locale.US, "font/%s", "avenir_roman.otf")))
        chart!!.data = data
        // undo all highlights
        chart!!.highlightValues(null)
        chart!!.invalidate()
    }

}