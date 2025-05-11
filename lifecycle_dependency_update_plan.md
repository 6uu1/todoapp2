# AndroidX Lifecycle 依赖更新计划

## 1. 目标

解决在 `app/src/main/java/com/todo/mygo/ui/home/HomeViewModel.kt` 文件中遇到的 `Unresolved reference 'Transformations'` 及相关的编译错误，并确保 AndroidX Lifecycle 依赖配置的正确性和一致性。

## 2. 背景分析

*   `HomeViewModel.kt` 使用 `import androidx.lifecycle.Transformations` 并调用 `Transformations.map()`。
*   项目当前依赖 `androidx.lifecycle:lifecycle-livedata-ktx:2.6.1`，此版本应提供 `Transformations` API。
*   项目同时依赖已弃用的 `androidx.lifecycle:lifecycle-extensions:2.2.0`。
*   推测编译错误是由于 `lifecycle-extensions` 与 `lifecycle-livedata-ktx` 在 `Transformations` 类的提供上产生冲突或解析混乱。

## 3. 核心行动：移除 `lifecycle-extensions`

将彻底移除 `androidx.lifecycle:lifecycle-extensions` 依赖。

### 3.1 修改 `gradle/libs.versions.toml`

1.  **删除版本变量定义** (原第16行):
    ```toml
    # lifecycleExtensions = "2.2.0" # Lifecycle extensions version (ViewModelProviders etc.)
    ```
2.  **删除库声明** (原第38行):
    ```toml
    # androidx-lifecycle-extensions = { group = "androidx.lifecycle", name = "lifecycle-extensions", version.ref = "lifecycleExtensions" }
    ```

### 3.2 修改 `app/build.gradle.kts`

1.  **删除依赖实现** (原第61行):
    ```kotlin
    // implementation(libs.androidx.lifecycle.extensions) // For ViewModelProviders and other lifecycle utilities
    ```

## 4. 保持核心 Lifecycle 组件版本

以下核心 AndroidX Lifecycle 组件将保持其当前版本 `2.6.1`：

*   `androidx.lifecycle:lifecycle-livedata-ktx:2.6.1`
    *   `gradle/libs.versions.toml` 中的 `lifecycleLivedataKtx = "2.6.1"`
    *   `app/build.gradle.kts` 中的 `implementation(libs.androidx.lifecycle.livedata.ktx)`
*   `androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1`
    *   `gradle/libs.versions.toml` 中的 `lifecycleViewmodelKtx = "2.6.1"`
    *   `app/build.gradle.kts` 中的 `implementation(libs.androidx.lifecycle.viewmodel.ktx)`
*   `androidx.lifecycle:lifecycle-common-java8:2.6.1`
    *   `gradle/libs.versions.toml` 中的 `lifecycleCommonJava8 = "2.6.1"`
    *   `app/build.gradle.kts` 中的 `implementation(libs.androidx.lifecycle.common.java8)`

## 5. 预期结果

*   移除 `lifecycle-extensions` 后，`import androidx.lifecycle.Transformations` 在 `HomeViewModel.kt` 中应该能够从 `androidx.lifecycle:lifecycle-livedata-ktx:2.6.1` 中被正确解析。
*   `Unresolved reference 'Transformations'` 编译错误以及相关的连锁错误（如类型推断问题）应得到解决。
*   项目将使用更现代、维护性更好的 Lifecycle 依赖配置。

## 6. 后续步骤

1.  切换到 "Code" 模式。
2.  应用上述对 `gradle/libs.versions.toml` 和 `app/build.gradle.kts` 文件的更改。
3.  同步 Gradle 项目。
4.  重新编译项目以验证错误是否已解决。
5.  如果错误解决，则任务完成。