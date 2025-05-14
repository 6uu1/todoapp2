 package com.todo.mygo.todo.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.todo.mygo.R
import com.todo.mygo.databinding.FragmentTodoImprovedBinding // Import ViewBinding
import com.todo.mygo.todo.data.TodoItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class TodoFragment : Fragment() {

    private val todoViewModel: TodoViewModel by viewModels()
    private lateinit var todoAdapter: TodoAdapter
    // private lateinit var recyclerView: RecyclerView // Replaced by ViewBinding
    // private lateinit var fabAddTodo: FloatingActionButton // Replaced by ViewBinding

    private var _binding: FragmentTodoImprovedBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTodoImprovedBinding.inflate(inflater, container, false)

        // 设置当前日期
        val today = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("M月d日 EEEE", Locale.CHINESE)
        binding.tvCurrentDate.text = dateFormat.format(today.time)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()

        todoViewModel.allTodoItems.observe(viewLifecycleOwner) { todos ->
            todos?.let { todoAdapter.submitList(it) }
        }

        todoViewModel.navigateToEditTodo.observe(viewLifecycleOwner) { todoItem ->
            todoItem?.let {
                showAddEditTodoDialog(it)
                todoViewModel.onEditTodoNavigated()
            }
        }

        binding.fabAddTodo.setOnClickListener {
            showAddEditTodoDialog(null)
        }

        binding.btnAiSuggestion.setOnClickListener {
            Toast.makeText(context, "AI Suggestion button clicked (placeholder)", Toast.LENGTH_SHORT).show()
        }

        setupItemTouchHelper()
    }

    private fun setupToolbar() {
        // 在新的布局中不需要设置ActionBar
    }

    private fun setupRecyclerView() {
        todoAdapter = TodoAdapter(
            onItemClicked = { todoItem ->
                todoViewModel.onTodoItemClicked(todoItem)
            },
            onToggleCompleted = { todoItem, isCompleted ->
                todoViewModel.toggleCompleted(todoItem, isCompleted)
            },
            onToggleStarred = { todoItem, isStarred ->
                // 这里我们临时使用优先级来表示星标状态
                val newPriority = if (isStarred) 1 else 2
                val updatedItem = todoItem.copy(priority = newPriority)
                todoViewModel.update(updatedItem)
                Toast.makeText(
                    context,
                    if (isStarred) "已标记为重要" else "已取消重要标记",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
        binding.rvTodoItems.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = todoAdapter
        }
    }

    private fun showAddEditTodoDialog(todoItem: TodoItem?) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_edit_todo, null)

        val etTitle = dialogView.findViewById<TextInputEditText>(R.id.et_todo_title)
        val etDescription = dialogView.findViewById<TextInputEditText>(R.id.et_todo_description)
        val spinnerPriority = dialogView.findViewById<Spinner>(R.id.spinner_todo_priority)
        val btnDueDate = dialogView.findViewById<Button>(R.id.btn_todo_due_date)
        var selectedDueDate: Long? = todoItem?.dueDate

        // Setup Priority Spinner
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.priority_levels, // This array needs to be created in strings.xml
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerPriority.adapter = adapter
        }

        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        fun updateDueDateButtonText() {
            btnDueDate.text = if (selectedDueDate != null) {
                "Due: ${sdf.format(Date(selectedDueDate!!))}"
            } else {
                "Set Due Date"
            }
        }

        if (todoItem != null) {
            etTitle.setText(todoItem.title)
            etDescription.setText(todoItem.description)
            spinnerPriority.setSelection(todoItem.priority - 1) // Assuming 1-High, 2-Medium, 3-Low
            updateDueDateButtonText()
        } else {
            spinnerPriority.setSelection(1) // Default to Medium (index 1 for priority 2)
            updateDueDateButtonText()
        }


        btnDueDate.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Due Date")
                .setSelection(selectedDueDate ?: MaterialDatePicker.todayInUtcMilliseconds())
                .build()

            datePicker.addOnPositiveButtonClickListener {
                selectedDueDate = it
                updateDueDateButtonText()
            }
            datePicker.show(childFragmentManager, "DATE_PICKER")
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (todoItem == null) "Add New Todo" else "Edit Todo")
            .setView(dialogView)
            .setNegativeButton("Cancel", null)
            .setPositiveButton(if (todoItem == null) "Add" else "Save") { _, _ ->
                val title = etTitle.text.toString().trim()
                val description = etDescription.text.toString().trim()
                // Priority is 1-based from spinner position (0-based) + 1
                val priority = spinnerPriority.selectedItemPosition + 1

                if (title.isEmpty()) {
                    Toast.makeText(context, "Title cannot be empty", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (todoItem == null) { // Add new
                    val newItem = TodoItem(
                        id = UUID.randomUUID().toString(),
                        title = title,
                        description = description.ifEmpty { null },
                        priority = priority,
                        dueDate = selectedDueDate,
                        creationDate = System.currentTimeMillis()
                    )
                    todoViewModel.insert(newItem)
                } else { // Update existing
                    val updatedItem = todoItem.copy(
                        title = title,
                        description = description.ifEmpty { null },
                        priority = priority,
                        dueDate = selectedDueDate
                    )
                    todoViewModel.update(updatedItem)
                }
            }
            .show()
    }

    private fun setupItemTouchHelper() {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false // We don't want to support drag and drop for now
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val todoToDelete = todoAdapter.currentList[position]

                AlertDialog.Builder(requireContext())
                    .setTitle("Delete Todo")
                    .setMessage("Are you sure you want to delete \"${todoToDelete.title}\"?")
                    .setPositiveButton("Delete") { _, _ ->
                        todoViewModel.delete(todoToDelete)
                        Toast.makeText(context, "Todo deleted", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        todoAdapter.notifyItemChanged(position) // Revert swipe
                        dialog.dismiss()
                    }
                    .setCancelable(false)
                    .show()
            }
        }).attachToRecyclerView(binding.rvTodoItems)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Important to avoid memory leaks
    }
}