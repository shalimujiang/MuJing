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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.*


/**
 * 从 Wiktionary 原始数据中提取单词发音音频 URL
 * 
 * Wiktionary（维基词典）是维基百科的姊妹项目，提供多语言词典
 * 这个函数从 Wiktionary 的原始数据文件中提取英语单词的发音链接
 * 
 * 数据来源：https://kaikki.org/dictionary/rawdata.html
 * 文件名：raw-wiktextract-data.json
 * 
 * 工作流程：
 * 1. 逐行读取 JSON 文件（每行是一个词条）
 * 2. 过滤出英语词条
 * 3. 检查该单词是否在本地词典中存在
 * 4. 提取发音音频的 URL（MP3 或 OGG 格式）
 * 5. 保存到 AudioUrls.json 文件
 * 
 * @param rawdata Wiktionary 原始数据文件
 */
private fun extractedAudioUrls(rawdata:File) {
    // 存储发音数据：单词 -> 发音列表
    val audios = mutableMapOf<String, MutableSet<Pronunciation>>()
    
    // 收集所有发音标签（如 UK、US、AU 等）
    val tags = mutableSetOf<String>()
    
    var count = 1  // 计数器，记录处理了多少行
    
    // JSON 解析配置：忽略未知字段
    val format = Json { ignoreUnknownKeys = true }
    
    // useLines：逐行读取文件，内存友好
    rawdata.useLines { lines ->
        lines.forEach { line ->
            try {
                // 将 JSON 行解析为 WikitionaryItem 对象
                val item = format.decodeFromString<WikitionaryItem>(line)
                
                if(item.lang == "English"){  // 只处理英语词条
                    val word = item.word.lowercase(Locale.getDefault())  // 转小写
                    print("Row ${count++}")
                    println("    $word")
                    
                    // 查询本地词典，检查单词是否存在
                    val result = Dictionary.query(word)
                    if (result != null) {
                        // 遍历该单词的所有发音
                        item.sounds.forEach { sound ->
                            // 收集发音标签
                            sound.tags.forEach { tag ->
                                tags.add(tag)
                            }
                            
                            // 优先使用 MP3 格式
                            if (sound.mp3_url.isNotEmpty()) {
                                val pronunciation = Pronunciation(sound.tags.first(), sound.mp3_url)
                                val pronunciations = audios.get(word)
                                if (pronunciations == null) {
                                    audios.put(word, mutableSetOf(pronunciation))
                                } else {
                                    pronunciations.add(pronunciation)
                                }

                            }else if(sound.ogg_url.isNotEmpty()){  // 其次使用 OGG 格式
                                val pronunciation = Pronunciation(sound.tags.first(), sound.ogg_url)
                                val pronunciations = audios.get(word)
                                if (pronunciations == null) {
                                    audios.put(word, mutableSetOf(pronunciation))
                                } else {
                                    pronunciations.add(pronunciation)
                                }
                            }


                        }
                    }
                }else{
                    println("lang:${item.lang}")  // 记录非英语词条
                }

            } catch (_: Exception) {
                // 忽略解析错误的行
            }
        }
    }

    // 配置 JSON 输出格式
    val json = Json {
        prettyPrint = true      // 美化输出
        encodeDefaults = true   // 包含默认值
    }

    // 序列化并保存
    val jsonString = json.encodeToString(audios)
    val audioUrls = File("resources/common/AudioUrls.json")
    audioUrls.writeText(jsonString)
}

/**
 * Wiktionary 词条数据类
 * 
 * 映射 Wiktionary 原始数据的 JSON 结构
 * 只提取需要的字段（单词、语言、发音）
 * 
 * @property word 单词文本
 * @property lang 语言，如 "English"
 * @property sounds 发音列表
 */
@Serializable
data class WikitionaryItem(
    val word:String,
    val lang:String,
    val sounds:List<AudioItem>
)

/**
 * Wiktionary 发音项数据类
 * 
 * 表示一个单词的一种发音
 * 可能包含多种音频格式（MP3、OGG）
 * 
 * @property audio 音频文件名
 * @property text 发音的文本描述
 * @property tags 标签列表，如 ["UK", "female"] 表示英式英语女声
 * @property ogg_url OGG 格式音频的 URL
 * @property mp3_url MP3 格式音频的 URL
 */
@Serializable
data class AudioItem(
    val audio:String,
    val text:String,
    val tags:List<String>,
    val ogg_url:String,
    val mp3_url:String,
)

/**
 * 单词发音数据类
 * 
 * 简化的发音信息，只包含地区标签和 URL
 * 
 * @property tag 地区/口音标签，如 "UK"（英式）、"US"（美式）、"AU"（澳式）
 * @property url 音频文件的网址，可以直接播放或下载
 */
@Serializable
data class Pronunciation(
    val tag:String,
    val url:String
)
