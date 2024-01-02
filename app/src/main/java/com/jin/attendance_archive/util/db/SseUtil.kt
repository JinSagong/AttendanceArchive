package com.jin.attendance_archive.util.db

import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import com.here.oksse.OkSse
import com.here.oksse.ServerSentEvent
import com.jin.attendance_archive.ScreenManager
import com.jin.attendance_archive.util.Debug
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableEmitter
import okhttp3.Request
import okhttp3.Response

class SseUtil(private val url: String) {
    private val okSse by lazy { OkSse() }
    private var sse: ServerSentEvent? = null
    private val sseDataUtil = SseDataUtil()

    fun connect(): Flowable<HashMap<String, JsonPrimitive>> {
        var emitter: FlowableEmitter<HashMap<String, JsonPrimitive>>? = null
        val listener = object : ServerSentEvent.Listener {
            override fun onOpen(sse: ServerSentEvent?, response: Response?) {
                Debug.request("onOpen=$url")
            }

            override fun onClosed(sse: ServerSentEvent?) {
                Debug.response("onClosed=$url")
                emitter?.onComplete()
            }

            override fun onMessage(
                sse: ServerSentEvent?,
                id: String?,
                event: String?,
                message: String?
            ) {
                if (event == "put" || event == "patch") {
                    parse(message)
                    val data = sseDataUtil.mapResult
                    Debug.response("onNext=$data")
                    emitter?.onNext(data)
                }
            }

            override fun onRetryError(
                sse: ServerSentEvent?,
                throwable: Throwable?,
                response: Response?
            ): Boolean {
                // desktop -> 진입시 에러, automatic pause/resume 동작
                // mobile -> onStart/onStop 체크, Network Check, 진입시 에러도 똑같음
                ScreenManager.openErrorScreen()
                if (throwable != null) {
                    Debug.error(throwable)
                    emitter?.onError(throwable)
                }
                return false
            }

            override fun onComment(sse: ServerSentEvent?, comment: String?) {
                Debug.i("onComment=$comment")
            }

            override fun onRetryTime(sse: ServerSentEvent?, milliseconds: Long): Boolean {
                Debug.i("onRetryTime=$milliseconds")
                return true
            }

            override fun onPreRetry(sse: ServerSentEvent?, originalRequest: Request?): Request {
                Debug.i("onRetryError=${originalRequest}")
                return originalRequest!!
            }
        }
        sse = okSse.newServerSentEvent(Request.Builder().url(url).build(), listener)

        return Flowable.create({ emitter = it }, BackpressureStrategy.BUFFER)
    }

    fun close() {
        sse?.close()
    }

    private fun parse(message: String?) {
        val parser = JsonParser()
        val element = parser.parse(message.orEmpty())
        val json = if (element?.isJsonObject == true) element.asJsonObject else null
        val path = json?.get("path")?.asString()?.split("/")?.mapNotNull { it.ifEmpty { null } }
        val data = json?.get("data")
        if (path != null && data != null) sseDataUtil.setMessage(path, data)
    }
}