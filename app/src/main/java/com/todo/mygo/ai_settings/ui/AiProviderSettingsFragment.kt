package com.todo.mygo.ai_settings.ui

import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.todo.mygo.R
import com.todo.mygo.ai_settings.data.AiProviderInfo
import com.todo.mygo.databinding.FragmentAiProviderSettingsBinding

class AiProviderSettingsFragment : Fragment() {

    private var _binding: FragmentAiProviderSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AiProviderSettingsViewModel
    private var isInitialSetupDone = false // 防止初始化时触发不必要的保存

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAiProviderSettingsBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(AiProviderSettingsViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSpinner()
        setupObservers()
        setupClickListeners()
        setupInputListeners()

        // 确保API Key输入框默认为密码模式
        binding.editTextApiKey.transformationMethod = PasswordTransformationMethod.getInstance()
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            viewModel.providerNames
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerAiProviders.adapter = adapter

        binding.spinnerAiProviders.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (isInitialSetupDone) { // 只有在初始设置完成后才响应用户选择
                    val selectedName = viewModel.providerNames[position]
                    viewModel.selectProvider(selectedName)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    private fun setupObservers() {
        viewModel.selectedProviderName.observe(viewLifecycleOwner) { name ->
            name?.let {
                val position = viewModel.providerNames.indexOf(it)
                if (position >= 0 && binding.spinnerAiProviders.selectedItemPosition != position) {
                    binding.spinnerAiProviders.setSelection(position)
                }
                // 在这里加载配置，确保spinner选择后，对应的配置被加载
                viewModel.loadProviderConfig(it)
            }
        }

        viewModel.currentProviderConfig.observe(viewLifecycleOwner) { providerInfo ->
            providerInfo?.let {
                binding.editTextApiUrl.setText(it.apiUrl)
                binding.editTextApiKey.setText(it.apiKey)
                // 标记初始设置完成，允许spinner的onItemSelectedListener开始工作
                // 放在这里确保配置加载完成后才认为初始设置完成
                if (!isInitialSetupDone) isInitialSetupDone = true
            }
        }

        viewModel.testConnectionResult.observe(viewLifecycleOwner) { result ->
            binding.textViewTestResult.text = "测试结果: ${result.second}"
            binding.textViewTestResult.setTextColor(
                if (result.first) resources.getColor(R.color.design_default_color_primary, null) // 替换为你的成功颜色
                else resources.getColor(R.color.design_default_color_error, null) // 替换为你的失败颜色
            )
            if (result.second != "正在测试连接...") { // 测试结束时才显示Toast
                 Toast.makeText(context, "测试结果: ${result.second}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupClickListeners() {
        binding.buttonSaveConfig.setOnClickListener {
            val currentName = binding.spinnerAiProviders.selectedItem as String
            val apiUrl = binding.editTextApiUrl.text.toString().trim()
            val apiKey = binding.editTextApiKey.text.toString().trim() // API Key也trim一下

            val providerInfo = AiProviderInfo(currentName, apiUrl, apiKey)
            viewModel.saveProviderConfig(providerInfo)
            Toast.makeText(context, "配置已保存: $currentName", Toast.LENGTH_SHORT).show()
        }

        binding.buttonTestConnection.setOnClickListener {
            val apiUrl = binding.editTextApiUrl.text.toString().trim()
            val apiKey = binding.editTextApiKey.text.toString().trim()
            viewModel.testConnection(apiUrl, apiKey)
        }
    }
    
    private fun setupInputListeners() {
        // 当用户修改URL或API Key时，清除之前的测试结果
        binding.editTextApiUrl.doAfterTextChanged { 
            binding.textViewTestResult.text = ""
        }
        binding.editTextApiKey.doAfterTextChanged {
            binding.textViewTestResult.text = ""
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        isInitialSetupDone = false // 重置状态
    }
}