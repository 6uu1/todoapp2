package com.todo.mygo.calendar.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.todo.mygo.R
import com.todo.mygo.calendar.data.Event
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CalendarFragment : Fragment() {

    private lateinit var calendarViewModel: CalendarViewModel
    private lateinit var calendarView: CalendarView
    private lateinit var selectedDateTextView: TextView
    private lateinit var eventsRecyclerView: RecyclerView
    private lateinit var eventAdapter: EventAdapter // You'll need to create this Adapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_calendar, container, false) // You'll need to create this layout

        calendarViewModel = ViewModelProvider(this).get(CalendarViewModel::class.java)

        calendarView = root.findViewById(R.id.calendarView)
        selectedDateTextView = root.findViewById(R.id.selectedDateTextView) // Add this to your layout
        eventsRecyclerView = root.findViewById(R.id.eventsRecyclerView) // Add this to your layout

        setupCalendarView()
        setupRecyclerView()
        observeViewModel()

        // Set initial date
        calendarViewModel.setSelectedDate(Date(calendarView.date))

        return root
    }

    private fun setupCalendarView() {
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            calendarViewModel.setSelectedDate(calendar.time)
        }
    }

    private fun setupRecyclerView() {
        eventAdapter = EventAdapter { event ->
            // Handle event click - e.g., open event details
            // For now, let's say navigate to an edit screen or show a dialog
        }
        eventsRecyclerView.layoutManager = LinearLayoutManager(context)
        eventsRecyclerView.adapter = eventAdapter
    }

    private fun observeViewModel() {
        calendarViewModel.selectedDate.observe(viewLifecycleOwner) { date ->
            val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
            selectedDateTextView.text = sdf.format(date)
            // Trigger loading events for the newly selected date (ViewModel handles this internally)
        }

        calendarViewModel.selectedDateEvents.observe(viewLifecycleOwner) { events ->
            eventAdapter.submitList(events)
        }

        // Observe allEvents if you need to react to any change in the database
        // calendarViewModel.allEvents.observe(viewLifecycleOwner) { events ->
        //     // This might trigger a reload or refresh of the current view
        //     calendarViewModel.setSelectedDate(calendarViewModel.selectedDate.value ?: Date())
        // }
    }
}