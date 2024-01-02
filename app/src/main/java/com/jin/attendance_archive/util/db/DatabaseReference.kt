package com.jin.attendance_archive.util.db

import io.reactivex.Single
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

class DatabaseReference private constructor(child: String) : Query(child) {
    private var sseUtil: SseUtil? = null

    private fun getUrl(): String {
        var url = "$BASE_URL/$children.json"
        var params = ""
        orderBy?.let { params += "&orderBy=$it" }
        limitToFirst?.let { params += "&limitToFirst=$it" }
        limitToLast?.let { params += "&limitToLast=$it" }
        startAtString?.let { params += "&startAt=$it" }
            ?: startAtInt?.let { params += "&startAt=$it" }
        endAtString?.let { params += "&endAt=$it" }
            ?: endAtInt?.let { params += "&endAt=$it" }
        equalToString?.let { params += "&equalTo=$it" }
            ?: equalToInt?.let { params += "&equalTo=$it" }
            ?: equalToBoolean?.let { params += "&equalTo=$it" }
        if (params.isNotEmpty()) url += params.replaceFirst('&', '?')
        return url
    }

    override fun getValue() = retrofitService.getData(
        children,
        orderBy,
        limitToFirst,
        limitToLast,
        startAtString ?: startAtInt,
        endAtString ?: endAtInt,
        equalToString ?: equalToInt ?: equalToBoolean
    )
        .single(getUrl())

    override fun observeValue() = SseUtil(getUrl())
        .also { sseUtil = it }
        .connect()
        .flowable()

    fun stopObserving() {
        sseUtil?.close()
    }

    override fun setValue(value: Any): Single<Any> = retrofitService
        .putData(children, value)
        .single(getUrl())

    companion object {
        private const val BASE_URL =
            "https://attendance-archive-default-rtdb.asia-southeast1.firebasedatabase.app"

        private val retrofitService by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create<FirebaseRetrofitService>()
        }

        fun child(child: String) = DatabaseReference(child)
    }
}