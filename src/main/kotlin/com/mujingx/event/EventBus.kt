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

package com.mujingx.event


import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * 事件总线类
 * 
 * 用于在不同组件之间传递事件消息，实现解耦
 * 
 * 什么是事件总线（Event Bus）？
 * - 一种设计模式，用于组件间的消息传递
 * - 发送者不需要知道接收者是谁，接收者也不需要知道发送者是谁
 * - 通过订阅-发布机制实现松耦合
 * 
 * 为什么使用事件总线？
 * - 解耦：组件之间不直接依赖，便于维护和测试
 * - 灵活：可以动态添加或移除事件监听器
 * - 简化：避免复杂的回调链
 * 
 * Kotlin Flow 概念：
 * - Flow 是 Kotlin 协程的异步数据流
 * - MutableSharedFlow 可以被多个订阅者共享
 * - asSharedFlow() 提供只读视图，防止外部直接发送事件
 */
class EventBus {
    /**
     * 私有的可变事件流
     * 
     * MutableSharedFlow<Any>：可以发送任何类型的事件
     * extraBufferCapacity = 64：缓冲区大小，可以暂存64个未处理的事件
     */
    private val _events = MutableSharedFlow<Any>(extraBufferCapacity = 64)
    
    /**
     * 公开的只读事件流
     * 
     * 外部组件只能订阅（监听）事件，不能直接发送事件
     * 必须通过 post() 方法发送事件
     */
    val events = _events.asSharedFlow()
    
    /**
     * 发送事件
     * 
     * suspend 关键字：这是一个挂起函数，只能在协程中调用
     * emit：发射事件到事件流
     * 
     * @param event 要发送的事件对象，可以是任何类型
     */
    suspend fun post(event: Any) = _events.emit(event)
}

/**
 * 播放器键盘事件类型枚举
 * 
 * 定义了视频播放器支持的所有键盘操作
 * 用户按键时，会发送对应的事件到事件总线
 */
enum class PlayerEventType {
    PLAY,                   // 播放/暂停
    ESC,                    // 退出全屏
    FULL_SCREEN,            // 进入全屏
    CLOSE_PLAYER,           // 关闭播放器
    DIRECTION_LEFT,         // 左方向键（快退）
    DIRECTION_RIGHT,        // 右方向键（快进）
    DIRECTION_UP,           // 上方向键（音量增大）
    DIRECTION_DOWN,         // 下方向键（音量减小）
    PREVIOUS_CAPTION,       // 跳转到上一句字幕
    NEXT_CAPTION,           // 跳转到下一句字幕
    REPEAT_CAPTION,         // 重复播放当前字幕
    AUTO_PAUSE,             // 开启/关闭自动暂停功能
    TOGGLE_FIRST_CAPTION,   // 显示/隐藏第一语言字幕（如英文）
    TOGGLE_SECOND_CAPTION,  // 显示/隐藏第二语言字幕（如中文）
}

/**
 * 单词学习界面键盘事件类型枚举
 * 
 * 定义了单词学习界面支持的所有键盘操作
 * 方便用户通过快捷键快速操作，提高学习效率
 */
enum class WordScreenEventType {
    NEXT_WORD,              // 切换到下一个单词
    PREVIOUS_WORD,          // 切换到上一个单词
    OPEN_SIDEBAR,           // 打开/关闭侧边栏
    SHOW_WORD,              // 显示/隐藏单词（听写模式）
    SHOW_PRONUNCIATION,     // 显示/隐藏音标
    SHOW_LEMMA,             // 显示/隐藏词形变化
    SHOW_DEFINITION,        // 显示/隐藏英文释义
    SHOW_TRANSLATION,       // 显示/隐藏中文翻译
    SHOW_SENTENCES,         // 显示/隐藏例句
    SHOW_SUBTITLES,         // 显示/隐藏字幕
    PLAY_AUDIO,             // 播放单词读音
    OPEN_VOCABULARY,        // 打开词库
    DELETE_WORD,            // 删除当前单词
    ADD_TO_FAMILIAR,        // 添加到熟悉词库
    ADD_TO_DIFFICULT,       // 添加到困难词库
    COPY_WORD,              // 复制单词到剪贴板
    PLAY_FIRST_CAPTION,     // 播放第一个例句的视频片段
    PLAY_SECOND_CAPTION,    // 播放第二个例句的视频片段
    PLAY_THIRD_CAPTION,     // 播放第三个例句的视频片段
    FOCUS_ON_WORD,          // 聚焦到单词输入框
}