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

package com.mujingx.data

/**
 * 词库类型枚举
 * 
 * 这个枚举类定义了幕境应用支持的三种词库来源类型
 * 
 * 什么是枚举（enum）？
 * - 枚举是一种特殊的类，用于定义一组固定的常量值
 * - 比如一周的七天、一年的十二个月、或者这里的词库类型
 * - 使用枚举可以让代码更清晰，避免使用容易出错的字符串或数字
 * 
 * @property DOCUMENT 文档词库 - 从 PDF、TXT 等文档生成的词库
 * @property SUBTITLES 字幕词库 - 从单独的字幕文件（.srt）生成的词库
 * @property MKV MKV视频词库 - 从带内嵌字幕的 MKV 视频文件生成的词库
 */
enum class VocabularyType {
    DOCUMENT,    // 文档类型：从 PDF、TXT 等文档中提取单词
    SUBTITLES,   // 字幕类型：从单独的字幕文件中提取单词
    MKV          // MKV视频类型：从 MKV 视频文件的内嵌字幕中提取单词
}