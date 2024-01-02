package com.jin.attendance_archive.util.db

import io.reactivex.Single
import retrofit2.http.*
import retrofit2.http.Query

interface FirebaseRetrofitService {
    @GET("/{child}.json")
    fun getData(
        @Path("child", encoded = true) child: String = "",
        @Query("orderBy", encoded = true) orderBy: String? = null,
        @Query("limitToFirst", encoded = true) limitToFirst: Int? = null,
        @Query("limitToLast", encoded = true) limitToLast: Int? = null,
        @Query("startAt", encoded = true) startAt: Any? = null,
        @Query("endAt", encoded = true) endAt: Any? = null,
        @Query("equalTo", encoded = true) equalTo: Any? = null
    ): Single<Any>

    @PUT("/{child}.json")
    fun putData(
        @Path("child", encoded = true) child: String = "",
        @Body value: Any
    ): Single<Any>
}