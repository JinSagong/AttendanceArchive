package com.jin.attendance_archive.util.db

import com.google.gson.JsonPrimitive
import io.reactivex.Flowable
import io.reactivex.Single

abstract class Query(child: String) {
    protected var children = child

    protected var orderBy: String? = null
    protected var limitToFirst: Int? = null
    protected var limitToLast: Int? = null
    protected var startAtString: String? = null
    protected var startAtInt: Int? = null
    protected var endAtString: String? = null
    protected var endAtInt: Int? = null
    protected var equalToString: String? = null
    protected var equalToInt: Int? = null
    protected var equalToBoolean: Boolean? = null

    fun child(child: String) = apply {
        if (children.isNotEmpty()) children += "/"
        children += child
    }

    fun orderByKey() = apply { orderBy = "\"\$key\"" }
    fun orderByValue() = apply { orderBy = "\"\$value\"" }
    fun orderByChild(child: String) = apply { orderBy = "\"$child\"" }
    fun limitToFirst(limit: Int) = apply { limitToFirst = limit }
    fun limitToLast(limit: Int) = apply { limitToLast = limit }
    fun startAt(value: String) = apply { startAtString = "\"$value\"" }
    fun startAt(value: Int) = apply { startAtInt = value }
    fun endAt(value: String) = apply { endAtString = "\"$value\"" }
    fun endAt(value: Int) = apply { endAtInt = value }
    fun equalTo(value: String) = apply { equalToString = "\"$value\"" }
    fun equalTo(value: Int) = apply { equalToInt = value }
    fun equalTo(value: Boolean) = apply { equalToBoolean = value }

    abstract fun getValue(): Single<Any>
    abstract fun observeValue(): Flowable<HashMap<String, JsonPrimitive>>
    abstract fun setValue(value: Any): Single<Any>
}