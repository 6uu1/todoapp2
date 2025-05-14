package com.todo.mygo.calendar.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.todo.mygo.R
import com.todo.mygo.calendar.data.Event
import java.util.Calendar
import java.util.Date

class CalendarAdapter(
    private val context: Context,
    private var currentMonth: Calendar,
    private var events: List<Event> = emptyList(),
    private val onDayClickListener: (Date) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.DayViewHolder>() {

    private val days = mutableListOf<CalendarDay>()
    private var selectedDate: Date? = null

    init {
        calculateDays()
    }

    fun updateMonth(calendar: Calendar) {
        currentMonth = calendar.clone() as Calendar
        calculateDays()
        notifyDataSetChanged()
    }

    fun updateEvents(newEvents: List<Event>) {
        events = newEvents
        notifyDataSetChanged()
    }

    fun setSelectedDate(date: Date) {
        selectedDate = date
        notifyDataSetChanged()
    }

    private fun calculateDays() {
        days.clear()

        // 获取当前月的第一天是星期几
        val firstDayOfMonth = currentMonth.clone() as Calendar
        firstDayOfMonth.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = firstDayOfMonth.get(Calendar.DAY_OF_WEEK) - 1 // 调整为从0开始

        // 添加上个月的日期填充第一周
        val prevMonth = currentMonth.clone() as Calendar
        prevMonth.add(Calendar.MONTH, -1)
        val daysInPrevMonth = prevMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (i in 0 until firstDayOfWeek) {
            val day = daysInPrevMonth - firstDayOfWeek + i + 1
            val date = prevMonth.clone() as Calendar
            date.set(Calendar.DAY_OF_MONTH, day)
            days.add(CalendarDay(date.time, false))
        }

        // 添加当前月的日期
        val daysInMonth = currentMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (i in 1..daysInMonth) {
            val date = currentMonth.clone() as Calendar
            date.set(Calendar.DAY_OF_MONTH, i)
            days.add(CalendarDay(date.time, true))
        }

        // 添加下个月的日期填充最后一周
        val nextMonth = currentMonth.clone() as Calendar
        nextMonth.add(Calendar.MONTH, 1)
        val remainingCells = 42 - days.size // 6行7列 = 42个单元格
        for (i in 1..remainingCells) {
            val date = nextMonth.clone() as Calendar
            date.set(Calendar.DAY_OF_MONTH, i)
            days.add(CalendarDay(date.time, false))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val day = days[position]
        holder.bind(day)
    }

    override fun getItemCount(): Int = days.size

    inner class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dayContainer: ConstraintLayout = itemView.findViewById(R.id.dayContainer)
        private val tvDayNumber: TextView = itemView.findViewById(R.id.tvDayNumber)
        private val tvHolidayIndicator: TextView = itemView.findViewById(R.id.tvHolidayIndicator)
        private val tvLunarDate: TextView = itemView.findViewById(R.id.tvLunarDate)
        private val tvHolidayName: TextView = itemView.findViewById(R.id.tvHolidayName)
        private val eventIndicator: View = itemView.findViewById(R.id.eventIndicator)

        fun bind(day: CalendarDay) {
            val calendar = Calendar.getInstance()
            calendar.time = day.date

            // 设置日期数字
            tvDayNumber.text = calendar.get(Calendar.DAY_OF_MONTH).toString()

            // 设置当前月和非当前月的样式
            if (day.isCurrentMonth) {
                tvDayNumber.setTextColor(ContextCompat.getColor(context, android.R.color.black))
                dayContainer.setBackgroundColor(ContextCompat.getColor(context, android.R.color.white))
            } else {
                tvDayNumber.setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
                dayContainer.setBackgroundColor(ContextCompat.getColor(context, android.R.color.white))
            }

            // 设置选中日期的样式
            if (selectedDate != null) {
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.time = selectedDate!!
                if (calendar.get(Calendar.YEAR) == selectedCalendar.get(Calendar.YEAR) &&
                    calendar.get(Calendar.MONTH) == selectedCalendar.get(Calendar.MONTH) &&
                    calendar.get(Calendar.DAY_OF_MONTH) == selectedCalendar.get(Calendar.DAY_OF_MONTH)
                ) {
                    dayContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.blue_100))
                    tvDayNumber.setTextColor(ContextCompat.getColor(context, R.color.blue_500))
                }
            }

            // 设置今天的样式
            val today = Calendar.getInstance()
            if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                calendar.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)
            ) {
                tvDayNumber.setTextColor(ContextCompat.getColor(context, R.color.teal_700))
                tvDayNumber.setTextSize(18f)
            }

            // 设置节假日信息（这里只是示例，实际应该从数据源获取）
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
            val month = calendar.get(Calendar.MONTH)
            
            // 清除之前的节假日信息
            tvHolidayIndicator.visibility = View.GONE
            tvLunarDate.text = ""
            tvHolidayName.text = ""
            
            // 示例：设置5月1日为劳动节
            if (month == Calendar.MAY && dayOfMonth == 1) {
                tvHolidayIndicator.visibility = View.VISIBLE
                tvLunarDate.text = "初五"
                tvHolidayName.text = "劳动节"
            }
            
            // 示例：设置5月4日为青年节
            if (month == Calendar.MAY && dayOfMonth == 4) {
                tvHolidayName.text = "青年节"
            }

            // 检查是否有事件
            val dayStart = Calendar.getInstance()
            dayStart.time = day.date
            dayStart.set(Calendar.HOUR_OF_DAY, 0)
            dayStart.set(Calendar.MINUTE, 0)
            dayStart.set(Calendar.SECOND, 0)
            dayStart.set(Calendar.MILLISECOND, 0)
            
            val dayEnd = Calendar.getInstance()
            dayEnd.time = day.date
            dayEnd.set(Calendar.HOUR_OF_DAY, 23)
            dayEnd.set(Calendar.MINUTE, 59)
            dayEnd.set(Calendar.SECOND, 59)
            dayEnd.set(Calendar.MILLISECOND, 999)
            
            val hasEvents = events.any { 
                it.startTime >= dayStart.timeInMillis && it.startTime <= dayEnd.timeInMillis 
            }
            
            eventIndicator.visibility = if (hasEvents) View.VISIBLE else View.GONE

            // 设置点击事件
            itemView.setOnClickListener {
                onDayClickListener(day.date)
            }
        }
    }

    data class CalendarDay(
        val date: Date,
        val isCurrentMonth: Boolean
    )
}
