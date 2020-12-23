package cc.xiaobaicz.retrofit.logger.helper

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okio.Buffer
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * <p>
 *     请求Logger拦截器
 * </p>
 * <p>
 *     使用OkHttpClient$Builder#addInterceptor可输出一次原始请求内容
 *     使用OkHttpClient$Builder#addNetInterceptor可输出多次请求内容    PS（重定向）
 * </p>
 * @param oStream 输出流
 * @param isShowHeader 是否打印Http头数据
 */
class RequestLoggerInterceptor private constructor(val oStream: OutputStream, val isShowHeader: Boolean, val format: Format) : Interceptor {

    companion object {
        /**
         * 创建一个默认的Logger拦截器，输出到控制台，不打印头信息
         * @param out 是否输出
         */
        @JvmStatic
        fun create(out: Boolean = true): Interceptor = Builder().build(out)
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(format.format(this, oStream, chain.request()))
    }

    /**
     * 构建者
     */
    class Builder {
        /**
         * 输出流
         */
        private var oStream: OutputStream = System.out

        /**
         * 显示头信息？
         */
        private var isShowHeader: Boolean = false

        /**
         * 格式化到输出流
         */
        private var format: Format = DefFormat()

        /**
         * 设置输出流
         */
        fun setOutputStream(out: OutputStream): Builder {
            oStream = out
            return this
        }

        /**
         * 设置显示头信息
         */
        fun isShowHeader(showHeader: Boolean): Builder {
            isShowHeader = showHeader
            return this
        }

        /**
         * 设置输出格式化
         */
        fun setFormat(format: Format): Builder {
            this.format = format
            return this
        }

        /**
         * 构建一个拦截器
         * @param out 是否输出
         */
        fun build(out: Boolean = true): Interceptor = if (out) {
            RequestLoggerInterceptor(oStream, isShowHeader, format)
        } else {
            EmptyInterceptor()
        }
    }

    /**
     * 格式化输出
     */
    fun interface Format {
        /**
         * @param interceptor 拦截器信息
         * @param out 输出流
         * @param req 请求体
         * @return 请求体 -> 可原样返回
         */
        fun format(interceptor: RequestLoggerInterceptor, out: OutputStream, req: Request): Request
    }

    /**
     * 默认格式化实现
     */
    private class DefFormat : Format {
        override fun format(interceptor: RequestLoggerInterceptor, out: OutputStream, req: Request): Request {
            val sb = StringBuilder()
            sb.append("====================Request==========================").appendln()
            //时间
            sb.append(SimpleDateFormat.getInstance().format(Date())).appendln()
            //请求信息
            sb.append("${req.method()} ${req.url()}").appendln()
            if (interceptor.isShowHeader) {
                //头部信息
                sb.append("-----------------------------------------------------").appendln()
                sb.append(req.headers().toString()).appendln()
                sb.append("-----------------------------------------------------").appendln()
            } else {
                sb.appendln()
            }
            out.write(sb.toString().toByteArray())
            req.body()?.apply {
                //body信息
                Buffer().use { buff ->
                    this.writeTo(buff)
                    buff.copyTo(out)
                }
            }
            sb.clear()
            sb.appendln().append("=========================End=========================").appendln().appendln()
            //结束
            out.write(sb.toString().toByteArray())
            out.flush()
            return req
        }
    }
}