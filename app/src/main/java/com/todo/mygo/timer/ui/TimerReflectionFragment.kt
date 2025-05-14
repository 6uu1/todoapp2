package com.todo.mygo.timer.ui

import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.todo.mygo.R
import com.todo.mygo.timer.data.PomodoroStatus
import com.todo.mygo.timer.service.TimerService

/**
 * 计时器与反思页面
 */
class TimerReflectionFragment : Fragment() {

    private lateinit var timerViewModel: TimerViewModel
    private lateinit var reflectionViewModel: ReflectionViewModel
    
    // 视图组件
    private lateinit var tabLayout: TabLayout
    private lateinit var timerText: TextView
    private lateinit var timerStatusText: TextView
    private lateinit var pomodoroStatusText: TextView
    private lateinit var btnStartPause: Button
    private lateinit var btnReset: Button
    private lateinit var btnSkip: Button
    private lateinit var reflectionContainer: ConstraintLayout
    private lateinit var reflectionRecyclerView: RecyclerView
    private lateinit var btnAddReflection: Button
    
    // 适配器
    private lateinit var reflectionAdapter: ReflectionAdapter
    
    // 服务
    private var timerService: TimerService? = null
    private var bound = false
    
    // 服务连接
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as TimerService.TimerBinder
            timerService = binder.getService()
            bound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            timerService = null
            bound = false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_timer_reflection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 初始化 ViewModel
        timerViewModel = ViewModelProvider(this).get(TimerViewModel::class.java)
        reflectionViewModel = ViewModelProvider(this).get(ReflectionViewModel::class.java)
        
        // 初始化视图组件
        initViews(view)
        
        // 设置监听器
        setupListeners()
        
        // 观察 ViewModel 数据变化
        observeViewModels()
        
        // 启动计时器服务
        startTimerService()
    }

    override fun onStart() {
        super.onStart()
        // 绑定服务
        Intent(requireContext(), TimerService::class.java).also { intent ->
            requireContext().bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        // 解绑服务
        if (bound) {
            requireContext().unbindService(connection)
            bound = false
        }
    }

    /**
     * 初始化视图组件
     */
    private fun initViews(view: View) {
        tabLayout = view.findViewById(R.id.tabLayout)
        timerText = view.findViewById(R.id.timerText)
        timerStatusText = view.findViewById(R.id.timerStatusText)
        pomodoroStatusText = view.findViewById(R.id.pomodoroStatusText)
        btnStartPause = view.findViewById(R.id.btnStartPause)
        btnReset = view.findViewById(R.id.btnReset)
        btnSkip = view.findViewById(R.id.btnSkip)
        reflectionContainer = view.findViewById(R.id.reflectionContainer)
        reflectionRecyclerView = view.findViewById(R.id.reflectionRecyclerView)
        btnAddReflection = view.findViewById(R.id.btnAddReflection)
        
        // 设置反思记录列表
        reflectionAdapter = ReflectionAdapter(
            onEditClick = { reflection ->
                showAddEditReflectionDialog(reflection)
            },
            onDeleteClick = { reflection ->
                showDeleteReflectionConfirmation(reflection)
            }
        )
        
        reflectionRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = reflectionAdapter
        }
    }

    /**
     * 设置监听器
     */
    private fun setupListeners() {
        // 标签页切换监听
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> timerViewModel.setTimerMode(TimerMode.POMODORO)
                    1 -> timerViewModel.setTimerMode(TimerMode.REGULAR)
                }
                updateTimerUI()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
        
        // 开始/暂停按钮
        btnStartPause.setOnClickListener {
            when (timerViewModel.timerState.value) {
                TimerState.IDLE -> timerViewModel.startTimer()
                TimerState.RUNNING -> timerViewModel.pauseTimer()
                TimerState.PAUSED -> timerViewModel.resumeTimer()
                else -> timerViewModel.startTimer()
            }
        }
        
        // 重置按钮
        btnReset.setOnClickListener {
            timerViewModel.stopTimer()
        }
        
        // 跳过按钮
        btnSkip.setOnClickListener {
            timerViewModel.skipCurrentPhase()
        }
        
        // 添加反思按钮
        btnAddReflection.setOnClickListener {
            showAddEditReflectionDialog(null)
        }
        
        // 反思容器拖动
        val reflectionHandle = view?.findViewById<View>(R.id.reflectionHandle)
        reflectionHandle?.setOnClickListener {
            toggleReflectionContainer()
        }
    }

    /**
     * 观察 ViewModel 数据变化
     */
    private fun observeViewModels() {
        // 观察计时器模式
        timerViewModel.timerMode.observe(viewLifecycleOwner) { mode ->
            tabLayout.getTabAt(if (mode == TimerMode.POMODORO) 0 else 1)?.select()
            updateTimerUI()
        }
        
        // 观察计时器状态
        timerViewModel.timerState.observe(viewLifecycleOwner) { state ->
            updateTimerControls(state)
        }
        
        // 观察剩余时间
        timerViewModel.timeRemaining.observe(viewLifecycleOwner) { timeRemaining ->
            if (timerViewModel.timerMode.value == TimerMode.POMODORO) {
                timerText.text = timerViewModel.formatTime(timeRemaining)
            }
        }
        
        // 观察已用时间
        timerViewModel.elapsedTime.observe(viewLifecycleOwner) { elapsedTime ->
            if (timerViewModel.timerMode.value == TimerMode.REGULAR) {
                timerText.text = timerViewModel.formatTime(elapsedTime)
            }
        }
        
        // 观察番茄钟状态
        timerViewModel.pomodoroStatus.observe(viewLifecycleOwner) { status ->
            updatePomodoroStatus(status)
        }
        
        // 观察已完成番茄数
        timerViewModel.completedPomodoros.observe(viewLifecycleOwner) { count ->
            pomodoroStatusText.text = "已完成: $count 个番茄"
        }
        
        // 观察今日反思
        reflectionViewModel.todayReflections.observe(viewLifecycleOwner) { reflections ->
            reflectionAdapter.submitList(reflections)
        }
        
        // 观察保存状态
        reflectionViewModel.saveStatus.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { message ->
                // 显示保存状态提示
                android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 启动计时器服务
     */
    private fun startTimerService() {
        Intent(requireContext(), TimerService::class.java).also { intent ->
            requireContext().startService(intent)
        }
    }

    /**
     * 更新计时器 UI
     */
    private fun updateTimerUI() {
        val isPomodoro = timerViewModel.timerMode.value == TimerMode.POMODORO
        
        // 更新番茄钟状态显示
        pomodoroStatusText.visibility = if (isPomodoro) View.VISIBLE else View.GONE
        
        // 更新跳过按钮显示
        btnSkip.visibility = if (isPomodoro) View.VISIBLE else View.GONE
        
        // 更新计时器文本
        if (isPomodoro) {
            timerText.text = timerViewModel.formatTime(timerViewModel.timeRemaining.value ?: 0)
            updatePomodoroStatus(timerViewModel.pomodoroStatus.value ?: PomodoroStatus.IDLE)
        } else {
            timerText.text = timerViewModel.formatTime(timerViewModel.elapsedTime.value ?: 0)
            timerStatusText.text = "正计时"
        }
    }

    /**
     * 更新计时器控制按钮
     */
    private fun updateTimerControls(state: TimerState) {
        when (state) {
            TimerState.IDLE -> {
                btnStartPause.text = "开始"
                btnReset.isEnabled = false
                btnSkip.isEnabled = false
            }
            TimerState.RUNNING -> {
                btnStartPause.text = "暂停"
                btnReset.isEnabled = true
                btnSkip.isEnabled = timerViewModel.timerMode.value == TimerMode.POMODORO
            }
            TimerState.PAUSED -> {
                btnStartPause.text = "继续"
                btnReset.isEnabled = true
                btnSkip.isEnabled = timerViewModel.timerMode.value == TimerMode.POMODORO
            }
            TimerState.FINISHED -> {
                btnStartPause.text = "开始"
                btnReset.isEnabled = false
                btnSkip.isEnabled = false
            }
        }
    }

    /**
     * 更新番茄钟状态
     */
    private fun updatePomodoroStatus(status: PomodoroStatus) {
        val statusText = when (status) {
            PomodoroStatus.IDLE -> "准备开始"
            PomodoroStatus.WORKING -> "工作中"
            PomodoroStatus.SHORT_BREAK -> "短休息"
            PomodoroStatus.LONG_BREAK -> "长休息"
            PomodoroStatus.PAUSED -> "已暂停"
        }
        timerStatusText.text = statusText
    }

    /**
     * 切换反思容器显示状态
     */
    private fun toggleReflectionContainer() {
        val params = reflectionContainer.layoutParams as ViewGroup.MarginLayoutParams
        if (reflectionContainer.height <= 100) {
            // 展开
            params.height = 300
        } else {
            // 收起
            params.height = 100
        }
        reflectionContainer.layoutParams = params
    }

    /**
     * 显示添加/编辑反思对话框
     */
    private fun showAddEditReflectionDialog(reflection: com.todo.mygo.timer.data.Reflection?) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_reflection, null)
        
        val dialogTitle = dialogView.findViewById<TextView>(R.id.dialogTitle)
        val editTextReflection = dialogView.findViewById<EditText>(R.id.editTextReflection)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSave)
        
        // 设置标题和内容
        dialogTitle.text = if (reflection == null) "添加反思" else "编辑反思"
        editTextReflection.setText(reflection?.content ?: "")
        
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        
        // 取消按钮
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        
        // 保存按钮
        btnSave.setOnClickListener {
            val content = editTextReflection.text.toString().trim()
            if (content.isNotBlank()) {
                if (reflection == null) {
                    // 创建新反思
                    reflectionViewModel.saveReflection(content)
                } else {
                    // 编辑现有反思
                    reflectionViewModel.editReflection(reflection.copy(content = content))
                    reflectionViewModel.saveReflection(content)
                }
                dialog.dismiss()
            } else {
                editTextReflection.error = "反思内容不能为空"
            }
        }
        
        dialog.show()
    }

    /**
     * 显示删除反思确认对话框
     */
    private fun showDeleteReflectionConfirmation(reflection: com.todo.mygo.timer.data.Reflection) {
        AlertDialog.Builder(requireContext())
            .setTitle("删除反思")
            .setMessage("确定要删除这条反思记录吗？")
            .setPositiveButton("删除") { _, _ ->
                reflectionViewModel.deleteReflection(reflection)
            }
            .setNegativeButton("取消", null)
            .show()
    }
}
