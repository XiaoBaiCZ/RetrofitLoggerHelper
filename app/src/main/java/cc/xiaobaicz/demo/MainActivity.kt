package cc.xiaobaicz.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cc.xiaobaicz.retrofit.logger.helper.RequestLoggerInterceptor
import cc.xiaobaicz.retrofit.logger.helper.ResponseLoggerInterceptor
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.*
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.File
import java.io.OutputStream
import java.io.RandomAccessFile

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //log文件
        val out = RandomAccessFile(File(filesDir, "log.txt"), "rw")
        //偏移到文件末尾
        out.seek(out.length())

        //包装成OutputStream
        val o = object : OutputStream() {
            override fun write(b: Int) {
                out.write(b)
            }
        }

        val r = Retrofit.Builder()
            .baseUrl("https://getman.cn/")
            .client(OkHttpClient.Builder()
//                响应同理
//                .addNetworkInterceptor(RequestLoggerInterceptor.create()) 默认true； true：默认输出到控制台； false：则不输出 PS(可通过BuildConfig.DEBUG动态配置)
//                .addNetworkInterceptor(RequestLoggerInterceptor.create(true))
//                .addNetworkInterceptor(RequestLoggerInterceptor.create(false))
//                .addNetworkInterceptor(RequestLoggerInterceptor.Builder().isShowHeader(true).setOutputStream(o).build()) 自由配置，是否显示头部信息，是否重定向输出流
//                addInterceptor 添加普通拦截器则仅记录初始内容
//                addNetworkInterceptor 添加网络拦截器则同时记录重定向内容
                .addNetworkInterceptor(RequestLoggerInterceptor.Builder()
                        .isShowHeader(true)
                        .setOutputStream(o)
                        .build(BuildConfig.DEBUG)
                )
                .addNetworkInterceptor(ResponseLoggerInterceptor.Builder()
                        .isShowHeader(true)
                        .setOutputStream(o)
                        .build(BuildConfig.DEBUG)
                )
                .build())
            .build()

        r.create<Api>().index().enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                println(response.body()?.string())
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                println(t)
            }
        })
    }

    interface Api {
        @GET("/mock/route/to/demo")
        fun index(): Call<ResponseBody>
    }
}