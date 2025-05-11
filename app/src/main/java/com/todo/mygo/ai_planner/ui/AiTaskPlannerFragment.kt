package com.todo.mygo.ai_planner.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.todo.mygo.databinding.FragmentAiTaskPlannerBinding

class AiTaskPlannerFragment : Fragment() {

    private var _binding: FragmentAiTaskPlannerBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AiTaskPlannerViewModel
    private lateinit var taskAdapter: PlannedTaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAiTaskPlannerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[AiTaskPlannerViewModel::class.java] // Updated to modern syntax
        setupRecyclerView()

        binding.buttonPlan.setOnClickListener {
            val goal = binding.editTextGoal.text.toString().trim()
            if (goal.isNotBlank()) {
                viewModel.planTasks(goal)
            } else {
                Toast.makeText(context, "请输入你的大目标", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.plannedTasks.observe(viewLifecycleOwner) { tasks ->
            taskAdapter.submitList(tasks)
            // Show save button only if there are tasks
            binding.buttonSaveTasks.isVisible = !tasks.isNullOrEmpty()
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.isVisible = isLoading
            binding.buttonPlan.isEnabled = !isLoading
            binding.editTextGoal.isEnabled = !isLoading
            // Disable save button while loading as well
            if (isLoading) binding.buttonSaveTasks.isEnabled = false
            else binding.buttonSaveTasks.isEnabled = !viewModel.plannedTasks.value.isNullOrEmpty()

        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                viewModel.clearError() // Clear error after showing
            }
        }

        binding.buttonSaveTasks.setOnClickListener {
            viewModel.savePlannedTasksToGantt()
        }

        viewModel.saveStatus.observe(viewLifecycleOwner) { statusMessage ->
            statusMessage?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                viewModel.clearSaveStatus() // Clear status after showing
            }
        }
    }

    private fun setupRecyclerView() {
        taskAdapter = PlannedTaskAdapter()
        binding.recyclerViewPlannedTasks.apply {
            adapter = taskAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}