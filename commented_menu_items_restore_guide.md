# 恢复底部导航菜单项指南

## 引言

本文档旨在帮助您恢复在 `app/src/main/res/menu/bottom_nav_menu.xml` 文件中先前为解决 `BottomNavigationView` 相关问题而注释掉的底部导航菜单项。

## 被注释的菜单项列表

以下是需要恢复的菜单项：

1.  **Gantt 视图**
    *   ID: `navigation_gantt`
    *   Title: `@string/title_gantt`
2.  **AI 任务规划器**
    *   ID: `navigation_ai_planner`
    *   Title: `@string/title_ai_planner`
3.  **AI 设置**
    *   ID: `navigation_ai_settings`
    *   Title: `@string/title_ai_settings`

## 恢复步骤

1.  **打开文件**：
    在您的 Android Studio 项目中，导航并打开以下文件：
    `app/src/main/res/menu/bottom_nav_menu.xml`

2.  **找到并取消注释代码块**：
    在文件中找到以下被注释掉的菜单项代码块。要恢复它们，请移除包裹每个 `<item>` 标签的 `<!--` 和 `-->` 注释标记。

    ---

    ### 1. Gantt 视图

    **注释掉的状态：**
    ```xml
    <!--
        <item
            android:id="@+id/navigation_gantt"
            android:icon="@drawable/ic_dashboard_black_24dp"  <!-- Consider a unique icon -->
            android:title="@string/title_gantt" />
    -->
    ```

    **恢复后的状态：**
    ```xml
        <item
            android:id="@+id/navigation_gantt"
            android:icon="@drawable/ic_dashboard_black_24dp"  <!-- Consider a unique icon -->
            android:title="@string/title_gantt" />
    ```
    *(注意：请保留或替换 `<!-- Consider a unique icon -->` 为合适的图标资源。)*

    ---

    ### 2. AI 任务规划器

    **注释掉的状态：**
    ```xml
    <!--
        <item
            android:id="@+id/navigation_ai_planner"
            android:icon="@drawable/ic_action_plan_black_24dp" <!-- Placeholder: replace with actual icon -->
            android:title="@string/title_ai_planner" />
    -->
    ```

    **恢复后的状态：**
    ```xml
        <item
            android:id="@+id/navigation_ai_planner"
            android:icon="@drawable/ic_action_plan_black_24dp" <!-- Placeholder: replace with actual icon -->
            android:title="@string/title_ai_planner" />
    ```
    *(注意：请将 `<!-- Placeholder: replace with actual icon -->` 替换为 AI 任务规划器的实际图标资源。)*

    ---

    ### 3. AI 设置

    **注释掉的状态：**
    ```xml
    <!--
        <item
            android:id="@+id/navigation_ai_settings"
            android:icon="@drawable/ic_settings_black_24dp"
            android:title="@string/title_ai_settings" />
    -->
    ```

    **恢复后的状态：**
    ```xml
        <item
            android:id="@+id/navigation_ai_settings"
            android:icon="@drawable/ic_settings_black_24dp"
            android:title="@string/title_ai_settings" />
    ```

完成这些步骤后，保存 [`bottom_nav_menu.xml`](app/src/main/res/menu/bottom_nav_menu.xml:0) 文件。重新构建并运行您的应用程序，这些菜单项应该会重新出现在底部导航栏中。