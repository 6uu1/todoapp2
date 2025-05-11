package com.todo.mygo.calendar.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.todo.mygo.R
import com.todo.mygo.calendar.data.Event
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.collections.joinToString

class CalendarFragment : Fragment() {

    private lateinit var calendarViewModel: CalendarViewModel
    private lateinit var calendarView: CalendarView
    private lateinit var selectedDateTextView: TextView
    private lateinit var eventsRecyclerView: RecyclerView
    private lateinit var eventAdapter: EventAdapter
    private lateinit var fabAddEvent: FloatingActionButton

    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_calendar, container, false) // You'll need to create this layout

        calendarViewModel = ViewModelProvider(this).get(CalendarViewModel::class.java)

        calendarView = root.findViewById(R.id.calendarView)
        selectedDateTextView = root.findViewById(R.id.selectedDateTextView)
        eventsRecyclerView = root.findViewById(R.id.eventsRecyclerView)
        fabAddEvent = root.findViewById(R.id.fabAddEvent)

        setupCalendarView()
        setupRecyclerView()
        setupFab()
        observeViewModel()

        // Set initial date
        val initialDate = Calendar.getInstance()
        calendarViewModel.setSelectedDate(initialDate.time)


        return root
    }

    private fun setupCalendarView() {
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth, 0, 0, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            calendarViewModel.setSelectedDate(calendar.time)
        }
    }

    private fun setupRecyclerView() {
        eventAdapter = EventAdapter(
            onItemClicked = { event ->
                showEventDetailsDialog(event)
            },
            onDeleteClicked = { event ->
                showDeleteConfirmationDialog(event)
            }
        )
        eventsRecyclerView.layoutManager = LinearLayoutManager(context)
        eventsRecyclerView.adapter = eventAdapter
    }

    private fun setupFab() {
        fabAddEvent.setOnClickListener {
            showEventDetailsDialog(null) // Pass null for new event
        }
    }

    private fun showEventDetailsDialog(event: Event?) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_event_details, null)
        val titleEditText = dialogView.findViewById<TextInputEditText>(R.id.titleEditText)
        val descriptionEditText = dialogView.findViewById<TextInputEditText>(R.id.descriptionEditText)
        val startTimeTextView = dialogView.findViewById<TextView>(R.id.startTimeTextView)
        val endTimeTextView = dialogView.findViewById<TextView>(R.id.endTimeTextView)
        val prioritySpinner = dialogView.findViewById<Spinner>(R.id.prioritySpinner)
        val categoryEditText = dialogView.findViewById<TextInputEditText>(R.id.categoryEditText)
        val tagsEditText = dialogView.findViewById<TextInputEditText>(R.id.tagsEditText)
        val saveButton = dialogView.findViewById<Button>(R.id.saveButton)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)
        val deleteButton = dialogView.findViewById<Button>(R.id.deleteButton) // Will add this later

        // Priority Spinner
        val priorities = listOf("低", "中", "高")
        val priorityAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, priorities)
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        prioritySpinner.adapter = priorityAdapter

        val selectedStartTime = Calendar.getInstance()
        val selectedEndTime = Calendar.getInstance()

        if (event != null) {
            titleEditText.setText(event.title)
            descriptionEditText.setText(event.description)
            selectedStartTime.timeInMillis = event.startTime
            selectedEndTime.timeInMillis = event.endTime
            startTimeTextView.text = dateTimeFormat.format(event.startTime)
            endTimeTextView.text = dateTimeFormat.format(event.endTime)
            prioritySpinner.setSelection(event.priority) // Assuming priority is 0, 1, 2
            categoryEditText.setText(event.category)
            tagsEditText.setText(event.tags ?: "")
            // deleteButton.visibility = View.VISIBLE // Show delete for existing events
        } else {
            // For new events, default start time to selected date on calendar or current time
            val currentSelectedDate = calendarViewModel.selectedDate.value ?: Date()
            selectedStartTime.time = currentSelectedDate
            // Default end time to one hour after start time
            selectedEndTime.time = currentSelectedDate
            selectedEndTime.add(Calendar.HOUR_OF_DAY, 1)

            startTimeTextView.text = dateTimeFormat.format(selectedStartTime.time)
            endTimeTextView.text = dateTimeFormat.format(selectedEndTime.time)
            // deleteButton.visibility = View.GONE
        }


        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        startTimeTextView.setOnClickListener {
            pickDateTime(selectedStartTime) { calendar ->
                startTimeTextView.text = dateTimeFormat.format(calendar.time)
                // Ensure end time is after start time
                if (selectedEndTime.before(selectedStartTime)) {
                    selectedEndTime.timeInMillis = selectedStartTime.timeInMillis
                    selectedEndTime.add(Calendar.HOUR_OF_DAY, 1)
                    endTimeTextView.text = dateTimeFormat.format(selectedEndTime.time)
                }
            }
        }

        endTimeTextView.setOnClickListener {
            pickDateTime(selectedEndTime) { calendar ->
                endTimeTextView.text = dateTimeFormat.format(calendar.time)
            }
        }

        saveButton.setOnClickListener {
            val title = titleEditText.text.toString().trim()
            val description = descriptionEditText.text.toString().trim()
            val category = categoryEditText.text.toString().trim()
            val tags = tagsEditText.text.toString().split(",").map { it.trim() }.filter { it.isNotEmpty() }
            val priority = prioritySpinner.selectedItemPosition

            if (title.isEmpty()) {
                titleEditText.error = "标题不能为空"
                return@setOnClickListener
            }

            if (selectedEndTime.before(selectedStartTime)) {
                Toast.makeText(context, "结束时间不能早于开始时间", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newOrUpdatedEvent = Event(
                id = event?.id ?: 0, // Use existing id for update, 0 for new (autoGenerate)
                title = title,
                description = description,
                startTime = selectedStartTime.timeInMillis,
                endTime = selectedEndTime.timeInMillis,
                priority = priority,
                category = category.firstOrNull()?.toString(),
                tags = tags.joinToString(", ")
            )

            if (event == null) {
                calendarViewModel.insertEvent(newOrUpdatedEvent)
                Toast.makeText(context, "日程已创建", Toast.LENGTH_SHORT).show()
            } else {
                calendarViewModel.updateEvent(newOrUpdatedEvent)
                Toast.makeText(context, "日程已更新", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showDeleteConfirmationDialog(event: Event) {
        AlertDialog.Builder(requireContext())
            .setTitle("删除日程")
            .setMessage("您确定要删除日程 “${event.title}” 吗？此操作无法撤销。")
            .setPositiveButton("删除") { _, _ ->
                calendarViewModel.deleteEvent(event)
                Toast.makeText(context, "日程已删除", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun pickDateTime(calendar: Calendar, onDateTimeSet: (Calendar) -> Unit) {
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
            TimePickerDialog(requireContext(), { _, hourOfDay, minute ->
                calendar.set(year, month, dayOfMonth, hourOfDay, minute)
                onDateTimeSet(calendar)
            }, currentHour, currentMinute, true).show()
        }, currentYear, currentMonth, currentDay).show()
    }


    private fun observeViewModel() {
        calendarViewModel.selectedDate.observe(viewLifecycleOwner) { date ->
            val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
            selectedDateTextView.text = sdf.format(date)
        }

        calendarViewModel.selectedDateEvents.observe(viewLifecycleOwner) { events ->
            eventAdapter.submitList(events)
        }
    }
}