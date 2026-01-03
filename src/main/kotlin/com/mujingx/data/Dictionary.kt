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

import com.mujingx.player.isMacOS
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException

/**
 * 把结果集映射成单词
 * 
 * 这个函数的作用：将数据库查询返回的 ResultSet（结果集）转换成我们程序中使用的 Word 对象
 * 
 * @param result 数据库查询返回的结果集，包含单词的各种信息
 * @return Word 对象，包含了单词的完整信息
 * 
 * 编程思想：
 * 1. 数据映射 - 将数据库格式转换为程序对象格式
 * 2. 空值处理 - 确保数据安全，避免空指针异常
 */
fun mapToWord(result: ResultSet): Word {
    // 从结果集中获取各个字段的值
    // result.getString("列名") 是 JDBC API，用于获取数据库中指定列的字符串值
    var value = result.getString("word")  // 获取单词本身，如 "hello"
    var uKphone = result.getString("british_phonetic")  // 获取英式音标
    var usphone = result.getString("american_phonetic")  // 获取美式音标
    var definition = result.getString("definition")  // 获取英文释义
    var translation = result.getString("translation")  // 获取中文翻译
    var pos = result.getString("pos")  // 获取词性（part of speech），如 n. v. adj.
    val collins = result.getInt("collins")  // 获取柯林斯词典星级（整数）
    val oxford = result.getBoolean("oxford")  // 获取是否是牛津3000词（布尔值）
    var tag = result.getString("tag")  // 获取标签信息
    val bnc = result.getInt("bnc")  // 获取 BNC 词频（英国国家语料库词频）
    val frq = result.getInt("frq")  // 获取 COCA 词频（当代美国英语语料库词频）
    var exchange = result.getString("exchange")  // 获取单词的变形，如复数、过去式等

    // 空值处理：数据库中的某些字段可能为 null，需要转换为空字符串
    // 这样可以避免后续使用时出现空指针异常（NullPointerException）
    if (value == null) value = ""
    if (uKphone == null) uKphone = ""
    if (usphone == null) usphone = ""
    if (definition == null) definition = ""
    if (translation == null) translation = ""
    if (pos == null) pos = ""
    if (tag == null) tag = ""
    if (exchange == null) exchange = ""

    // 处理换行符：数据库中存储的是 "\\n" 字符串，需要替换为真正的换行符 \n
    // replace 是 Kotlin 的字符串方法，用于替换字符串中的内容
    definition = definition.replace("\\n", "\n")
    translation = translation.replace("\\n", "\n")
    
    // 创建并返回 Word 对象
    // Word 是一个数据类（data class），用于存储单词的所有信息
    // mutableListOf() 创建一个可变列表，用于存储字幕等额外信息
    return Word(
        value,          // 单词
        usphone,        // 美式音标
        uKphone,        // 英式音标
        definition,     // 英文释义
        translation,    // 中文翻译
        pos,            // 词性
        collins,        // 柯林斯星级
        oxford,         // 是否牛津3000词
        tag,            // 标签
        bnc,            // BNC词频
        frq,            // COCA词频
        exchange,       // 单词变形
        mutableListOf(),  // 外部字幕列表（初始为空）
        mutableListOf()   // 字幕列表（初始为空）
    )
}
/**
 * Dictionary 对象 - 词典数据库操作类
 * 
 * 这是一个单例对象（object），在整个程序中只有一个实例
 * 负责所有与词典数据库（ecdict.db）相关的操作，包括查询、插入、删除等
 * 
 * 什么是 object？
 * - object 是 Kotlin 的单例模式，创建一个全局唯一的对象
 * - 不需要使用 new 关键字创建实例，直接通过 Dictionary.query() 调用方法
 * - 适合用于工具类、管理类等不需要多个实例的场景
 * 
 * 数据库技术：
 * - 使用 SQLite 数据库存储词典数据
 * - 使用 JDBC (Java Database Connectivity) API 进行数据库操作
 */
object Dictionary{
    /**
     * 获取 SQLite 数据库的连接 URL
     * 
     * 这是一个私有函数（private），只在 Dictionary 对象内部使用
     * 
     * @param fileName 数据库文件名，如 "ecdict.db"
     * @return 数据库连接 URL，格式如 "jdbc:sqlite:路径/文件名"
     * 
     * 编程思想：
     * 1. 环境适配 - 根据不同的运行环境（开发环境或打包后）返回不同的数据库路径
     * 2. 跨平台支持 - 区分 macOS 和其他操作系统的文件路径
     */
    private fun getSQLiteURL(fileName:String): String {
        // 获取系统属性，判断当前运行环境
        val property = "compose.application.resources.dir"
        val dir = System.getProperty(property)  // System.getProperty() 用于获取 JVM 系统属性
        
        // 根据环境返回不同的数据库路径
        return if (dir != null && !dir.endsWith("prepareAppResources")) {
            // 这个分支处理打包之后的应用程序环境
            if(isMacOS()){
                // macOS 系统：应用程序安装在 /Applications 目录下
                "jdbc:sqlite:file:/Applications/幕境.app/Contents/app/resources/dictionary/$fileName"
            }else{
                // Windows/Linux 系统：使用相对路径
                "jdbc:sqlite:app/resources/dictionary/$fileName"
            }
        } else {
            // 开发环境：使用项目中的资源目录
            "jdbc:sqlite:resources/common/dictionary/$fileName"
        }
    }

    /**
     * 查询一个单词
     * 
     * @param word 要查询的单词，如 "hello"
     * @return 如果找到返回 Word 对象，如果没找到返回 null
     * 
     * 数据库操作流程：
     * 1. 建立数据库连接
     * 2. 准备 SQL 查询语句
     * 3. 执行查询
     * 4. 处理结果
     * 5. 关闭连接（use 会自动关闭）
     * 
     * Kotlin 语法说明：
     * - use {} 是 Kotlin 的资源管理语法，类似 Java 的 try-with-resources
     * - 它会在代码块执行完后自动关闭资源（数据库连接、语句等），即使发生异常也会关闭
     * - 这样可以避免资源泄漏
     */
    fun query(word: String): Word? {
        try {
            // 获取数据库连接 URL
            val url = getSQLiteURL("ecdict.db")
            
            // DriverManager.getConnection(url) 创建数据库连接
            // use { conn -> ... } 使用连接，执行完后自动关闭
            DriverManager.getConnection(url).use { conn ->
                // 准备 SQL 查询语句
                // ? 是占位符，用于参数化查询，可以防止 SQL 注入攻击
                val sql = "SELECT * from ecdict WHERE word = ?"
                
                // prepareStatement 创建预处理语句
                conn.prepareStatement(sql).use { statement ->
                    try {
                        // 设置占位符的值，第一个参数是位置（从1开始），第二个参数是值
                        statement.setString(1, word)
                        
                        // executeQuery() 执行查询，返回结果集
                        val result = statement.executeQuery()
                        
                        // while (result.next()) 遍历结果集
                        // result.next() 移动到下一行，如果有数据返回 true，没有返回 false
                        while (result.next()) {
                            // 找到单词，将结果集映射为 Word 对象并返回
                            return mapToWord(result)
                        }
                    } catch (se: SQLException) {
                        // SQLException 是 SQL 异常，如查询语法错误、连接失败等
                        // printStackTrace() 打印异常堆栈信息，方便调试
                        se.printStackTrace()
                    }
                }
            }

        } catch (e: Exception) {
            // 捕获其他异常，如数据库驱动加载失败等
            e.printStackTrace()
        }
        // 如果没有找到单词或发生异常，返回 null
        return null
    }

    /**
     * 查询一个单词列表
     * 
     * @param words 要查询的单词列表，如 ["hello", "world", "kotlin"]
     * @return 查询到的单词对象列表
     * 
     * 注意：这个方法会按顺序逐个查询每个单词，效率较低
     * 如果不需要保证查询顺序，建议使用 fastQueryList() 方法
     * 
     * Kotlin 语法说明：
     * - mutableListOf<Word>() 创建一个可变列表，可以添加、删除元素
     * - forEach { } 是 Kotlin 的集合遍历方法，对列表中的每个元素执行操作
     * - 它是函数式编程风格，比传统的 for 循环更简洁
     */
    fun queryList(words: List<String>): MutableList<Word> {
        // 创建结果列表，用于存储查询到的单词
        val results = mutableListOf<Word>()
        
        try {
            val url = getSQLiteURL("ecdict.db")
            DriverManager.getConnection(url).use { conn ->
                // 准备 SQL 查询语句（每次查询一个单词）
                val sql = "SELECT * from ecdict WHERE word = ?"
                conn.prepareStatement(sql).use { statement ->
                    // forEach 遍历单词列表，对每个单词执行查询
                    // { word -> ... } 是 lambda 表达式，word 是当前遍历到的单词
                    words.forEach { word ->
                        try {
                            // 设置查询参数为当前单词
                            statement.setString(1, word)
                            val result = statement.executeQuery()
                            
                            // 处理查询结果
                            while (result.next()) {
                                val resultWord = mapToWord(result)
                                results.add(resultWord)  // 将查询到的单词添加到结果列表
                            }
                        } catch (se: SQLException) {
                            // 捕获异常但继续处理下一个单词
                            se.printStackTrace()
                        }
                    }
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return results
    }
    /**
     * 批量快速查询一个列表（不保证查询顺序）
     * 
     * @param words 要查询的单词列表
     * @return 查询到的单词对象列表（顺序可能与输入不一致）
     * 
     * 性能优化：
     * - 使用 SQL IN 子句一次查询多个单词，比逐个查询快得多
     * - 例如：SELECT * FROM ecdict WHERE word IN (?, ?, ?)
     * 
     * Kotlin 语法说明：
     * - joinToString { "?" } 是集合方法，将列表转换为字符串
     * - 它会用逗号分隔每个元素，每个元素都映射为 "?"
     * - 例如：["hello", "world"] -> "?, ?"
     * - forEachIndexed { index, word -> ... } 遍历时同时获取索引和值
     */
    fun fastQueryList(words: List<String>): MutableList<Word> {
        val results = mutableListOf<Word>()
        try {
            val url = getSQLiteURL("ecdict.db")
            DriverManager.getConnection(url).use { conn ->
                // 构建 SQL 语句，使用 IN 子句批量查询
                // joinToString { "?" } 将单词列表转换为 ?, ?, ? 形式的占位符
                val sql = "SELECT * from ecdict WHERE word IN (${words.joinToString { "?" }})"
                
                conn.prepareStatement(sql).use { statement ->
                    // forEachIndexed 遍历时获取索引和值
                    // 索引从 0 开始，但 SQL 占位符从 1 开始，所以要 +1
                    words.forEachIndexed { index, word ->
                        statement.setString(index + 1, word)
                    }
                    
                    // 一次执行查询所有单词
                    val result = statement.executeQuery()
                    while (result.next()) {
                        val resultWord = mapToWord(result)
                        results.add(resultWord)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return results
    }

    /**
     * 根据 BNC 词频区间查询单词
     * 
     * BNC（British National Corpus）：英国国家语料库
     * BNC 词频数值越小，表示单词使用频率越高，越常用
     * 
     * @param start 起始词频值
     * @param end 结束词频值
     * @return 符合条件的单词列表，按词频从小到大排序（从常用到不常用）
     * 
     * SQL 知识：
     * - WHERE bnc != 0 排除词频为0的单词（没有词频数据的单词）
     * - AND bnc >= $start AND bnc <= $end 指定词频范围
     * - ORDER BY bnc 按词频排序（升序，从小到大）
     * 
     * 注意：这里使用字符串插值 $start $end，实际项目中建议使用参数化查询防止 SQL 注入
     */
    fun queryByBncRange(start:Int,end:Int):List<Word>{
        // 构建 SQL 查询语句
        // $ 符号用于字符串插值，将变量值嵌入字符串中
        val sql = "SELECT * FROM ecdict WHERE bnc != 0  AND bnc >= $start AND bnc <= $end" +
                " ORDER BY bnc"
        val results = mutableListOf<Word>()
        try{
            val url = getSQLiteURL("ecdict.db")
            DriverManager.getConnection(url).use { conn ->
                // createStatement() 创建普通语句（非预处理语句）
                conn.createStatement().use { statement ->
                    val result = statement.executeQuery(sql)
                    while(result.next()){
                        val word = mapToWord(result)
                        results.add(word)
                    }
                }
            }
        }catch (se:SQLException){
            se.printStackTrace()
        }
        return results
    }

    /**
     * 查询所有 BNC 词频小于 num 的单词
     * 
     * @param num 词频上限
     * @return 词频小于 num 的所有单词，按词频排序
     * 
     * 应用场景：
     * 例如查询最常用的 5000 个单词，可以调用 queryByBncLessThan(5000)
     */
    fun queryByBncLessThan(num:Int):List<Word>{
        val sql = "SELECT * FROM ecdict WHERE bnc < $num AND bnc != 0 " +
                "ORDER BY bnc"
        val results = mutableListOf<Word>()
        try{
            val url = getSQLiteURL("ecdict.db")
            DriverManager.getConnection(url).use { conn ->
                conn.createStatement().use { statement ->
                    val result = statement.executeQuery(sql)
                    while(result.next()){
                        val word = mapToWord(result)
                        results.add(word)
                    }
                }
            }
        }catch (se:SQLException){
            se.printStackTrace()
        }
        return results
    }

    /**
     * 根据 COCA 词频区间查询单词
     * 
     * COCA（Corpus of Contemporary American English）：当代美国英语语料库
     * FRQ 是 COCA 词频（frequency 的缩写）
     * 与 BNC 类似，词频值越小表示越常用
     * 
     * @param start 起始词频值
     * @param end 结束词频值
     * @return 符合条件的单词列表，按词频从小到大排序
     */
    fun queryByFrqRange(start:Int,end: Int):List<Word>{
        val sql = "SELECT * FROM ecdict WHERE frq != 0   AND frq >= $start AND frq <= $end" +
                " ORDER BY frq"
        val results = mutableListOf<Word>()
        try{
            val url = getSQLiteURL("ecdict.db")
            DriverManager.getConnection(url).use { conn ->
                conn.createStatement().use { statement ->
                    val result = statement.executeQuery(sql)
                    while(result.next()){
                        val word = mapToWord(result)
                        results.add(word)
                    }
                }
            }
        }catch (se:SQLException){
            se.printStackTrace()
        }
        return results
    }

    /**
     * 查询所有 COCA 词频小于 num 的单词
     * 
     * @param num 词频上限
     * @return 词频小于 num 的所有单词，按词频排序
     */
    fun queryByFrqLessThan(num:Int):List<Word>{
        val sql = "SELECT * FROM ecdict WHERE frq < $num AND frq != 0 " +
                "ORDER BY frq"
        val results = mutableListOf<Word>()
        try{
            val url = getSQLiteURL("ecdict.db")
            DriverManager.getConnection(url).use { conn ->
                conn.createStatement().use { statement ->
                    val result = statement.executeQuery(sql)
                    while(result.next()){
                        val word = mapToWord(result)
                        results.add(word)
                    }
                }
            }
        }catch (se:SQLException){
            se.printStackTrace()
        }
        return results
    }

    /**
     * 执行更新操作
     * 
     * 用于执行 UPDATE、INSERT、DELETE 等修改数据的 SQL 语句
     * 
     * @param sql 要执行的 SQL 语句
     * 
     * SQL 操作类型：
     * - UPDATE：更新现有数据
     * - INSERT：插入新数据
     * - DELETE：删除数据
     * 
     * 与 executeQuery 的区别：
     * - executeQuery：执行 SELECT 查询，返回结果集
     * - executeUpdate：执行修改操作，返回受影响的行数
     */
    fun executeUpdate(sql: String) {
        try {
            val url = getSQLiteURL("ecdict.db")
            DriverManager.getConnection(url).use { conn ->
                conn.createStatement().use { statement ->
                    // executeUpdate() 执行 SQL 更新语句
                    statement.executeUpdate(sql)
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 执行通用 SQL 命令
     * 
     * @param sql 要执行的 SQL 命令
     * 
     * 常用场景：
     * - "VACUUM"：清理数据库中的未使用空间，优化数据库文件大小
     * - 其他数据库维护命令
     * 
     * execute 与 executeUpdate 的区别：
     * - execute：可以执行任何 SQL 语句，包括不返回结果的命令
     * - executeUpdate：主要用于 DML（数据操作语言）语句
     */
    fun execute(sql: String) {
        try {
            val url = getSQLiteURL("ecdict.db")
            DriverManager.getConnection(url).use { conn ->
                conn.createStatement().use { statement ->
                    // execute() 执行 SQL 命令
                    statement.execute(sql)
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 查询 BNC 词频的最大值
     * 
     * @return BNC 词频的最大值，如果查询失败返回 0
     * 
     * SQL 知识：
     * - MAX(bnc)：聚合函数，返回 bnc 列的最大值
     * - as max_bnc：给结果列起别名
     * 
     * 应用场景：
     * 用于了解数据库中词频数据的范围，便于设置词频筛选条件
     */
    fun queryBncMax():Int{
        try {
            val url = getSQLiteURL("ecdict.db")
            DriverManager.getConnection(url).use { conn ->
                val sql = "SELECT MAX(bnc) as max_bnc from ecdict"
                conn.createStatement().use {statement->
                    val result = statement.executeQuery(sql)
                    // result.next() 移动到第一行结果
                    if(result.next()){
                        // getInt(1) 获取第一列的整数值（索引从1开始）
                        return result.getInt(1)
                    }else return 0
                }

            }

        } catch (e: Exception) {
            e.printStackTrace()
            return 0
        }
    }

    /**
     * 查询 COCA 词频的最大值
     * 
     * @return COCA 词频的最大值，如果查询失败返回 0
     * 
     * SQL 知识：
     * - MAX(frq)：聚合函数，返回 frq 列的最大值
     */
    fun queryFrqMax():Int{
        try {
            val url = getSQLiteURL("ecdict.db")
            DriverManager.getConnection(url).use { conn ->
                val sql = "SELECT MAX(frq) as max_frq from ecdict"
                conn.createStatement().use {statement ->
                    val result = statement.executeQuery(sql)
                    if(result.next()){
                        return result.getInt(1)
                    }else return 0
                }

            }

        } catch (e: Exception) {
            e.printStackTrace()
            return 0
        }
    }

    /**
     * 查询内置词典的单词总数
     * 
     * @return 词典中的单词总数，如果查询失败返回 0
     * 
     * SQL 知识：
     * - COUNT(*)：聚合函数，统计表中的总行数
     * - as count：给结果列起别名
     */
    fun wordCount():Int{
        try {
            val url = getSQLiteURL("ecdict.db")
            DriverManager.getConnection(url).use { conn ->
                val sql = "SELECT COUNT(*) as count from ecdict"
                conn.createStatement().use{ statement ->
                    val result = statement.executeQuery(sql)
                    if(result.next()){
                        return result.getInt(1)
                    }else return 0
                }

            }

        } catch (e: Exception) {
            e.printStackTrace()
            return 0
        }
    }

    /**
     * 查询所有单词
     * 
     * @return 数据库中的所有单词列表
     * 
     * 注意：此操作会加载整个词典到内存，数据量大时可能消耗较多内存
     * 
     * SQL 知识：
     * - SELECT * from ecdict：查询表中所有行和所有列
     */
    fun queryAllWords():List<Word>{
        val words = mutableListOf<Word>()
        try {
            val url = getSQLiteURL("ecdict.db")
            DriverManager.getConnection(url).use { conn ->
                val sql = "SELECT * from ecdict"
                conn.createStatement().use{ statement ->
                    val result = statement.executeQuery(sql)
                    // 遍历所有结果行
                    while(result.next()){
                        val word = mapToWord(result)
                        words.add(word)
                    }
                    // 关闭语句（use 会自动关闭，这里显式调用是为了确保资源释放）
                    statement.close()
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            return words
        }
        return words
    }

    /**
     * 批量插入单词
     * 
     * @param words 要插入的单词列表
     * 
     * 性能优化：使用事务和批处理
     * 1. 事务（Transaction）：将多个操作作为一个整体，要么全部成功，要么全部失败
     *    - 好处：保证数据一致性，提高性能
     * 2. 批处理（Batch）：将多个 SQL 语句打包一起执行
     *    - 好处：减少网络开销，大幅提高插入速度
     * 
     * Kotlin 语法说明：
     * - word.bnc!! 中的 !! 是非空断言操作符
     * - 它告诉编译器："我确定这个值不是 null，如果是 null 就抛出异常"
     * - 使用时要确保值真的不为 null，否则会抛出 NullPointerException
     */
    fun insertWords(words:List<Word>){
        try {
            val url = getSQLiteURL("ecdict.db")
            DriverManager.getConnection(url).use { conn ->
                // 关闭自动提交，开始事务
                // 事务可以将多个操作打包，提高性能并保证数据一致性
                conn.autoCommit = false
                
                // 准备 SQL 插入语句，12个 ? 对应12个字段
                val sql = "INSERT INTO ecdict(word,british_phonetic,american_phonetic,definition,translation,pos,collins,oxford,tag,bnc,frq,exchange) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)"
                conn.prepareStatement(sql).use { statement ->
                    // 遍历所有要插入的单词
                    words.forEach { word ->
                        // 为每个占位符设置值
                        statement.setString(1,word.value)       // 单词
                        statement.setString(2,word.ukphone)     // 英式音标
                        statement.setString(3,word.usphone)     // 美式音标
                        statement.setString(4,word.definition)  // 英文释义
                        statement.setString(5,word.translation) // 中文翻译
                        statement.setString(6,word.pos)         // 词性
                        statement.setInt(7,word.collins)        // 柯林斯星级
                        statement.setBoolean(8,word.oxford)     // 是否牛津3000词
                        statement.setString(9,word.tag)         // 标签
                        statement.setInt(10, word.bnc!!)        // BNC词频（!!确保不为null）
                        statement.setInt(11, word.frq!!)        // COCA词频（!!确保不为null）
                        statement.setString(12,word.exchange)   // 单词变形
                        
                        // addBatch() 将当前 SQL 添加到批处理中
                        // 不会立即执行，而是等待 executeBatch() 时一起执行
                        statement.addBatch()
                    }
                    // executeBatch() 批量执行所有添加的 SQL 语句
                    statement.executeBatch()
                }
                // 提交事务，使所有插入操作生效
                conn.commit()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 删除一个单词
     * 
     * @param word 要删除的单词
     * 
     * SQL 知识：
     * - DELETE FROM：删除表中的数据
     * - WHERE word = ?：指定删除条件，只删除匹配的单词
     * 
     * 注意：删除操作不可恢复，使用时要谨慎
     */
    fun deleteWord(word:String){
        try {
            val url = getSQLiteURL("ecdict.db")
            DriverManager.getConnection(url).use { conn ->
                // 准备删除语句，使用参数化查询防止 SQL 注入
                val sql = "DELETE FROM ecdict WHERE word = ?"
                conn.prepareStatement(sql).use { statement ->
                    // 设置要删除的单词
                    statement.setString(1,word)
                    // 执行删除操作
                    statement.executeUpdate()
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}




// 使用 SQLite 数据库
//fun createNewDatabase(fileName:String){
//    val url = getSQLiteURL(fileName)
//
//    DriverManager.getConnection(url).use { conn ->
//        if(conn != null){
//            val meta = conn.metaData
//            println("The driver name is ${meta.driverName}")
//            println("A new database has been created.")
//        }
//    }
//}
//
//
//fun insertWords(words:List<Word>){
//    try {
//        val url = getSQLiteURL("ecdict.db")
//        DriverManager.getConnection(url).use { conn ->
//            val sql = "INSERT INTO ecdict(word,british_phonetic,american_phonetic,definition,translation,pos,collins,oxford,tag,bnc,frq,exchange) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)"
//            conn.prepareStatement(sql).use { statement ->
//                words.forEach { word ->
//                    statement.setString(1,word.value)
//                    statement.setString(2,word.ukphone)
//                    statement.setString(3,word.usphone)
//                    statement.setString(4,word.definition)
//                    statement.setString(5,word.translation)
//                    statement.setString(6,word.pos)
//                    statement.setInt(7,word.collins)
//                    statement.setBoolean(8,word.oxford)
//                    statement.setString(9,word.tag)
//                    statement.setInt(10, word.bnc!!)
//                    statement.setInt(11, word.frq!!)
//                    statement.setString(12,word.exchange)
//                    statement.addBatch()
//                }
//                statement.executeBatch()
//            }
//        }
//
//    } catch (e: Exception) {
//        //Handle errors for Class.forName
//        e.printStackTrace()
//    }
//}
//
//fun createNewTable(){
//    val url = getSQLiteURL("ecdict.db")
//    try{
//        DriverManager.getConnection(url).use { conn ->
//            val sql = "CREATE TABLE IF NOT EXISTS ecdict" +
//                    "(word VARCHAR(64) NOT NULL UNIQUE , " +
//                    " british_phonetic VARCHAR(64), " +
//                    " american_phonetic VARCHAR(64), " +
//                    " definition TEXT, " +
//                    " translation TEXT, " +
//                    " pos VARCHAR(16), " +
//                    " collins INTEGER DEFAULT (0), " +
//                    " oxford BOOLEAN DEFAULT (0), " +
//                    " tag VARCHAR(64), " +
//                    " bnc INTEGER DEFAULT (0), " +
//                    " frq INTEGER DEFAULT (0), " +
//                    " exchange VARCHAR(256), " +
//                    " detail VARCHAR(64), " +
//                    " audio VARCHAR(64), " +
//                    " PRIMARY KEY ( word ))"
//            conn.createStatement().use { statement ->
//                statement.execute(sql)
//            }
//        }
//    }catch (e:SQLException){
//        e.printStackTrace()
//    }
//}

