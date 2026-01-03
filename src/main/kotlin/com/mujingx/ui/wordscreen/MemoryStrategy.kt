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

package com.mujingx.ui.wordscreen

/**
 * 记忆策略枚举
 * 
 * 定义了幕境应用中不同的单词学习模式
 * 每种模式有不同的功能限制和交互方式
 * 
 * 学习模式设计思想：
 * - 渐进式学习：从完整信息展示到纯听写测试
 * - 及时反馈：错误后立即复习，巩固记忆
 * - 灵活测试：支持单元测试和多章节测试
 */
enum class MemoryStrategy {
    /**
     * 正常记忆模式
     * 
     * 完整的学习体验，包含所有功能：
     * - 可以多次练习拼写单词
     * - 可以播放单词读音和视频片段
     * - 可以抄写字幕加深记忆
     * - 显示所有信息（音标、释义、例句等）
     * 
     * 适用场景：首次学习新单词
     */
    Normal,

    /**
     * 单元听写测试模式
     * 
     * 学完一个单元的词后的测试
     * - 隐藏单词，只显示读音和释义
     * - 需要根据提示拼写出单词
     * - 错误的单词会被记录，供后续复习
     * 
     * 适用场景：完成一个学习单元后的自测
     */
    Dictation,

    /**
     * 正常模式错词复习
     * 
     * 复习在正常模式的听写测试中拼写错误的单词
     * - 重点复习易错词
     * - 帮助巩固薄弱环节
     * 
     * 适用场景：针对性地复习错词
     */
    NormalReviewWrong,

    /**
     * 独立听写测试模式
     * 
     * 从侧边栏打开的综合测试
     * - 可以选择多个章节进行测试
     * - 不限于单个学习单元
     * - 适合阶段性检验学习成果
     * 
     * 适用场景：跨章节、跨单元的综合测试
     */
    DictationTest,

    /**
     * 独立测试错词复习
     * 
     * 复习独立听写测试中的错误单词
     * - 针对综合测试中的薄弱点
     * - 查漏补缺
     * 
     * 适用场景：复习综合测试中的错词
     */
    DictationTestReviewWrong
}