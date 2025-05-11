# App 页面重新设计和开发规划文档 (v1)

## 1. 引言

本项目旨在重新设计并实现一款移动应用的用户界面，核心围绕五个底部导航页面展开。目标是提升用户体验，整合现有功能，并引入新的 AI 辅助规划能力，使用户能够更高效地管理任务、规划日程和进行个人反思。本文档将详细阐述各个页面的设计方案、功能需求、交互逻辑以及初步的开发路线图建议。

## 2. 整体导航结构

应用将采用底部导航栏（Bottom Navigation Bar）设计，包含以下五个核心页面：

1.  **待办事项 (Todo)**：用户每日任务管理中心。
2.  **日历与甘特图 (Calendar & Gantt)**：日程与项目计划可视化工具。
3.  **AI 交互规划 (AI Planner)**：通过与 AI 对话进行智能规划。
4.  **计时与反思 (Timer & Reflection)**：专注工作与每日回顾。
5.  **设置 (Settings)**：应用配置中心。

## 3. 各页面详细设计

### 3.1 页面一：待办事项 (Todo)

*   **功能描述：**
    *   整合原有的 Todo 页面功能。
    *   显示用户当天的待办事项列表。
    *   允许用户手动添加、编辑、删除、标记完成待办事项。
    *   提供 AI 建议功能，帮助用户规划任务。
*   **UI 布局建议 (参考用户提供的第一张图片描述)：**
    *   **顶部区域：**
        *   左上角：固定标题“今天打算要干的事”。
        *   （可选，根据图片推断）日期显示：当前日期。
    *   **内容区域：**
        *   待办事项列表：
            *   每项包含：复选框（标记完成）、任务描述、星标（标记重要性，可选）。
            *   可考虑不同状态（如已完成、未完成）的视觉区分。
    *   **底部区域：**
        *   右下角：一个圆角浮动操作按钮 (FAB) "+"号，用于手动添加新的待办事项。
        *   左下方（或与FAB协调布局）：一个小的 AI 建议悬浮椭圆按钮，文字为“AI建议”或类似图标，点击后触发 AI 建议功能。
    *   **背景：**
        *   （根据图片推断）可设置用户自定义背景图或主题色。
*   **交互说明：**
    *   点击 "+" FAB：弹出添加待办事项的对话框或跳转到新页面。
    *   点击 AI 建议按钮：
        *   触发 AI 规划逻辑，例如：
            *   AI 分析用户近期待办、日历计划等，智能推荐今日可执行的任务。
            *   允许用户选择 AI 建议的时间范围（如“今日”、“本周”、“本月”）。
            *   AI 建议的任务可以直接添加到待办列表中。
    *   列表项操作：
        *   点击复选框：切换任务完成状态。
        *   长按或滑动列表项：出现编辑、删除等操作选项。

### 3.2 页面二：日历与甘特图 (Calendar & Gantt)

*   **功能描述：**
    *   提供日历视图，方便用户查看日期和关联的事件/计划。
    *   提供甘特图视图，用于展示项目或复杂计划的时间轴和依赖关系。
    *   允许用户在日历或甘特图上添加、编辑、删除计划。
    *   提供 AI 建议功能，辅助用户进行整体计划分配。
*   **UI 布局建议：**
    *   **主视图区域：**
        *   默认显示日历视图（例如月视图，参考用户提供的第二张图片）。日历应清晰展示公历日期，并可集成显示节假日标记、农历日期、节气等信息。
        *   不同计划的持续时间用不同颜色的细线条在日期下方或背景上划过表示，线条颜色对比度不宜过高，避免遮挡日期数字。
        *   甘特图视图：
            *   横向时间轴，纵向任务列表。
            *   任务条清晰展示起止时间、进度（可选）。
            *   可支持任务依赖关系连接线。
    *   **底部/浮动按钮区域：**
        *   右下角：一个三小横线的菜单按钮 (FAB 或普通按钮)。点击后展开菜单，包含：
            1.  “切换到甘特图” / “切换到日历” (根据当前视图动态显示)。
            2.  “添加计划”。
        *   左下方（或与菜单按钮协调布局）：一个小的 AI 建议悬浮椭圆按钮（设计风格同第一个页面的AI建议按钮），用于与 AI 交互规划整体计划分配（例如考研复习计划，AI 可根据总目标和时间范围，智能拆分阶段性任务并分配到日历/甘特图）。
    *   **日历视图切换：**
        *   日历本身的视图切换（如月、周、日）应通过日历组件内部交互或顶部的选择器实现，不采用图片中底部“月”、“三日”、“台历”的独立按钮组。
*   **交互说明：**
    *   点击菜单按钮：展开操作选项。
        *   点击“切换视图”：平滑过渡到日历或甘特图视图。
        *   点击“添加计划”：弹出添加计划的表单，用户可输入计划名称、起止时间、描述、关联颜色等。
    *   点击 AI 建议按钮：
        *   启动 AI 规划流程，引导用户输入规划目标（如“考研复习”）、总时间范围等。
        *   AI 生成初步计划方案，用户可调整并确认。
        *   确认后的计划自动添加到日历和甘特图中。
    *   日历交互：
        *   点击日期：可高亮显示，并展示当天相关的计划列表。
        *   长按日期或计划标记：可进行编辑或删除操作。
    *   甘特图交互：
        *   拖动任务条调整起止时间。
        *   点击任务条查看详情或编辑。

### 3.3 页面三：AI 交互规划 (AI Planner)

*   **功能描述：**
    *   提供一个专门的 AI 对话界面。
    *   通过启发式对话，帮助用户梳理需求、设定目标，并共同制定详细计划。
    *   AI 规划的结果能够直接同步到日历与甘特图页面。
*   **UI 布局建议：**
    *   **对话界面：**
        *   类似主流聊天应用的界面布局。
        *   顶部显示 AI Assistant 名称或头像。
        *   中间为对话气泡区域，区分用户输入和 AI 回复。
        *   底部为文本输入框和发送按钮。
    *   **辅助功能（可选）：**
        *   快捷指令按钮：例如“制定周计划”、“规划旅行”、“设定学习目标”等。
        *   上下文信息展示：AI 在规划过程中，可能会引用或展示来自待办事项、日历的信息。
*   **交互说明：**
    *   用户在输入框输入自然语言描述自己的规划需求。
    *   AI 理解用户意图，通过提问、引导、提供选项等方式进行多轮对话。
    *   AI 能够理解模糊指令，并逐步细化。例如，用户说“我想为下个月的考试做准备”，AI 可以追问“是什么考试？”、“有多少天准备时间？”、“你希望每天学习多久？”等。
    *   规划过程中，AI 可以提供模板或建议的计划结构。
    *   用户可以随时修改或确认 AI 的建议。
*   **与日历/甘特图的数据同步机制：**
    *   当 AI 与用户共同完成一个计划制定后，AI 会向用户确认是否将此计划添加到日程中。
    *   用户确认后，AI 将结构化的计划数据（任务名称、起止时间、依赖关系等）发送给日历/甘特图模块。
    *   日历/甘特图模块接收数据并创建相应的事件或任务。
    *   （可选）提供一个“查看已规划日程”的快捷入口，直接跳转到日历/甘特图页面并高亮显示新添加的计划。

### 3.4 页面四：番茄钟/正计时与记录反思 (Timer & Reflection)

*   **功能描述：**
    *   提供番茄钟计时功能，帮助用户专注工作。
    *   提供正计时功能，用于记录某项活动的时长。
    *   提供“今天的事项记录，反思”功能，方便用户快速记录想法和进行每日回顾。
*   **UI 布局建议 (参考用户提供的第三张图片描述 - 计时器部分)：**
    *   **计时器区域 (页面上半部分或主要区域)：**
        *   顶部标签页或分段控件切换：“番茄计时”、“正计时”。
        *   中间显眼位置：一个大的圆形计时器，清晰显示剩余时间 (番茄钟) 或已用时间 (正计时)，例如 "25:00"。
        *   计时器下方：
            *   “开始”/“暂停”/“继续”/“重置”/“跳过”等控制按钮。
            *   （番茄钟）显示当前是工作时段还是休息时段，以及已完成的番茄数量。
    *   **记录反思区域 (页面下半部分)：**
        *   一个默认收起的“小白条”（下拉式面板），可从底部向上拉出展开。
        *   小白条收起时，可显示一个提示性图标或文字，如“今日反思”或“+ 添加记录”。
        *   向上拉出后，展开为一个列表区域：
            *   顶部可有日期选择或默认为“今天”。
            *   列表项为一条条快速记录的入口/内容预览。
            *   提供快速添加新记录的按钮或输入框。
*   **交互说明：**
    *   **计时器：**
        *   点击标签切换番茄钟和正计时模式。
        *   点击“开始”按钮启动计时。
        *   番茄钟到点后，应有声音和视觉提示，并自动切换到休息时段或提示开始下个番茄。
    *   **记录反思：**
        *   从底部向上滑动小白条，展开记录反思面板。
        *   向下滑动面板或点击收起按钮，收起面板。
        *   点击添加记录按钮/输入框：进入详细记录编辑界面或直接在列表添加简短记录。
        *   点击已有的记录项：查看或编辑详细内容。

### 3.5 页面五：设置 (Settings)

*   **功能描述：**
    *   集中管理应用的所有配置项。
    *   包括通用设置、账户设置、通知设置以及 AI 相关设置。
*   **UI 布局建议：**
    *   标准的列表形式设置界面。
    *   可分组展示不同类别的设置项。
*   **设置项列表 (初步建议，需结合现有应用和 [`commented_menu_items_restore_guide.md`](commented_menu_items_restore_guide.md:0) 补充)：**
    *   **通用设置：**
        *   主题（浅色/深色/跟随系统）
        *   语言选择
        *   数据同步（如果涉及云同步）
        *   默认视图（例如，打开应用时默认进入哪个页面）
    *   **通知设置：**
        *   任务提醒
        *   番茄钟提醒
        *   AI 建议通知
    *   **AI 相关设置 (参考 [`commented_menu_items_restore_guide.md`](commented_menu_items_restore_guide.md:0) 中的 `navigation_ai_settings`，具体内容需进一步定义)：**
        *   AI 建议开启/关闭 (针对不同模块，如待办、日历)
        *   AI 模型选择 (如果支持多种)
        *   AI 数据使用偏好 (例如，是否允许 AI 访问日历数据以提供更精准建议)
        *   清除 AI 对话历史
        *   (根据 [`AiProviderSettingsFragment.kt`](app/src/main/java/com/todo/mygo/ai_settings/ui/AiProviderSettingsFragment.kt:0) 和 [`AiSettingsRepository.kt`](app/src/main/java/com/todo/mygo/ai_settings/data/AiSettingsRepository.kt:0) 推断，可能包含 AI 服务提供商相关的配置)
    *   **关于与帮助：**
        *   版本号
        *   用户手册/FAQ
        *   反馈入口
        *   隐私政策
        *   服务条款

## 4. 通用 UI/UX 元素

*   **AI 建议悬浮椭圆按钮：**
    *   设计风格应统一，小巧不突兀，但易于识别。
    *   可考虑使用统一的 AI 图标配合简短文字（如“AI”或“助手”）。
    *   点击反馈应清晰，例如按钮状态变化、加载指示等。
*   **浮动操作按钮 (FAB)：**
    *   在“待办事项”页用于添加任务，在“日历与甘特图”页用于展开菜单。
    *   样式和位置应符合 Material Design 指南或平台通用规范。
*   **加载与空状态：**
    *   为数据加载过程提供合适的加载指示器。
    *   为空列表（如无待办事项、无计划）设计友好的空状态提示和引导操作。
*   **颜色与字体：**
    *   定义一套和谐的色彩方案，确保对比度和可读性。
    *   选择清晰易读的字体。

## 5. 开发路线图/任务分解建议

### 5.1 现有代码和资源评估 (基于提供的文件列表进行高层次评估)

*   **可复用/相关的现有模块：**
    *   **Todo 功能：**
        *   [`app/src/main/java/com/todo/mygo/todo/data/TodoDao.kt`](app/src/main/java/com/todo/mygo/todo/data/TodoDao.kt:0), [`app/src/main/java/com/todo/mygo/todo/data/TodoItem.kt`](app/src/main/java/com/todo/mygo/todo/data/TodoItem.kt:0), [`app/src/main/java/com/todo/mygo/todo/ui/TodoFragment.kt`](app/src/main/java/com/todo/mygo/todo/ui/TodoFragment.kt:0), [`app/src/main/java/com/todo/mygo/todo/ui/TodoViewModel.kt`](app/src/main/java/com/todo/mygo/todo/ui/TodoViewModel.kt:0), [`app/src/main/res/layout/fragment_todo.xml`](app/src/main/res/layout/fragment_todo.xml:0), [`app/src/main/res/layout/item_todo.xml`](app/src/main/res/layout/item_todo.xml:0)。这些是现有 Todo 功能的核心，新设计将在此基础上进行 UI 调整和功能增强（如 AI 建议）。
    *   **日历功能：**
        *   [`app/src/main/java/com/todo/mygo/calendar/data/CalendarDatabase.kt`](app/src/main/java/com/todo/mygo/calendar/data/CalendarDatabase.kt:0), [`app/src/main/java/com/todo/mygo/calendar/data/CalendarRepository.kt`](app/src/main/java/com/todo/mygo/calendar/data/CalendarRepository.kt:0), [`app/src/main/java/com/todo/mygo/calendar/data/Event.kt`](app/src/main/java/com/todo/mygo/calendar/data/Event.kt:0), [`app/src/main/java/com/todo/mygo/calendar/data/EventDao.kt`](app/src/main/java/com/todo/mygo/calendar/data/EventDao.kt:0), [`app/src/main/java/com/todo/mygo/calendar/ui/CalendarFragment.kt`](app/src/main/java/com/todo/mygo/calendar/ui/CalendarFragment.kt:0), [`app/src/main/java/com/todo/mygo/calendar/ui/CalendarViewModel.kt`](app/src/main/java/com/todo/mygo/calendar/ui/CalendarViewModel.kt:0), [`app/src/main/res/layout/fragment_calendar.xml`](app/src/main/res/layout/fragment_calendar.xml:0)。现有日历功能的基础，需要在此基础上增加甘特图切换、计划标记、AI 建议等。
    *   **Gantt 功能 (待恢复)：**
        *   [`app/src/main/java/com/todo/mygo/gantt/GanttFragment.kt`](app/src/main/java/com/todo/mygo/gantt/GanttFragment.kt:0), [`app/src/main/java/com/todo/mygo/gantt/GanttViewModel.kt`](app/src/main/java/com/todo/mygo/gantt/GanttViewModel.kt:0), [`app/src/main/java/com/todo/mygo/gantt/data/PlannedTask.kt`](app/src/main/java/com/todo/mygo/gantt/data/PlannedTask.kt:0), [`app/src/main/java/com/todo/mygo/gantt/data/PlannedTaskDao.kt`](app/src/main/java/com/todo/mygo/gantt/data/PlannedTaskDao.kt:0), [`app/src/main/res/layout/fragment_gantt.xml`](app/src/main/res/layout/fragment_gantt.xml:0)。这些是甘特图的基础，需要恢复并整合到新的日历与甘特图页面。
    *   **AI Planner 功能 (待恢复)：**
        *   [`app/src/main/java/com/todo/mygo/ai_planner/ui/AiTaskPlannerFragment.kt`](app/src/main/java/com/todo/mygo/ai_planner/ui/AiTaskPlannerFragment.kt:0), [`app/src/main/java/com/todo/mygo/ai_planner/ui/AiTaskPlannerViewModel.kt`](app/src/main/java/com/todo/mygo/ai_planner/ui/AiTaskPlannerViewModel.kt:0), [`app/src/main/res/layout/fragment_ai_task_planner.xml`](app/src/main/res/layout/fragment_ai_task_planner.xml:0)。这是 AI 交互规划页面的基础，需要恢复并根据新需求调整 UI 和交互。
    *   **AI 设置功能 (待恢复)：**
        *   [`app/src/main/java/com/todo/mygo/ai_settings/data/AiProviderInfo.kt`](app/src/main/java/com/todo/mygo/ai_settings/data/AiProviderInfo.kt:0), [`app/src/main/java/com/todo/mygo/ai_settings/data/AiSettingsRepository.kt`](app/src/main/java/com/todo/mygo/ai_settings/data/AiSettingsRepository.kt:0), [`app/src/main/java/com/todo/mygo/ai_settings/ui/AiProviderSettingsFragment.kt`](app/src/main/java/com/todo/mygo/ai_settings/ui/AiProviderSettingsFragment.kt:0), [`app/src/main/java/com/todo/mygo/ai_settings/ui/AiProviderSettingsViewModel.kt`](app/src/main/java/com/todo/mygo/ai_settings/ui/AiProviderSettingsViewModel.kt:0), [`app/src/main/res/layout/fragment_ai_provider_settings.xml`](app/src/main/res/layout/fragment_ai_provider_settings.xml:0)。这是 AI 设置页面的基础，需要恢复并整合到新的设置页面。
    *   **导航结构：**
        *   [`app/src/main/res/menu/bottom_nav_menu.xml`](app/src/main/res/menu/bottom_nav_menu.xml:0) 和 [`app/src/main/res/navigation/mobile_navigation.xml`](app/src/main/res/navigation/mobile_navigation.xml:0) 定义了应用的导航，需要根据新的五个导航页进行重大调整。
        *   [`commented_menu_items_restore_guide.md`](commented_menu_items_restore_guide.md:0) 提供了恢复部分导航项的指导。
    *   **通用 UI 组件和资源：**
        *   [`app/src/main/res/values/colors.xml`](app/src/main/res/values/colors.xml:0), [`app/src/main/res/values/dimens.xml`](app/src/main/res/values/dimens.xml:0), [`app/src/main/res/values/strings.xml`](app/src/main/res/values/strings.xml:0), [`app/src/main/res/values/themes.xml`](app/src/main/res/values/themes.xml:0) 等可以作为新设计的基础，但可能需要根据新的 UI 风格进行调整。
        *   现有的 drawable 资源（如 `ic_settings_black_24dp.xml`）可以部分复用。
*   **可能需要较大修改或新建的模块：**
    *   **番茄钟/正计时与记录反思页面：** 这是一个全新的页面，需要从头开始设计和实现其 UI、业务逻辑和数据存储（如果记录反思需要持久化）。
    *   **UI 整体风格和布局：** 既然是“重新设计”，意味着现有页面的布局 ([`fragment_home.xml`](app/src/main/java/com/todo/mygo/ui/home/HomeFragment.kt:0) 等) 可能需要大幅修改或替换，以符合新的设计语言和用户体验目标。
    *   **AI 建议的集成：** 在待办事项和日历/甘特图页面中集成 AI 建议按钮及其背后的逻辑是新增功能。
    *   **数据同步机制：** AI 交互规划结果到日历/甘特图的数据同步需要仔细设计和实现。

### 5.2 模块划分 (初步建议)

1.  **Core / Common Module:**
    *   基础数据模型 (Task, Event, Plan, ReflectionLog)
    *   数据库帮助类 (SQLite/Room)
    *   通用 UI 组件 (自定义按钮, 对话框, 加载动画等)
    *   网络请求封装 (如果 AI 服务需要联网)
    *   权限管理
2.  **Navigation Module:**
    *   底部导航栏管理
    *   各 Fragment 之间的导航逻辑
3.  **Todo Module:**
    *   Todo 列表 UI ([`TodoFragment.kt`](app/src/main/java/com/todo/mygo/todo/ui/TodoFragment.kt:0) 重构)
    *   Todo 数据管理 ([`TodoViewModel.kt`](app/src/main/java/com/todo/mygo/todo/ui/TodoViewModel.kt:0), [`TodoDao.kt`](app/src/main/java/com/todo/mygo/todo/data/TodoDao.kt:0) 调整)
    *   AI 建议集成逻辑
4.  **Calendar & Gantt Module:**
    *   日历视图 UI ([`CalendarFragment.kt`](app/src/main/java/com/todo/mygo/calendar/ui/CalendarFragment.kt:0) 重构)
    *   甘特图视图 UI ([`GanttFragment.kt`](app/src/main/java/com/todo/mygo/gantt/GanttFragment.kt:0) 恢复与重构)
    *   视图切换逻辑
    *   计划数据管理 ([`CalendarViewModel.kt`](app/src/main/java/com/todo/mygo/calendar/ui/CalendarViewModel.kt:0), [`EventDao.kt`](app/src/main/java/com/todo/mygo/calendar/data/EventDao.kt:0), [`PlannedTaskDao.kt`](app/src/main/java/com/todo/mygo/gantt/data/PlannedTaskDao.kt:0) 调整)
    *   AI 建议集成逻辑
5.  **AI Planner Module:**
    *   AI 对话界面 UI ([`AiTaskPlannerFragment.kt`](app/src/main/java/com/todo/mygo/ai_planner/ui/AiTaskPlannerFragment.kt:0) 恢复与重构)
    *   对话逻辑与 AI 服务交互 ([`AiTaskPlannerViewModel.kt`](app/src/main/java/com/todo/mygo/ai_planner/ui/AiTaskPlannerViewModel.kt:0) 调整)
    *   规划结果同步到 Calendar & Gantt Module 的接口
6.  **Timer & Reflection Module (新建):**
    *   番茄钟/正计时 UI 与逻辑
    *   记录反思 UI 与逻辑 (下拉面板，列表)
    *   数据存储 (如果需要)
7.  **Settings Module:**
    *   设置项 UI (基于现有 [`AiProviderSettingsFragment.kt`](app/src/main/java/com/todo/mygo/ai_settings/ui/AiProviderSettingsFragment.kt:0) 扩展)
    *   各项设置的逻辑实现

### 5.3 开发优先级和步骤建议

1.  **阶段一：基础架构与导航重建**
    *   **任务1.1:** 确定整体 UI 风格和设计规范。
    *   **任务1.2:** 重建底部导航栏，包含五个新的导航目标。参考 [`commented_menu_items_restore_guide.md`](commented_menu_items_restore_guide.md:0) 恢复基础菜单项，并调整为新的五个页面结构。修改 [`bottom_nav_menu.xml`](app/src/main/res/menu/bottom_nav_menu.xml:0) 和 [`mobile_navigation.xml`](app/src/main/res/navigation/mobile_navigation.xml:0)。
    *   **任务1.3:** 为每个导航页面创建基础的 Fragment 骨架。
    *   **任务1.4:** 搭建核心数据模型和数据库结构 (Room)。

2.  **阶段二：核心功能页面实现 (迭代进行)**
    *   **任务2.1: 待办事项 (Todo) 页面重构**
        *   实现新的 UI 布局。
        *   整合现有 Todo 功能。
        *   实现手动添加/编辑/删除/完成功能。
    *   **任务2.2: 日历与甘特图页面**
        *   实现日历视图基础功能 (事件展示)。
        *   实现甘特图视图基础功能 (任务展示，从 [`GanttFragment.kt`](app/src/main/java/com/todo/mygo/gantt/GanttFragment.kt:0) 恢复和调整)。
        *   实现日历/甘特图切换功能。
        *   实现手动添加计划功能。
        *   实现计划在日历上的颜色标记。
    *   **任务2.3: 设置页面**
        *   实现基础设置项 UI。
        *   恢复并整合 AI 设置 (从 [`AiProviderSettingsFragment.kt`](app/src/main/java/com/todo/mygo/ai_settings/ui/AiProviderSettingsFragment.kt:0) 恢复和调整)。

3.  **阶段三：AI 功能集成**
    *   **任务3.1: AI 交互规划页面**
        *   恢复并重构 AI 对话界面 ([`AiTaskPlannerFragment.kt`](app/src/main/java/com/todo/mygo/ai_planner/ui/AiTaskPlannerFragment.kt:0))。
        *   实现基本的对话交互逻辑。
        *   实现规划结果到日历/甘特图的数据同步。
    *   **任务3.2: Todo 页面的 AI 建议**
        *   实现 AI 建议按钮和触发逻辑。
        *   集成 AI 算法或服务，根据用户数据生成任务建议。
    *   **任务3.3: 日历与甘特图页面的 AI 建议**
        *   实现 AI 建议按钮和触发逻辑。
        *   集成 AI 算法或服务，辅助整体计划分配。

4.  **阶段四：番茄钟/正计时与记录反思页面**
    *   **任务4.1:** 实现番茄钟计时器 UI 和核心逻辑。
    *   **任务4.2:** 实现正计时器 UI 和核心逻辑。
    *   **任务4.3:** 实现记录反思的下拉面板 UI 和交互。
    *   **任务4.4:** 实现记录内容的添加、查看、编辑（如果需要持久化，则包括数据存储）。

5.  **阶段五：优化、测试与收尾**
    *   **任务5.1:** 整体 UI/UX 细节打磨和一致性检查。
    *   **任务5.2:** 性能优化。
    *   **任务5.3:** 全面测试 (单元测试、集成测试、UI 测试)。
    *   **任务5.4:** Bug 修复。
    *   **任务5.5:** 准备发布。

### 5.4 技术选型考量 (通用建议，供后续 `code` agent 参考)

*   **编程语言：** Kotlin (鉴于项目已使用 Kotlin，继续使用以保持一致性，并利用其现代特性)。
*   **UI 框架：**
    *   **选项1 (维持现状):** Android XML Layouts + ViewBinding/DataBinding。如果团队熟悉且项目已有大量 XML 布局，可以继续使用并优化。
    *   **选项2 (推荐逐步引入):** Jetpack Compose。对于新页面或重构页面，可以考虑使用 Jetpack Compose，它能提供更声明式、更高效的 UI 开发体验。可以与现有 XML 布局混合使用。
*   **架构模式：** MVVM (Model-View-ViewModel)，结合 Android Jetpack (ViewModel, LiveData/Flow, Room, Navigation Component)。项目似乎已部分采用（如 [`TodoViewModel.kt`](app/src/main/java/com/todo/mygo/todo/ui/TodoViewModel.kt:0)），应保持并强化。
*   **异步处理：** Kotlin Coroutines + Flow。
*   **数据库：** Room Persistence Library (项目似乎已使用，如 [`TodoDao.kt`](app/src/main/java/com/todo/mygo/todo/data/TodoDao.kt:0), [`EventDao.kt`](app/src/main/java/com/todo/mygo/calendar/data/EventDao.kt:0))。
*   **依赖注入：** Hilt 或 Koin。Hilt 是 Jetpack 推荐的方案。
*   **AI 集成：**
    *   **本地模型：** TensorFlow Lite (如果需要离线 AI 功能且模型可部署在端侧)。
    *   **云端 AI 服务：** Google AI Platform, OpenAI API, 或其他第三方 AI 服务。需要考虑 API 调用、数据传输和认证。
    *   **SDKs：** 许多 AI 服务提供官方 Android SDK，可以简化集成。
*   **日历视图组件：**
    *   可考虑使用成熟的第三方开源日历库（如 Material Calendar View, Horizon Calendar 等）以加速开发并获得更丰富的特性。
    *   或者基于现有 [`CalendarFragment.kt`](app/src/main/java/com/todo/mygo/calendar/ui/CalendarFragment.kt:0) 进行深度定制和扩展。
*   **甘特图组件：**
    *   甘特图在移动端实现较为复杂，可以寻找合适的第三方库，或者基于 Canvas 自定义绘制（挑战较大）。
*   **图像加载：** Glide 或 Coil。

---

本文档提供了 App 重新设计和开发的初步规划。在实际开发过程中，可能需要根据具体情况进行调整和细化。