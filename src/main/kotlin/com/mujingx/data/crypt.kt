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

import com.sun.jna.platform.win32.Crypt32Util
import com.mujingx.player.isMacOS
import com.mujingx.player.isWindows
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * 加密工具类
 * 
 * 用于安全地存储敏感信息（如 API 密钥）
 * 根据不同操作系统使用对应的系统级加密服务
 * 
 * 为什么需要加密？
 * - 保护用户隐私：API密钥等敏感信息不应明文存储
 * - 系统安全：利用操作系统提供的安全机制
 * - 跨平台支持：不同系统有不同的加密方案
 * 
 * 各平台加密方式：
 * - Windows：使用 DPAPI（Data Protection API）
 * - macOS：使用 Keychain（钥匙串）
 * - Linux：使用 Keyring（密钥环）
 */
class Crypt{
    companion object{
        /**
         * 加密数据
         * 
         * 根据当前操作系统选择合适的加密方式
         * 
         * @param data 要加密的明文数据
         * @return 加密后的数据或标识符
         */
        fun encrypt(data: String): String {
            if(data.isEmpty()) return data
            return if(isWindows()) {
                DPAPI.encrypt(data)  // Windows：返回加密后的 Base64 字符串
            }else if(isMacOS()){
                Keychain.addItem(data)  // macOS：返回 "Keychain" 标识
            }else{
                Keyring.addItem(data)  // Linux：返回 "Keyring" 标识
            }
        }

        /**
         * 解密数据
         * 
         * 根据数据格式判断加密方式并解密
         * 
         * @param data 加密的数据或标识符
         * @return 解密后的明文数据
         */
        fun decrypt(data: String): String {
            if (data.isEmpty()) return ""
            return if (data.length == 32) {
                // 32位可能是未加密的密钥
                data
            } else if (data.length > 32) {
                // 长度大于32，使用 DPAPI 解密（Windows）
                DPAPI.decrypt(data)
            } else if (data == "Keychain") {
                // "Keychain" 标识，从 macOS 钥匙串获取
                Keychain.getItem()
            } else if (data == "Keyring") {
                // "Keyring" 标识，从 Linux 密钥环获取
                Keyring.getItem()
            } else data  // 其他情况直接返回
        }
    }

}

/**
 * Windows DPAPI 加密对象
 * 
 * DPAPI（Data Protection API）是 Windows 提供的数据保护接口
 * 使用用户的登录凭据自动加密/解密数据
 * 只有相同用户才能解密，提供了系统级的安全保护
 */
object DPAPI {
    /**
     * 使用 DPAPI 加密数据
     * 
     * @param data 要加密的字符串
     * @return Base64 编码的加密数据
     */
    fun encrypt(data: String): String {
        return try {
            // Crypt32Util.cryptProtectData：Windows DPAPI 加密函数
            // toByteArray：将字符串转换为字节数组
            val encryptedData = Crypt32Util.cryptProtectData(
                data.toByteArray(StandardCharsets.UTF_8),  // 要加密的数据
                "MuJing".toByteArray(),  // 附加信息（可选）
                0,  // 标志
                "",  // 描述
                null  // 提示
            )
            // Base64.getEncoder().encodeToString：将字节数组编码为 Base64 字符串
            Base64.getEncoder().encodeToString(encryptedData)
        } catch (e: Exception) {
            println("Error encrypting data: ${e.message}")
            return data  // 加密失败，返回原始数据
        }
    }

    /**
     * 使用 DPAPI 解密数据
     * 
     * @param data Base64 编码的加密数据
     * @return 解密后的字符串
     */
    fun decrypt(data: String): String {
        return try {
            // Base64.getDecoder().decode：将 Base64 字符串解码为字节数组
            // Crypt32Util.cryptUnprotectData：Windows DPAPI 解密函数
            val decryptedData = Crypt32Util.cryptUnprotectData(
                Base64.getDecoder().decode(data),
                "MuJing".toByteArray(),
                0,
                null
            )
            // String()：将字节数组转换为字符串
            String(decryptedData, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            println("Error decrypting data: ${e.message}")
            ""  // 解密失败，返回空字符串
        }
    }
}


/**
 * macOS Keychain（钥匙串）加密对象
 * 
 * Keychain 是 macOS 的密码管理系统
 * 通过系统的 security 命令行工具访问
 */
object Keychain{
    /**
     * 添加密码到钥匙串
     * 
     * @param password 要存储的密码
     * @return "Keychain" 标识字符串
     */
    fun addItem(password:String):String{
        val username = System.getProperty("user.name")  // 获取当前用户名
        
        // 构造 security 命令
        // security add-generic-password：添加通用密码
        val command = listOf(
            "security", "add-generic-password",
            "-a", username,           // 账户名
            "-w", password,           // 密码
            "-s", "MuJing Service"    // 服务名
        )

        try {
            // ProcessBuilder：用于执行系统命令
            val process = ProcessBuilder(command).start()
            process.waitFor()  // 等待命令执行完成
        } catch (e: Exception) {
            println("Error adding item to Keychain: ${e.message}")
            return password  // 失败时返回原始密码
        }
        return "Keychain"  // 成功时返回标识符
    }
    
    /**
     * 从钥匙串获取密码
     * 
     * @return 存储的密码字符串
     */
    fun getItem(): String {
        val username = System.getProperty("user.name")
        
        // security find-generic-password：查找通用密码
        // -w：只输出密码，不输出其他信息
        val command = listOf(
            "security", "find-generic-password",
            "-a", username,
            "-s", "MuJing Service",
            "-w"
        )

        return try {
            val process = ProcessBuilder(command).start()
            // bufferedReader：读取命令输出
            val reader = process.inputStream.bufferedReader()
            val password = reader.readLine()  // 读取一行（密码）
            process.waitFor()
            password
        } catch (e: Exception) {
            println("Error retrieving item from Keychain: ${e.message}")
            ""  // 失败时返回空字符串
        }
    }

}


/**
 * Linux Keyring（密钥环）加密对象
 * 
 * 使用 GNOME Keyring 或其他兼容的密钥环服务
 * 通过 secret-tool 命令行工具访问
 */
object Keyring{
    /**
     * 添加密码到密钥环
     * 
     * @param password 要存储的密码
     * @return "Keyring" 标识字符串
     */
    fun addItem(password: String):String {
        // secret-tool store：存储密码
        // --label：密码的标签
        val command = listOf(
            "/bin/sh", "-c",
            "echo -n $password | secret-tool store --label='MuJing Password' MuJing-Service AzureKey"
        )

        try {
            val process = ProcessBuilder(command).start()
            process.waitFor()
        } catch (e: Exception) {
            println("Error adding item to Keyring: ${e.message}")
            return password  // 失败时返回原始密码
        }
        return "Keyring"  // 成功时返回标识符
    }

    /**
     * 从密钥环获取密码
     * 
     * @return 存储的密码字符串
     */
    fun getItem(): String {
        // secret-tool lookup：查找密码
        val command = listOf(
            "secret-tool", "lookup",
            "service:", "MuJing AzureKey Service"
        )

        return try {
            val process = ProcessBuilder(command).start()
            val reader = process.inputStream.bufferedReader()
            val password = reader.readLine()
            process.waitFor()
            password
        } catch (e: Exception) {
            println("Error retrieving item from Keyring: ${e.message}")
            ""  // 失败时返回空字符串
        }
    }
}