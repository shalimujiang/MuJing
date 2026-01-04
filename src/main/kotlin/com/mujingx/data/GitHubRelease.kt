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
 * GitHub 发布版本数据类
 * 
 * 用于检查和下载幕境应用的新版本
 * 对应 GitHub API 返回的发布信息
 * 
 * GitHub API 文档：https://docs.github.com/cn/rest/releases/releases#get-the-latest-release
 * 
 * @property url API 地址
 * @property html_url 网页版发布页面地址
 * @property assets_url 资源列表 API 地址
 * @property upload_url 上传地址（开发者用）
 * @property tarball_url 源代码压缩包地址（.tar.gz 格式）
 * @property zipball_url 源代码压缩包地址（.zip 格式）
 * @property id 发布版本 ID
 * @property node_id 节点 ID（GitHub 内部使用）
 * @property tag_name 版本标签，如 "v1.0.0"
 * @property target_commitish 目标分支或提交
 * @property name 发布版本名称
 * @property body 发布说明（更新内容）
 * @property draft 是否为草稿
 * @property prerelease 是否为预发布版本
 * @property created_at 创建时间
 * @property published_at 发布时间
 * @property author 发布者信息
 * @property assets 附件列表（安装包文件）
 */
@ExperimentalSerializationApi
@Serializable
data class GitHubRelease(
    val url: String,
    val html_url: String,
    val assets_url: String,
    val upload_url: String,
    val tarball_url: String?,
    val zipball_url: String?,
    val id: Int,
    val node_id: String,
    val tag_name: String,
    val target_commitish: String,
    val name: String?,
    val body: String?,
    val draft: Boolean,
    val prerelease: Boolean,
    val created_at: String,
    val published_at: String?,
    val author: Author,
    val assets: List<Assert>,
)

/**
 * 发布资源数据类
 * 
 * 表示发布版本中的一个附件文件（如安装包）
 * 
 * @property url API 地址
 * @property id 资源 ID
 * @property node_id 节点 ID
 * @property name 文件名，如 "MuJing-Windows-1.0.0.exe"
 * @property label 标签
 * @property uploader 上传者信息
 * @property content_type 文件类型，如 "application/octet-stream"
 * @property state 状态
 * @property size 文件大小（字节）
 * @property download_count 下载次数
 * @property created_at 创建时间
 * @property updated_at 更新时间
 * @property browser_download_url 下载链接
 */
@ExperimentalSerializationApi
@Serializable
data class Assert(
    val url: String,
    val id: Int,
    val node_id: String,
    val name: String,
    val label: String?,
    val uploader: Author?,
    val content_type: String,
    val state: String,
    val size: Int,
    val download_count: Int,
    val created_at: String,
    val updated_at: String,
    val browser_download_url: String
)

/**
 * 作者/用户数据类
 * 
 * 表示 GitHub 用户信息
 * 可以是发布者、上传者等
 * 
 * @property name 用户名称
 * @property email 用户邮箱
 * @property login GitHub 用户名
 * @property id 用户 ID
 * @property node_id 节点 ID
 * @property avatar_url 头像地址
 * @property gravatar_id Gravatar ID
 * @property url API 地址
 * @property html_url 用户主页地址
 * @property followers_url 关注者列表 API
 * @property following_url 正在关注列表 API
 * @property gists_url Gists 列表 API
 * @property starred_url 收藏仓库列表 API
 * @property subscriptions_url 订阅列表 API
 * @property organizations_url 组织列表 API
 * @property repos_url 仓库列表 API
 * @property events_url 事件列表 API
 * @property received_events_url 接收事件列表 API
 * @property type 用户类型，如 "User"
 * @property site_admin 是否为网站管理员
 */
@ExperimentalSerializationApi
@Serializable
data class Author(
    val name:String? = null,
    val email:String? = null,
    val login: String,
    val id: Int,
    val node_id: String,
    val avatar_url: String,
    val gravatar_id: String?,
    val url: String,
    val html_url: String,
    val followers_url: String,
    val following_url: String,
    val gists_url: String,
    val starred_url: String,
    val subscriptions_url: String,
    val organizations_url: String,
    val repos_url: String,
    val events_url: String,
    val received_events_url: String,
    val type: String,
    val site_admin: Boolean,
)