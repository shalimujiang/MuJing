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

package com.mujingx.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.TooltipPlacement
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HighlightOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.mujingx.player.isMacOS

/**
 * 关闭按钮组件
 * 
 * 用于关闭已打开的词库、字幕或文本文件
 * 显示为一个带工具提示的 X 图标按钮
 * 
 * Compose UI 组件思想：
 * - @Composable 注解：标记这是一个可组合函数，用于构建UI
 * - 组件化：将UI拆分成小的、可复用的组件
 * - 声明式：描述UI应该是什么样子，而不是如何构建
 * 
 * @param toolTip 工具提示文本，鼠标悬停时显示
 * @param onClick 点击回调函数，点击按钮时执行
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class, ExperimentalComposeUiApi::class)
@Composable
fun RemoveButton(
    toolTip:String,
    onClick: () -> Unit
){
    // TooltipArea：工具提示区域，鼠标悬停时显示提示信息
    TooltipArea(
        tooltip = {
            // 工具提示的外观
            Surface(
                elevation = 4.dp,  // 阴影高度
                border = BorderStroke(1.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.12f)),
                shape = RectangleShape  // 矩形形状
            ) {
                Text(text = toolTip, modifier = Modifier.padding(10.dp))
            }
        },
        delayMillis = 50,  // 延迟50毫秒后显示工具提示
        tooltipPlacement = TooltipPlacement.ComponentRect(
            anchor = Alignment.BottomCenter,     // 锚点：组件底部中心
            alignment = Alignment.BottomCenter,  // 对齐：工具提示底部中心
            offset = DpOffset.Zero               // 偏移量为0
        )
    ) {
        // 获取当前主题的背景文字颜色
        val color = MaterialTheme.colors.onBackground
        
        // 图标颜色状态：记住颜色值，鼠标悬停时变红
        // remember：记住状态，组件重组时保持值不变
        var tint by remember(color){ mutableStateOf(color) }
        
        // 图标按钮
        IconButton(
            onClick = onClick,  // 点击事件回调
            modifier = Modifier
                .padding(top = if (isMacOS()) 44.dp else 0.dp)  // macOS需要额外的顶部间距
                // 鼠标进入事件：变红色
                .onPointerEvent(PointerEventType.Enter){
                    tint = Color.Red
                }
                // 鼠标离开事件：恢复原色
                .onPointerEvent(PointerEventType.Exit){
                    tint = color
                }
        ) {
            // 显示 X 图标
            Icon(
                Icons.Filled.HighlightOff,  // Material Design 的 X 图标
                contentDescription = "Localized description",  // 无障碍描述
                tint = tint  // 图标颜色
            )
        }
    }
}