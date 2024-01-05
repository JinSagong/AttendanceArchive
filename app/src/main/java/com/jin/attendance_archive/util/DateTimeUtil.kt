package com.jin.attendance_archive.util

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat")
object DateTimeUtil {
    private val dateFormat by lazy { SimpleDateFormat("yyMMdd") }
    private val yearPositionFormat by lazy { SimpleDateFormat("yyyy") }
    private val monthPositionFormat by lazy { SimpleDateFormat("MM") }
    private val datePositionFormat by lazy { SimpleDateFormat("dd") }
    private val logTimeFormat by lazy { SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS") }

    private val sundayInstance
        get() = Calendar.getInstance().apply { set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY) }
    private val saturdayInstance
        get() = Calendar.getInstance().apply { set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY) }

    fun getWeek(
        pYear: Int = getYearPosition(),
        pMonth: Int = getMonthPosition(),
        pWeek: Int = getWeekPosition()
    ) = "${pYear + 2020}년 " +
            "${(pMonth + 1).toString().let { if (it.length == 1) "0$it" else it }}월 " +
            "${pWeek + 1}주차"

    fun getWeekValue(
        pYear: Int = getYearPosition(),
        pMonth: Int = getMonthPosition(),
        pWeek: Int = getWeekPosition()
    ) = pYear * 1000 + pMonth * 10 + pWeek

    private fun getWeekValue(calendar: Calendar): Int {
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        val pYear = getYearPosition(calendar)
        val pMonth = getMonthPosition(calendar)
        val pWeek = getWeekPosition(calendar)
        return pYear * 1000 + pMonth * 10 + pWeek
    }

    fun getStartWeekValue(pYear: Int, pMonth: Int = 0) = pYear * 1000 + pMonth * 10

    fun getEndWeekValue(pYear: Int, pMonth: Int = 11): Int {
        val pThisYear = getYearPosition()
        val pThisMonth = getMonthPosition()
        val pThisWeek = getWeekPosition()
        val ppYear = if (pYear > pThisYear && pThisMonth == 11) pThisYear else pYear
        val ppMonth = if (pYear >= pThisYear && pMonth > pThisMonth) pThisMonth else pMonth
        val ppWeek = if (pYear >= pThisYear && ppMonth == pThisMonth) pThisWeek
        else getMaxWeekCount(pYear, ppMonth)
        return ppYear * 1000 + ppMonth * 10 + ppWeek
    }

    fun getStartWeekValueForYearly(pYear: Int, day: Int): Int {
        val instance = Calendar.getInstance().apply {
            set(Calendar.YEAR, pYear + 2020)
            set(Calendar.MONTH, 0)
            set(Calendar.DATE, 1)
        }
        if (instance.get(Calendar.DAY_OF_WEEK) > day) instance.add(Calendar.DATE, 7)
        return getWeekValue(instance)
    }

    fun getEndWeekValueForYearly(pYear: Int, day: Int): Int {
        val pThisYear = getYearPosition()
        val pThisMonth = getMonthPosition()
        val pThisWeek = getWeekPosition()
        val ppYear = if (pYear > pThisYear && pThisMonth == 11) pThisYear else pYear
        val ppMonth = if (pYear >= pThisYear && 11 > pThisMonth) pThisMonth else 11
        var ppWeek = if (pYear >= pThisYear && ppMonth == pThisMonth) pThisWeek
        else getMaxWeekCount(pYear, ppMonth)

        val instance = Calendar.getInstance().apply {
            set(Calendar.YEAR, ppYear + 2020)
            set(Calendar.MONTH, ppMonth)
            set(Calendar.WEEK_OF_MONTH, 3)
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        }
        val midDate = datePositionFormat.format(instance.time).toInt()
        val weekIdx = (midDate - 1) / 7 + 1
        val date = midDate + (ppWeek + 1 - weekIdx) * 7
        instance.set(Calendar.DATE, date)
        instance.add(Calendar.DATE, day - Calendar.SUNDAY)
        if (instance.get(Calendar.YEAR) > pYear + 2020) ppWeek--

        return ppYear * 1000 + ppMonth * 10 + ppWeek
    }

    fun splitWeekValueRange(startWeek: Int, endWeek: Int): Array<Pair<Int, Int>> {
        val dateArray = getDateArrayOfWeek(startWeek, endWeek)
        return if (dateArray.size >= 9) {
            val idx1 = dateArray.size / 3
            val idx2 = dateArray.size * 2 / 3
            arrayOf(
                Pair(startWeek, dateArray[idx1 - 1].second),
                Pair(dateArray[idx1].second, dateArray[idx2 - 1].second),
                Pair(dateArray[idx2].second, endWeek)
            )
        } else arrayOf(Pair(startWeek, endWeek))
    }

    fun getDateArrayOfWeek(
        startWeek: Int,
        endWeek: Int,
        day: Int = Calendar.SUNDAY
    ): Array<Pair<String, Int>> {
        if (startWeek > endWeek) return emptyArray()

        val startMonthAndDate = getInstanceOfWeek(startWeek, day)
        val endMonthAndDate = getInstanceOfWeek(endWeek, day)
        val endResultMonth =
            (endMonthAndDate.get(Calendar.MONTH) + 1).let { if (it < 10) "0${it}" else "$it" }
        val endResultDate =
            endMonthAndDate.get(Calendar.DATE).let { if (it < 10) "0${it}" else "$it" }
        val endResult = "$endResultMonth/$endResultDate"
        val result = arrayListOf<Pair<String, Int>>()
        var sResult = ""
        while (sResult != endResult && result.size <= 52) {
            val sMonth =
                (startMonthAndDate.get(Calendar.MONTH) + 1).let { if (it < 10) "0${it}" else "$it" }
            val sDate =
                startMonthAndDate.get(Calendar.DATE).let { if (it < 10) "0${it}" else "$it" }
            sResult = "$sMonth/$sDate"
            result.add(Pair(sResult, getWeekValue(startMonthAndDate)))
            startMonthAndDate.add(Calendar.DATE, 7)
        }
        return result.toTypedArray()
    }

    private fun getInstanceOfWeek(week: Int, day: Int): Calendar {
        val mYear = week / 1000
        val mMonth = (week % 1000) / 10
        val mWeek = week % 10

        val newInstance = Calendar.getInstance().apply {
            set(Calendar.YEAR, mYear + 2020)
            set(Calendar.MONTH, mMonth)
            set(Calendar.WEEK_OF_MONTH, 3)
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        }
        val midDate = datePositionFormat.format(newInstance.time).toInt()
        val weekIdx = (midDate - 1) / 7 + 1
        val date = midDate + (mWeek + 1 - weekIdx) * 7
        newInstance.set(Calendar.DATE, date)
        newInstance.add(Calendar.DATE, day - Calendar.SUNDAY)
        return newInstance
    }

    fun getPeriod(): String {
        val sun = sundayInstance.time
        val sat = saturdayInstance.time
        return dateFormat.format(sun) + "-" + dateFormat.format(sat)
    }

    fun getYearPosition(calendar: Calendar = sundayInstance) =
        yearPositionFormat.format(calendar.time).toInt() - 2020

    fun getMonthPosition(calendar: Calendar = sundayInstance) =
        monthPositionFormat.format(calendar.time).toInt() - 1

    fun getWeekPosition(calendar: Calendar = sundayInstance) =
        (datePositionFormat.format(calendar.time).toInt() - 1) / 7

    fun getYearArray() = Array(getYearPosition(Calendar.getInstance()) + 1) { "${it + 2020}년" }
    fun getMonthArray() = Array(12) { "${it + 1}월".let { m -> if (m.length == 2) "0$m" else m } }
    fun getWeekArray() = Array(5) { "${it + 1}주차" }

    fun getMaxWeekCount(pYear: Int, pMonth: Int): Int {
        val newInstance = Calendar.getInstance().apply {
            set(Calendar.YEAR, pYear + 2020)
            set(Calendar.MONTH, pMonth)
            set(Calendar.WEEK_OF_MONTH, 3)
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        }
        val date = datePositionFormat.format(newInstance.time).toInt()
        val maxDate = newInstance.getActualMaximum(Calendar.DAY_OF_MONTH)
        return (date - 1) / 7 + (maxDate - date) / 7 + 1
    }

    fun getWeeklyFileTitle(pYear: Int, pMonth: Int, pWeek: Int, title: String = "출석부"): String {
        val sYear = "${pYear + 2020}년"
        val sMonth = (pMonth + 1).let { if (it < 10) "0${it}월" else "${it}월" }
        val sWeek = "${pWeek + 1}주차"
        return "$sYear $sMonth $sWeek $title"
    }

    fun getMonthlyFileTitle(pYear: Int, pMonth: Int, title: String = "출석부"): String {
        val sYear = "${pYear + 2020}년"
        val sMonth = (pMonth + 1).let { if (it < 10) "0${it}월" else "${it}월" }
        return "$sYear $sMonth $title"
    }

    fun getYearlyFileTitle(pYear: Int, title: String = "출석부"): String {
        val sYear = "${pYear + 2020}년"
        return "$sYear $title"
    }

    fun getLogTime(millis: Long = System.currentTimeMillis()): String =
        logTimeFormat.format(Date(millis))
}