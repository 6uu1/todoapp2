package com.todo.mygo.calendar.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.todo.mygo.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CalendarFragment : Fragment() {

    private lateinit var calendarViewModel: CalendarViewModel
    private lateinit var tvYearMonth: TextView
    private lateinit var calendarGrid: RecyclerView
    private lateinit var ganttViewContainer: FrameLayout
    private lateinit var ganttChart: HorizontalBarChart
    private lateinit var fabMenu: FloatingActionButton
    private lateinit var btnAiSuggestion: Button
    private lateinit var btnAddEvent: ImageButton
    private lateinit var btnMenu: ImageButton

    private var isCalendarViewVisible = true
    private lateinit var calendarAdapter: CalendarAdapter
    private val currentMonth = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_calendar_improved, container, false)

        calendarViewModel = ViewModelProvider(this).get(CalendarViewModel::class.java)

        // 初始化视图
        tvYearMonth = root.findViewById(R.id.tvYearMonth)
        calendarGrid = root.findViewById(R.id.calendarGrid)
        ganttViewContainer = root.findViewById(R.id.ganttViewContainer)
        ganttChart = root.findViewById(R.id.ganttChart)
        fabMenu = root.findViewById(R.id.fabMenu)
        btnAiSuggestion = root.findViewById(R.id.btnAiSuggestion)
        btnAddEvent = root.findViewById(R.id.btnAddEvent)
        btnMenu = root.findViewById(R.id.btnMenu)

        setupCalendarView()
        setupGanttView()
        setupButtons()
        observeViewModel()

        // 设置初始日期
        val initialDate = Calendar.getInstance()
        calendarViewModel.setSelectedDate(initialDate.time)
        updateMonthDisplay(initialDate)

        updateViewVisibility() // 初始视图可见性设置

        return root
    }

    private fun setupCalendarView() {
        // 设置日历网格
        calendarAdapter = CalendarAdapter(
            requireContext(),
            currentMonth,
            emptyList()
        ) { date ->
            // 日期点击回调
            calendarViewModel.setSelectedDate(date)
            Toast.makeText(context, "Selected date: ${dateFormat.format(date)}", Toast.LENGTH_SHORT).show()
        }

        calendarGrid.apply {
            layoutManager = GridLayoutManager(context, 7) // 7列，对应一周7天
            adapter = calendarAdapter
        }
    }

    private fun setupGanttView() {
        // 甘特图设置将在GanttFragment中处理
    }

    private fun setupButtons() {
        // 设置菜单按钮
        btnMenu.setOnClickListener { view ->
            showViewSwitchMenu(view)
        }

        // 设置FAB菜单按钮
        fabMenu.setOnClickListener { view ->
            showViewSwitchMenu(view)
        }

        // 设置添加事件按钮
        btnAddEvent.setOnClickListener {
            Toast.makeText(context, "Add Event clicked (Placeholder)", Toast.LENGTH_SHORT).show()
            // 后续将打开添加事件的对话框或页面
        }

        // 设置AI建议按钮
        btnAiSuggestion.setOnClickListener {
            Toast.makeText(context, "AI Suggestion clicked (Placeholder)", Toast.LENGTH_SHORT).show()
            // 后续将触发AI建议逻辑
        }
    }

    private fun showViewSwitchMenu(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.calendar_gantt_menu, popupMenu.menu)

        // 动态设置切换视图的文本
        val switchMenuItem = popupMenu.menu.findItem(R.id.action_switch_view)
        switchMenuItem.title = if (isCalendarViewVisible) {
            getString(R.string.switch_to_gantt)
        } else {
            getString(R.string.switch_to_calendar)
        }

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_switch_view -> {
                    isCalendarViewVisible = !isCalendarViewVisible
                    updateViewVisibility()
                    true
                }
                R.id.action_add_plan -> {
                    Toast.makeText(context, "Add Plan clicked (Placeholder)", Toast.LENGTH_SHORT).show()
                    // 后续将打开添加计划的对话框或页面
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun updateViewVisibility() {
        if (isCalendarViewVisible) {
            // 显示日历视图
            calendarGrid.visibility = View.VISIBLE
            tvYearMonth.visibility = View.VISIBLE
            ganttViewContainer.visibility = View.GONE
        } else {
            // 显示甘特图视图
            calendarGrid.visibility = View.GONE
            tvYearMonth.visibility = View.GONE
            ganttViewContainer.visibility = View.VISIBLE
        }
    }

    private fun updateMonthDisplay(calendar: Calendar) {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1 // 月份从0开始，需要+1
        tvYearMonth.text = "$year / $month"

        // 更新日历适配器的月份
        calendarAdapter.updateMonth(calendar)
    }

    private fun observeViewModel() {
        // 观察选中的日期
        calendarViewModel.selectedDate.observe(viewLifecycleOwner) { date ->
            val calendar = Calendar.getInstance()
            calendar.time = date

            // 更新选中的日期
            calendarAdapter.setSelectedDate(date)

            // 如果选中的月份与当前显示的月份不同，则更新月份显示
            if (calendar.get(Calendar.YEAR) != currentMonth.get(Calendar.YEAR) ||
                calendar.get(Calendar.MONTH) != currentMonth.get(Calendar.MONTH)) {
                currentMonth.time = date
                updateMonthDisplay(currentMonth)
            }
        }

        // 观察事件列表
        calendarViewModel.allEvents.observe(viewLifecycleOwner) { events ->
            calendarAdapter.updateEvents(events)
        }
    }

    // 日期格式化工具
    companion object {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    }
}