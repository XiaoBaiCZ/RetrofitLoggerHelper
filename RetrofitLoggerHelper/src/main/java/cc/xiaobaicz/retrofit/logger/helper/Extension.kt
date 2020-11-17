package cc.xiaobaicz.retrofit.logger.helper

import java.lang.StringBuilder

/**
 * 插入换行符
 */
fun StringBuilder.appendln(): StringBuilder {
    append("\n")
    return this
}