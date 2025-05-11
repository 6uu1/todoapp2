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
import com.todo.mygo.databinding.FragmentGanttBinding
import com.todo.mygo.gantt.data.PlannedTask // 替换 Event 为 PlannedTask
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
        ganttViewModel.plannedTasks.observe(viewLifecycleOwner) { tasks -> // 观察 plannedTasks
            tasks?.let { displayGanttData(it) }
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

    private fun displayGanttData(tasks: List<PlannedTask>) { // 修改参数类型为 PlannedTask
        if (tasks.isEmpty()) {
            chart.clear()
            chart.invalidate()
            return
        }

        // Sort tasks by start time for consistent Y-axis ordering
        val sortedTasks = tasks.sortedBy { it.startTime }
        val taskLabels = ArrayList<String>()
        val entries = ArrayList<BarEntry>()

        // Determine the overall min and max time for the X-axis
        var minStartTime = Long.MAX_VALUE
        var maxEndTime = Long.MIN_VALUE

        sortedTasks.forEachIndexed { index, task ->
            taskLabels.add(task.name) // 使用 PlannedTask.name 作为 Y 轴标签

            if (task.startTime < minStartTime) minStartTime = task.startTime
            if (task.endTime > maxEndTime) maxEndTime = task.endTime

            // For HorizontalBarChart, y is the task index.
            // The x-values for BarEntry should be a float array where the first value
            // is the start of the bar and the second is the end of the bar.
            // MPAndroidChart handles the range rendering.
            // IMPORTANT: MPAndroidChart's HorizontalBarChart expects the values for the bar
            // to be [valueFrom, valueTo].
            // However, for a Gantt chart, we want the bar to represent the duration.
            // The BarEntry's x value is the *end* point of the bar, and the y value is the stack value.
            // For a simple Gantt, we can represent the start time as the "low" value and end time as the "high" value
            // in a stacked bar, or more simply, use the start time as the beginning of the bar
            // and the duration as the length.
            // Let's use the approach where BarEntry takes (y, x) where y is task index,
            // and x is the end value. The bar starts at a specified value.
            // A common way for Gantt with MPAndroidChart is to use BarEntry(index, floatArrayOf(startTime, endTime))
            // where the chart then interprets these as ranges.
            // The BarEntry constructor for ranges is `BarEntry(float x, float[] yValues)`
            // For HorizontalBarChart, x is the position on the Y-axis (task index),
            // and yValues are the [start, end] for the X-axis (time).
            entries.add(BarEntry(index.toFloat(), floatArrayOf(task.startTime.toFloat(), task.endTime.toFloat())))
        }

        if (entries.isEmpty()) { // Should not happen if tasks is not empty, but good practice
            chart.clear()
            chart.invalidate()
            return
        }

        val dataSet = BarDataSet(entries, "Tasks")
        // TODO: Customize colors later, e.g., based on task type or status
        dataSet.color = resources.getColor(com.todo.mygo.R.color.design_default_color_primary, null) // Use a color from your resources
        dataSet.setDrawValues(false) // Values on bars might be cluttered for Gantt

        val barData = BarData(dataSet)
        barData.barWidth = 0.6f // Adjust for better visual appearance

        chart.data = barData

        // Configure X-axis (Time Axis)
        val xAxis = chart.xAxis
        xAxis.axisMinimum = minStartTime.toFloat()
        // Add a little padding to the max end time to ensure the last bar is fully visible
        xAxis.axisMaximum = maxEndTime.toFloat() + TimeUnit.HOURS.toMillis(12).toFloat() // Padding of 12 hours
        xAxis.valueFormatter = object : IndexAxisValueFormatter() {
            private val dateFormat = SimpleDateFormat("MMM dd HH:mm", Locale.getDefault()) // More precise format
            override fun getFormattedValue(value: Float): String {
                return dateFormat.format(Date(value.toLong()))
            }
        }
        // Adjust label count and granularity for better readability
        val timeRangeMillis = maxEndTime - minStartTime
        val daysInRange = TimeUnit.MILLISECONDS.toDays(timeRangeMillis)

        if (daysInRange < 1) {
            xAxis.granularity = TimeUnit.HOURS.toMillis(1).toFloat()
            xAxis.labelCount = (TimeUnit.MILLISECONDS.toHours(timeRangeMillis) / 2).toInt().coerceIn(2, 12)
        } else if (daysInRange < 7) {
            xAxis.granularity = TimeUnit.HOURS.toMillis(6).toFloat()
            xAxis.labelCount = (daysInRange * 2).toInt().coerceIn(4, 10)
        } else {
            xAxis.granularity = TimeUnit.DAYS.toMillis(1).toFloat()
            xAxis.labelCount = daysInRange.toInt().coerceIn(5, 14)
        }
        xAxis.isGranularityEnabled = true


        // Configure Y-axis (Task Names)
        val yAxisLeft = chart.axisLeft
        yAxisLeft.valueFormatter = IndexAxisValueFormatter(taskLabels)
        yAxisLeft.labelCount = taskLabels.size.coerceAtLeast(1) // Ensure at least 1 label if there's data
        yAxisLeft.axisMinimum = -0.5f
        yAxisLeft.axisMaximum = taskLabels.size - 0.5f
        yAxisLeft.granularity = 1f // Each task is one unit
        yAxisLeft.isGranularityEnabled = true


        chart.invalidate() // Refresh the chart
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}