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

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

/**
 * 幕境服务端版本发布信息数据类
 * 
 * 用于检查和获取幕境应用的最新版本信息
 * 与服务端 API 的数据结构保持一致
 * 
 * 与 GitHubRelease 的区别：
 * - GitHubRelease：直接从 GitHub API 获取，包含所有详细信息
 * - MujingRelease：从幕境自己的服务端获取，只包含必要信息
 * - MujingRelease 更轻量，适合快速检查版本更新
 * 
 * @property tag_name 版本标签，如 "v1.0.0"（必需）
 * @property name 发布版本名称（可选）
 * @property body 发布说明，描述更新内容（可选）
 * @property draft 是否为草稿版本（默认 false）
 * @property prerelease 是否为预发布版本（默认 false）
 * @property created_at 创建时间（必需）
 * @property published_at 发布时间（可选）
 * @property html_url 发布页面网址（可选）
 */
@ExperimentalSerializationApi
@Serializable
data class MujingRelease(
    val tag_name: String,               // 版本号
    val name: String? = null,           // 版本名称
    val body: String? = null,           // 更新说明
    val draft: Boolean = false,         // 是否草稿
    val prerelease: Boolean = false,    // 是否预发布
    val created_at: String,             // 创建时间
    val published_at: String? = null,   // 发布时间
    val html_url: String? = null        // 网页链接
)