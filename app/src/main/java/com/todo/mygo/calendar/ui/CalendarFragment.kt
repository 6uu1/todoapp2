package com.todo.mygo.calendar.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CalendarView
import android.widget.FrameLayout
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.todo.mygo.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CalendarFragment : Fragment() {

   private lateinit var calendarViewModel: CalendarViewModel
   private lateinit var calendarView: CalendarView
   private lateinit var ganttViewContainer: FrameLayout
   private lateinit var fabMenu: FloatingActionButton
   private lateinit var btnAiSuggestion: Button

   private var isCalendarViewVisible = true

   // private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
   // private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
   // private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

   override fun onCreateView(
       inflater: LayoutInflater, container: ViewGroup?,
       savedInstanceState: Bundle?
   ): View? {
       val root = inflater.inflate(R.layout.fragment_calendar, container, false)

       calendarViewModel = ViewModelProvider(this).get(CalendarViewModel::class.java)

       calendarView = root.findViewById(R.id.calendarView)
       ganttViewContainer = root.findViewById(R.id.ganttViewContainer)
       fabMenu = root.findViewById(R.id.fabMenu)
       btnAiSuggestion = root.findViewById(R.id.btnAiSuggestion)

       setupCalendarView()
       setupFabMenu()
       setupAiSuggestionButton()
       observeViewModel()

       // Set initial date for CalendarView
       val initialDate = Calendar.getInstance()
       calendarViewModel.setSelectedDate(initialDate.time) // Assuming ViewModel handles this for CalendarView if needed

       updateViewVisibility() // Initial setup for view visibility

       return root
   }

   private fun setupCalendarView() {
       calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
           val calendar = Calendar.getInstance()
           calendar.set(year, month, dayOfMonth, 0, 0, 0)
           calendar.set(Calendar.MILLISECOND, 0)
           calendarViewModel.setSelectedDate(calendar.time)
           // Potentially show a toast or log for now, as event list is removed
           Toast.makeText(context, "Selected date: ${dateFormat.format(calendar.time)}", Toast.LENGTH_SHORT).show()
       }
   }

   private fun setupFabMenu() {
       fabMenu.setOnClickListener { view ->
           val popupMenu = PopupMenu(requireContext(), view)
           popupMenu.menuInflater.inflate(R.menu.calendar_gantt_menu, popupMenu.menu) // Create this menu resource

           // Dynamically set the text for switching view
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
                       // Later, this will open a dialog or new fragment to add a plan
                       true
                   }
                   else -> false
               }
           }
           popupMenu.show()
       }
   }

   private fun setupAiSuggestionButton() {
       btnAiSuggestion.setOnClickListener {
           Toast.makeText(context, "AI Suggestion clicked (Placeholder)", Toast.LENGTH_SHORT).show()
           // Later, this will trigger AI suggestion logic
       }
   }

   private fun updateViewVisibility() {
       if (isCalendarViewVisible) {
           calendarView.visibility = View.VISIBLE
           ganttViewContainer.visibility = View.GONE
       } else {
           calendarView.visibility = View.GONE
           ganttViewContainer.visibility = View.VISIBLE
       }
   }

   private fun observeViewModel() {
       calendarViewModel.selectedDate.observe(viewLifecycleOwner) { date ->
           // The selectedDateTextView was removed, so no UI update here for now.
           // This observer can be used if other parts of the fragment need to react to date changes.
           // For example, if the CalendarView itself needs to be programmatically set to this date.
           // calendarView.date = date.time // If needed and supported directly
       }
       // Removed observation for selectedDateEvents as eventsRecyclerView is removed.
   }

   // Companion object to hold date format, if needed by other classes or for consistency
   companion object {
       val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
   }
}