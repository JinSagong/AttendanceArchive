package com.jin.attendance_archive.util

import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow

class CollectableFlow<T>(initValue: T, replay:Int = 1) {
    private val mutableFlow = MutableSharedFlow<T>(replay = replay)

    var value: T = initValue
        set(value) {
            mutableFlow.tryEmit(value)
            field = value
        }

    suspend fun collect(collector: FlowCollector<T>) {
        mutableFlow.collect(collector)
    }
}