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

import androidx.compose.runtime.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.mujingx.state.getResourcesFile
import com.mujingx.state.getSettingsDirectory
import java.io.File
import javax.swing.JOptionPane

/**
 * 词库数据类
 * 
 * 这个类用于存储一个完整词库的所有信息
 * 词库是幕境应用的核心数据结构，包含了从视频、文档或字幕中提取的单词列表
 * 
 * 什么是 data class（数据类）？
 * - Kotlin 的特殊类，专门用于存储数据
 * - 自动生成 equals()、hashCode()、toString() 等方法
 * - 可以使用 copy() 方法创建副本
 * 
 * 什么是 @Serializable 注解？
 * - 标记这个类可以被序列化（转换为 JSON 格式）和反序列化（从 JSON 格式还原）
 * - 用于保存词库到文件或从文件读取词库
 * 
 * @property name 词库名称，如"老友记第一季"
 * @property type 词库类型：文档、字幕或MKV视频，详见 VocabularyType
 * @property language 词库语言，如 "eng"（英语）
 * @property size 词库中单词的数量
 * @property relateVideoPath 关联的视频文件路径（如果是从视频生成的词库）
 * @property subtitlesTrackId 字幕轨道ID（MKV视频可能有多条字幕轨道）
 * @property wordList 单词列表，MutableList 表示可以修改（添加、删除单词）
 */
@Serializable
data class Vocabulary(
    var name: String = "",                                  // 词库名称
    val type: VocabularyType = VocabularyType.DOCUMENT,     // 词库类型
    val language: String,                                   // 语言代码
    var size: Int,                                          // 单词数量
    var relateVideoPath: String = "",                       // 关联视频路径
    val subtitlesTrackId: Int = 0,                          // 字幕轨道ID
    var wordList: MutableList<Word> = mutableListOf(),      // 单词列表
)

/**
 * 可观察的词库类
 * 
 * 这个类是 Vocabulary 的可观察版本，用于 Compose UI
 * 当词库的任何属性发生变化时，UI 会自动更新
 * 
 * Compose UI 编程思想：
 * - 声明式UI：描述UI应该是什么样子，而不是如何改变它
 * - 状态驱动：UI随着状态的变化自动重新渲染
 * - mutableStateOf：创建可观察的状态，当值改变时通知UI更新
 * 
 * 什么是 by 关键字？
 * - Kotlin 的属性委托语法
 * - `var name by mutableStateOf(...)` 相当于把 name 的读写操作委托给 mutableStateOf
 * - 每次读取或修改 name 时，都会通过 mutableStateOf 来处理，从而触发UI更新
 * 
 * @param vocabulary 普通的 Vocabulary 对象，将被转换为可观察版本
 */
class MutableVocabulary(vocabulary: Vocabulary) {
    // 使用 by mutableStateOf 创建可观察属性，属性改变时UI会自动更新
    var name by mutableStateOf(vocabulary.name)                     // 词库名称
    var type by mutableStateOf(vocabulary.type)                     // 词库类型
    var language by mutableStateOf(vocabulary.language)             // 语言代码
    var size by mutableStateOf(vocabulary.size)                     // 单词数量
    var relateVideoPath by mutableStateOf(vocabulary.relateVideoPath)   // 关联视频路径
    var subtitlesTrackId by mutableStateOf(vocabulary.subtitlesTrackId) // 字幕轨道ID
    // toMutableStateList() 创建可观察的列表，列表内容改变时UI会自动更新
    var wordList = vocabulary.wordList.toMutableStateList()         // 可观察的单词列表

    /**
     * 将可观察词库转换回普通词库对象
     * 
     * 用途：保存词库到文件时需要转换为可序列化的普通对象
     * get() 表示这是一个计算属性（每次访问时重新计算），不占用存储空间
     */
    val serializeVocabulary
        get() = Vocabulary(name, type, language, size, relateVideoPath, subtitlesTrackId, wordList)

}


/**
 * 单词数据类
 * 
 * 存储一个单词的完整信息，包括发音、释义、例句等
 * 
 * links 存储字幕链接：链接字幕词库功能，将外部字幕关联到当前单词
 * 格式：(subtitlePath)[videoPath][subtitleTrackId][index]
 * 
 * captions 字幕列表：单词在词库原始内容中的例句（如电影台词）
 * 
 * @property value 单词本身，如 "hello"
 * @property usphone 美式音标，如 "həˈloʊ"
 * @property ukphone 英式音标，如 "həˈləʊ"
 * @property definition 英文释义，通常来自英英词典
 * @property translation 中文翻译
 * @property pos 词性（Part of Speech），如 "n." (名词)、"v." (动词)、"adj." (形容词)
 * @property collins 柯林斯词典星级（1-5星），星级越高表示越常用
 * @property oxford 是否属于牛津3000核心词汇（true/false）
 * @property tag 标签，可以自定义分类
 * @property bnc BNC词频（英国国家语料库），数值越小表示越常用
 * @property frq COCA词频（当代美国英语语料库），数值越小表示越常用
 * @property exchange 词形变化，如 "p:went/d:gone/i:going/3:goes" 表示各种时态变化
 * @property externalCaptions 外部字幕列表，通过"链接字幕"功能添加的例句
 * @property captions 内部字幕列表，词库生成时自动提取的例句
 */
@Serializable
data class Word(
    var value: String,                                              // 单词
    var usphone: String = "",                                       // 美式音标
    var ukphone: String = "",                                       // 英式音标
    var definition: String = "",                                    // 英文释义
    var translation: String = "",                                   // 中文翻译
    var pos: String = "",                                           // 词性
    var collins: Int = 0,                                           // 柯林斯星级
    var oxford: Boolean = false,                                    // 是否牛津3000词
    var tag: String = "",                                           // 标签
    var bnc: Int? = 0,                                              // BNC词频
    var frq: Int? = 0,                                              // COCA词频
    var exchange: String = "",                                      // 词形变化
    var externalCaptions: MutableList<ExternalCaption> = mutableListOf(),  // 外部例句
    var captions: MutableList<Caption> = mutableListOf()            // 内部例句
) {
    /**
     * 重写 equals 方法：单词比较时忽略大小写
     * 
     * 用途：判断两个 Word 对象是否表示同一个单词
     * 例如："Hello" 和 "hello" 被认为是同一个单词
     */
    override fun equals(other: Any?): Boolean {
        val otherWord = other as Word
        return this.value.lowercase() == otherWord.value.lowercase()
    }

    /**
     * 重写 hashCode 方法：与 equals 保持一致
     * 
     * Kotlin/Java 规则：如果重写了 equals，必须同时重写 hashCode
     * 确保相同的单词有相同的哈希值，用于 HashSet、HashMap 等集合
     */
    override fun hashCode(): Int {
        return value.lowercase().hashCode()
    }
}
/**
 * 深拷贝单词对象
 * 
 * 创建一个完全独立的 Word 对象副本，包括所有的例句
 * 
 * 什么是扩展函数？
 * - fun Word.deepCopy() 是 Kotlin 的扩展函数语法
 * - 可以为已存在的类添加新方法，而不需要修改类的源代码
 * - 调用方式：word.deepCopy()，就像调用 Word 类自己的方法一样
 * 
 * 为什么需要深拷贝？
 * - 普通的复制（浅拷贝）只复制对象的引用，修改副本会影响原对象
 * - 深拷贝创建完全独立的副本，修改副本不会影响原对象
 * - 特别是 externalCaptions 和 captions 这些列表，需要逐个复制元素
 * 
 * @return 一个新的 Word 对象，包含相同的数据但完全独立
 */
fun Word.deepCopy():Word{
    // 创建新的 Word 对象，复制基本属性
    val newWord =  Word(
        value, usphone, ukphone, definition, translation, pos, collins, oxford, tag, bnc, frq, exchange
    )
    // 逐个复制外部例句（forEach 是 Kotlin 的循环方法）
    externalCaptions.forEach { externalCaption ->
        newWord.externalCaptions.add(externalCaption)
    }
    // 逐个复制内部例句
    captions.forEach { caption ->
        newWord.captions.add(caption)
    }
    return newWord
}


/**
 * 字幕数据类
 * 
 * 表示一条字幕，包含开始时间、结束时间和文本内容
 * 用于在学习单词时显示该单词在影视作品中的使用场景
 * 
 * @property start 字幕开始时间，格式如 "00:01:23,456"（时:分:秒,毫秒）
 * @property end 字幕结束时间
 * @property content 字幕文本内容，即电影或电视剧的台词
 */
@Serializable
data class Caption(var start: String, var end: String, var content: String) {
    /**
     * 重写 toString 方法
     * 
     * 当需要将 Caption 对象转换为字符串时（如打印、显示），只返回内容部分
     * 这样可以直接显示台词文本，而不是显示整个对象的内部结构
     */
    override fun toString(): String {
        return content
    }
}

/**
 * 外部字幕数据类
 * 
 * 表示通过"链接字幕词库"功能添加的外部例句
 * 与 Caption 的区别：ExternalCaption 记录了字幕的来源（哪个视频、哪个字幕文件）
 * 
 * 使用场景：
 * 当你学习一个单词时，可以链接其他影视作品中包含这个单词的字幕
 * 这样就能看到同一个单词在不同场景中的用法
 * 
 * @param relateVideoPath 关联的视频文件路径，用于播放对应的视频片段
 * @param subtitlesTrackId 字幕轨道 ID，MKV视频可能有多条字幕轨道
 * @param subtitlesName 字幕名称，用于在界面上显示，也用于批量删除某个字幕文件的所有例句
 * @param start 字幕开始时间
 * @param end 字幕结束时间
 * @param content 字幕文本内容
 */
@Serializable
data class ExternalCaption(
    val relateVideoPath: String,        // 视频路径
    val subtitlesTrackId: Int,          // 字幕轨道ID
    var subtitlesName: String,          // 字幕名称
    var start: String,                  // 开始时间
    var end: String,                    // 结束时间
    var content: String                 // 字幕内容
) {
    /**
     * 重写 toString 方法
     * 只返回字幕内容，便于显示
     */
    override fun toString(): String {
        return content
    }
}




fun loadMutableVocabulary(path: String): MutableVocabulary {
    val file = getResourcesFile(path)
    val name = file.nameWithoutExtension
    // 如果当前词库被删除，重启之后又没有选择新的词库，再次重启时才会调用，
    // 主要是为了避免再次重启是出现”找不到词库"对话框
    return if(path.isEmpty()){
        val vocabulary = Vocabulary(
            name = name,
            type = VocabularyType.DOCUMENT,
            language = "",
            size = 0,
            relateVideoPath = "",
            subtitlesTrackId = 0,
            wordList = mutableListOf()
        )
        MutableVocabulary(vocabulary)
    }else if (file.exists()) {

        try {
            val vocabulary = Json.decodeFromString<Vocabulary>(file.readText())
            if(vocabulary.size != vocabulary.wordList.size){
                vocabulary.size = vocabulary.wordList.size
            }
            // 如果用户修改了词库的文件名，以用户修改的为准。
            if(vocabulary.name != name){
                vocabulary.name = name
            }
            MutableVocabulary(vocabulary)
        } catch (exception: Exception) {
            exception.printStackTrace()
            val vocabulary = Vocabulary(
                name = name,
                type = VocabularyType.DOCUMENT,
                language = "",
                size = 0,
                relateVideoPath = "",
                subtitlesTrackId = 0,
                wordList = mutableListOf()
            )
            JOptionPane.showMessageDialog(null, "词库解析错误：\n地址：$path\n" + exception.message)
            if(vocabulary.size != vocabulary.wordList.size){
                vocabulary.size = vocabulary.wordList.size
            }
            MutableVocabulary(vocabulary)
        }

    } else {
        val vocabulary = Vocabulary(
            name = name,
            type = VocabularyType.DOCUMENT,
            language = "",
            size = 0,
            relateVideoPath = "",
            subtitlesTrackId = 0,
            wordList = mutableListOf()
        )
        JOptionPane.showMessageDialog(null, "找不到词库：\n$path")
        MutableVocabulary(vocabulary)
    }

}


/**
 * 加载普通词库（非可观察）
 * 
 * 与 loadMutableVocabulary 类似，但返回普通的 Vocabulary 对象
 * 用于不需要UI更新的场景，如词库导入、导出等
 * 
 * @param path 词库文件的相对路径
 * @return Vocabulary 普通词库对象
 */
fun loadVocabulary(path: String): Vocabulary {
    val file = getResourcesFile(path)  // 获取文件路径
    val name = file.nameWithoutExtension  // 提取文件名
    if (file.exists()) {
        return try {
            //  TODO 链接字幕词库时选取了一个错误文件，导致程序卡死。定位到这里  error:  Required array length 2147483638 + 16142 is too large
           val vocabulary =  Json.decodeFromString<Vocabulary>(file.readText())
            // 如果用户修改了词库的文件名，以用户修改的为准。
            if(vocabulary.name != name){
                vocabulary.name = name
            }
            vocabulary
        } catch (exception: Exception) {
            // 捕获解析错误
            exception.printStackTrace()
            JOptionPane.showMessageDialog(null, "词库解析错误：\n地址：$path\n" + exception.message)
            Vocabulary(
                name = name,
                type = VocabularyType.DOCUMENT,
                language = "",
                size = 0,
                relateVideoPath = "",
                subtitlesTrackId = 0,
                wordList = mutableListOf()
            )
        }
    } else {
        // 文件不存在，返回空词库
        return Vocabulary(
            name = name,
            type = VocabularyType.DOCUMENT,
            language = "",
            size = 0,
            relateVideoPath = "",
            subtitlesTrackId = 0,
            wordList = mutableListOf()
        )
    }

}
/**
 * 按名称加载可观察词库
 * 
 * 专门用于加载两个特殊词库：熟悉词库和困难词库
 * 这两个词库存储在固定位置，用于记录用户已掌握或需要加强的单词
 * 
 * @param name 词库名称，只能是 "FamiliarVocabulary"（熟悉词库）或 "HardVocabulary"（困难词库）
 * @return MutableVocabulary 可观察的词库对象
 */
fun loadMutableVocabularyByName(name: String):MutableVocabulary{
    // 根据名称获取对应的文件
    // if...else 是条件表达式，可以直接赋值给变量
    val file = if (name == "FamiliarVocabulary") {
        getFamiliarVocabularyFile()  // 获取熟悉词库文件
    } else getHardVocabularyFile()   // 获取困难词库文件

    return if (file.exists()) {
        // 文件存在，读取词库
        try {
            val vocabulary = Json.decodeFromString<Vocabulary>(file.readText())
            MutableVocabulary(vocabulary)
        } catch (exception: Exception) {
            // 文件内容损坏，返回空词库
            exception.printStackTrace()
            val vocabulary = Vocabulary(
                name = name,
                type = VocabularyType.DOCUMENT,
                language = "",
                size = 0,
                relateVideoPath = "",
                subtitlesTrackId = 0,
                wordList = mutableListOf()
            )
            JOptionPane.showMessageDialog(null, "词库解析错误：\n地址：${file.absoluteFile}\n" + exception.message)
            MutableVocabulary(vocabulary)
        }

    } else {
        // 文件不存在，创建空词库（首次使用时会自动创建）
        val vocabulary = Vocabulary(
            name = name,
            type = VocabularyType.DOCUMENT,
            language = "",
            size = 0,
            relateVideoPath = "",
            subtitlesTrackId = 0,
            wordList = mutableListOf()
        )
        MutableVocabulary(vocabulary)
}}


/**
 * 获取熟悉词库文件
 * 
 * 返回存储熟悉单词的文件对象
 * 熟悉词库：用户标记为"已掌握"的单词会被添加到这里
 * 
 * @return File 熟悉词库文件对象
 */
fun getFamiliarVocabularyFile():File{
    val settingsDir = getSettingsDirectory()  // 获取设置目录
    return File(settingsDir, "FamiliarVocabulary.json")  // 在设置目录下创建/获取文件
}

/**
 * 获取困难词库文件
 * 
 * 返回存储困难单词的文件对象
 * 困难词库：用户标记为"需要加强"的单词会被添加到这里
 * 
 * @return File 困难词库文件对象
 */
fun getHardVocabularyFile():File{
    val settingsDir = getSettingsDirectory()  // 获取设置目录
    return File(settingsDir, "HardVocabulary.json")  // 在设置目录下创建/获取文件
}

/**
 * 保存词库到临时目录
 * 
 * 用于开发和调试，将词库保存到临时文件夹
 * 不影响用户的正式词库文件
 * 
 * @param vocabulary 要保存的词库
 * @param directory 临时目录名称
 */
fun saveVocabularyToTempDirectory(vocabulary: Vocabulary, directory: String) {
    // 配置 JSON 格式化选项
    val format = Json {
        prettyPrint = true      // 美化输出，便于阅读
        encodeDefaults = true   // 包含默认值
    }
    val json = format.encodeToString(vocabulary)  // 将词库对象转换为 JSON 字符串
    val file = File("src/com.mujingx.main/resources/temp/$directory/${vocabulary.name}.json")
    
    // 确保父目录存在
    // exists() 检查目录是否存在，mkdirs() 创建多级目录
    if (!file.parentFile.exists()) {
        file.parentFile.mkdirs()
    }
    File("src/com.mujingx.main/resources/temp/$directory/${vocabulary.name}.json").writeText(json)
}

/**
 * 保存词库到指定路径
 * 
 * 将词库对象序列化为 JSON 格式并保存到文件
 * 这是保存用户词库的主要方法
 * 
 * @param vocabulary 要保存的词库对象
 * @param path 保存路径（相对于资源目录）
 */
fun saveVocabulary(vocabulary: Vocabulary, path: String) {
    // 配置 JSON 格式化选项
    val format = Json {
        prettyPrint = true      // 美化输出，方便手动编辑和查看
        encodeDefaults = true   // 包含所有字段，即使是默认值
    }
    val json = format.encodeToString(vocabulary)  // 序列化：对象 -> JSON 字符串
    val file = getResourcesFile(path)  // 获取完整文件路径
    file.writeText(json)  // 写入文件
}
