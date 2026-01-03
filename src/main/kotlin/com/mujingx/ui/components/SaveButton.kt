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

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * 保存按钮组件预览
 * 
 * @Preview 注解：用于在 Android Studio 中预览组件效果
 * 开发时可以不运行程序就能看到组件的外观
 */
@Preview
@Composable
fun SaveButtonPreview() {
    PreviewLayout {
        SaveButton(
            saveClick = {},   // 空实现，仅用于预览
            otherClick = {}   // 空实现，仅用于预览
        )
    }
}


/**
 * 保存按钮组件
 * 
 * 带下拉菜单的保存按钮，支持两种保存方式：
 * 1. 直接保存（点击"保存"文字）
 * 2. 保存其他格式（点击下拉箭头，选择"保存其他格式"）
 * 
 * UI 组成：
 * - 左侧："保存"文字按钮
 * - 中间：竖直分隔线
 * - 右侧：下拉箭头，点击显示菜单
 * 
 * @param enabled 是否启用按钮，false 时按钮变灰且不可点击
 * @param saveClick 点击"保存"时的回调函数
 * @param otherClick 点击"保存其他格式"时的回调函数
 */
@Composable
fun SaveButton(
    enabled: Boolean = true,        // 默认启用
    saveClick: () -> Unit,          // 保存回调
    otherClick: () -> Unit          // 保存其他格式回调
) {
    // Surface：Material Design 的表面组件，提供背景和圆角
    Surface(
        shape = MaterialTheme.shapes.small,  // 小圆角
    ){
        // 边框样式
        val border = BorderStroke(1.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.12f))
        
        // Row：水平布局，子元素从左到右排列
        Row(
            verticalAlignment = Alignment.CenterVertically,  // 垂直居中对齐
            modifier = Modifier
                .height(36.dp)  // 固定高度
                .border(
                    border = BorderStroke(1.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.12f)),
                )
        ) {
            // 下拉菜单展开状态
            // remember：记住状态，重组时保持
            // mutableStateOf：创建可变状态
            var expanded by remember { mutableStateOf(false) }
            
            // 按钮颜色：根据启用状态选择
            val buttonColor = if(enabled) 
                MaterialTheme.colors.primary  // 启用：主题色
            else 
                MaterialTheme.colors.onSurface.copy(alpha = 0.38f)  // 禁用：灰色
            
            // Box：基础容器，可以叠加子元素
            Box(Modifier.width(IntrinsicSize.Max)) {  // IntrinsicSize.Max：自适应内容宽度
                // "保存"文字按钮
                Text(
                    text = "保存",
                    color = buttonColor,
                    modifier = Modifier
                        .clickable(
                            enabled = enabled,
                            onClick = {  saveClick() }  // 点击时执行保存回调
                        )
                        .padding(start = 16.dp, end = 4.dp, top = 5.dp, bottom = 5.dp)
                )
                
                // 下拉菜单
                DropdownMenu(
                    expanded = expanded,  // 是否展开
                    onDismissRequest = { expanded = false },  // 点击外部时关闭
                    modifier = Modifier
                        .width(130.dp)
                        .height(40.dp)
                        .background(MaterialTheme.colors.background)
                        .border(border = border)
                        .clickable(
                            onClick = {
                                expanded = false  // 关闭菜单
                                otherClick()      // 执行保存其他格式回调
                            }
                        )
                ) {
                    Text(
                        text = "保存其他格式",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(130.dp).height(40.dp)
                    )
                }
            }

            // 竖直分隔线
            Divider(Modifier.fillMaxHeight().width(1.dp))
            
            // 下拉箭头图标
            Icon(
                Icons.Default.KeyboardArrowDown,  // Material Design 的下箭头图标
                contentDescription = null,
                tint = buttonColor,
                modifier = Modifier
                    .clickable(
                        enabled = enabled,
                        onClick = { expanded = true }  // 点击时展开菜单
                    )
                    .padding(start = 4.dp, end = 8.dp, top = 5.dp, bottom = 5.dp)

            )
        }
    }
}

