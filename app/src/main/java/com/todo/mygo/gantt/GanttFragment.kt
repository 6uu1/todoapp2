package com.todo.mygo.gantt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.todo.mygo.calendar.data.Event
import com.todo.mygo.databinding.FragmentGanttBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class GanttFragment : Fragment() {

    private var _binding: FragmentGanttBinding? = null
    private val binding get() = _binding!!
    private val ganttViewModel: GanttViewModel by viewModels()

    private lateinit var chart: HorizontalBarChart

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGanttBinding.inflate(inflater, container, false)
        chart = binding.ganttChart
        setupGanttChart()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ganttViewModel.allEvents.observe(viewLifecycleOwner) { events ->
            events?.let { displayGanttData(it) }
        }
    }

    private fun setupGanttChart() {
        chart.description.isEnabled = false
        chart.setDrawGridBackground(false)
        chart.setDrawValueAboveBar(false) // Usually false for Gantt for clarity
        chart.setPinchZoom(true)
        chart.isDragEnabled = true
        chart.setScaleEnabled(true)
        chart.setFitBars(true) // make the bars fit into the viewport

        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(true)
        xAxis.setDrawAxisLine(true)
        xAxis.granularity = 1f // Default, might need adjustment based on time scale
        xAxis.valueFormatter = IndexAxisValueFormatter() // Placeholder, will be replaced by date formatter

        val yAxisLeft = chart.axisLeft
        yAxisLeft.setDrawGridLines(false)
        yAxisLeft.setDrawAxisLine(true)
        yAxisLeft.setDrawLabels(true) // We need to show task names or identifiers
        yAxisLeft.granularity = 1f
        yAxisLeft.isInverted = true // Typically tasks are listed from top to bottom

        val yAxisRight = chart.axisRight
        yAxisRight.isEnabled = false

        chart.legend.isEnabled = false // Usually not needed for a simple Gantt
        // chart.animateY(1000) // Can be enabled if desired
    }

    private fun displayGanttData(events: List<Event>) {
        if (events.isEmpty()) {
            chart.clear()
            chart.invalidate()
            return
        }

        // Sort events by start time for consistent Y-axis ordering (optional)
        val sortedEvents = events.sortedBy { it.startTime }
        val taskLabels = ArrayList<String>()
        val entries = ArrayList<BarEntry>()

        // Determine the overall min and max time for the X-axis
        var minStartTime = Long.MAX_VALUE
        var maxEndTime = Long.MIN_VALUE

        sortedEvents.forEachIndexed { index, event ->
            taskLabels.add(event.title) // Use event title for Y-axis labels

            // For HorizontalBarChart, y is the task index, x is the range [startTime, endTime]
            // We need to convert timestamps to a relative float value for the chart.
            // Let's use days since a reference point (e.g., earliest start time).
            if (event.startTime < minStartTime) minStartTime = event.startTime
            if (event.endTime > maxEndTime) maxEndTime = event.endTime

            // BarEntry expects a single y-value and an array of x-values for ranges.
            // The x-values here will be the start and end times.
            // For MPAndroidChart, the actual values for start/end are used.
            // The first value in the array is the "shadow" or "base" and the second is the "actual" bar end.
            // For Gantt, we want the bar to start at startTime and end at endTime.
            // So, we provide {startTime, endTime}
            entries.add(BarEntry(index.toFloat(), floatArrayOf(event.startTime.toFloat(), event.endTime.toFloat())))
        }

        if (entries.isEmpty()) {
            chart.clear()
            chart.invalidate()
            return
        }
        
        val dataSet = BarDataSet(entries, "Tasks")
        // Customize colors later, e.g., based on task type or status
        dataSet.color = resources.getColor(com.todo.mygo.R.color.design_default_color_primary, null)
        dataSet.setDrawValues(true) // Show values on bars (e.g., duration) - might be too cluttered

        val barData = BarData(dataSet)
        barData.barWidth = 0.6f // Adjust bar width as needed

        chart.data = barData

        // Configure X-axis (Time)
        val xAxis = chart.xAxis
        xAxis.axisMinimum = minStartTime.toFloat()
        xAxis.axisMaximum = maxEndTime.toFloat() + TimeUnit.DAYS.toMillis(1).toFloat() // Add some padding
        xAxis.valueFormatter = object : IndexAxisValueFormatter() {
            private val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
            override fun getFormattedValue(value: Float): String {
                return dateFormat.format(Date(value.toLong()))
            }
        }
        xAxis.labelCount = 5 // Adjust as needed, force to show N labels
        xAxis.isGranularityEnabled = true
        xAxis.granularity = TimeUnit.DAYS.toMillis(1).toFloat() // Minimum interval of 1 day

        // Configure Y-axis (Tasks)
        val yAxisLeft = chart.axisLeft
        yAxisLeft.valueFormatter = IndexAxisValueFormatter(taskLabels)
        yAxisLeft.labelCount = taskLabels.size
        yAxisLeft.axisMinimum = -0.5f // To ensure first label is fully visible
        yAxisLeft.axisMaximum = taskLabels.size - 0.5f // To ensure last label is fully visible


        chart.invalidate() // Refresh chart
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}