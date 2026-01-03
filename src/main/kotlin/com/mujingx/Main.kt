package com.mujingx
/*
 * Copyright (c) 2023-2025 tang shimin
 *
 * This file is part of MuJing.
 *
 * MuJing is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MuJing is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MuJing. If not, see <https://www.gnu.org/licenses/>.
 */

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.window.application
import com.formdev.flatlaf.FlatDarkLaf
import com.formdev.flatlaf.FlatLightLaf
import io.github.vinceglb.filekit.FileKit
import kotlinx.serialization.ExperimentalSerializationApi
import com.mujingx.theme.isSystemDarkMode
import com.mujingx.ui.App

/**
 * 程序主入口
 * 
 * 这是幕境应用的启动点，所有程序从这里开始执行
 * 
 * Kotlin 主函数：
 * - fun main() 是 Kotlin 程序的入口函数，类似于 Java 的 public static void main(String[] args)
 * - 程序启动时，操作系统会自动调用这个函数
 * 
 * @OptIn 注解：
 * - 表示使用了实验性 API（Experimental API）
 * - 这些 API 可能在未来版本中发生变化，所以需要明确标注
 * - ExperimentalSerializationApi: 序列化库的实验性功能
 * - ExperimentalFoundationApi: Compose Foundation 的实验性功能
 * - ExperimentalAnimationApi: Compose 动画的实验性功能
 * 
 * application { } 函数：
 * - Compose Desktop 的应用程序入口
 * - 创建桌面应用的窗口和管理应用生命周期
 */
@OptIn(ExperimentalSerializationApi::class)
@ExperimentalFoundationApi
@ExperimentalAnimationApi
fun main() = application {
    init()  // 初始化应用（设置主题、文件选择器等）
    App()   // 启动主界面
}

/**
 * 初始化应用
 * 
 * 在显示主界面之前进行必要的初始化工作
 * 包括：
 * 1. 初始化文件选择器
 * 2. 根据系统主题设置应用外观（深色/浅色）
 */
fun init(){
    // FileKit.init 初始化文件选择器库
    // appId: 应用标识符，用于保存用户的文件选择偏好
    FileKit.init(appId = "幕境")
    
    // 根据系统主题设置应用的外观
    // FlatLaf 是一个现代化的 Swing 外观库
    if(isSystemDarkMode()) {
        // 系统是深色模式，使用深色主题
        FlatDarkLaf.setup()
    }else {
        // 系统是浅色模式，使用浅色主题
        FlatLightLaf.setup()
    }
}