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

import kotlinx.serialization.Serializable

/**
 * 最近使用项目数据类
 * 
 * 用于记录用户最近打开过的词库，方便快速访问
 * 在主界面会显示最近使用的词库列表
 * 
 * @property time 最后打开时间，格式如 "2024-01-01 12:00:00"
 * @property name 词库名称
 * @property path 词库文件路径
 * @property index 上次学习到的单词索引（记住学习进度）
 */
@Serializable
data class RecentItem(val time: String, val name: String, val path: String, val index: Int = 0) {
    /**
     * 重写 equals 方法
     * 
     * 判断两个最近项是否相同：只比较名称和路径，不比较时间和索引
     * 用途：防止同一个词库在最近列表中出现多次
     */
    override fun equals(other: Any?): Boolean {
        val otherItem = other as RecentItem
        return this.name == otherItem.name && this.path == otherItem.path
    }

    /**
     * 重写 hashCode 方法
     * 
     * 与 equals 保持一致，用名称和路径的哈希值计算
     * 用于 HashSet、HashMap 等集合去重
     */
    override fun hashCode(): Int {
        return name.hashCode() + path.hashCode()
    }
}