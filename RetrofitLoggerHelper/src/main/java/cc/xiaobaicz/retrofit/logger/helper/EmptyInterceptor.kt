package cc.xiaobaicz.retrofit.logger.helper

import okhttp3.Interceptor
import okhttp3.Response

/**
 * 空的拦截器
 */
internal class EmptyInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        //不做特殊处理
        return chain.proceed(chain.request())
    }
}