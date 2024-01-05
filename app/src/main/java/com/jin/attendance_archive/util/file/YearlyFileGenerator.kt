package com.jin.attendance_archive.util.file

import com.jin.attendance_archive.model.data.DataAttendance
import com.jin.attendance_archive.model.data.DataOrganization
import com.jin.attendance_archive.model.util.OrganizationUtil
import com.jin.attendance_archive.model.util.PeopleUtil
import com.jin.attendance_archive.model.util.UserUtil
import com.jin.attendance_archive.util.DateTimeUtil
import org.apache.poi.ss.util.WorkbookUtil
import org.apache.poi.xssf.usermodel.XSSFSheet
import java.io.File

class YearlyFileGenerator(title: String) : FileGenerator(title) {
    private val ws: XSSFSheet by lazy {
        wb.createSheet(WorkbookUtil.createSafeSheetName(title)).apply { isDisplayGridlines = false }
    }

    // Map<peopleId, Pair<Map<week, checked>, reason>
    private var attendanceList = emptyMap<String, Pair<Map<Int, Boolean>, String>>()
    private var dateList = emptyArray<Pair<String, Int>>()
    private var lastColumnIdx = dateList.size + 3

    fun setData(isSunResult: Boolean, list: List<DataAttendance>, startWeek: Int, endWeek: Int, day: Int) {
        dateList = DateTimeUtil.getDateArrayOfWeek(startWeek, endWeek, day)
        lastColumnIdx = dateList.size + 3
        val listAdjusted = if (isSunResult) {
            val attendanceTypeList = arrayListOf("id000000", "id000001", "id000002")
            list.filter { item -> item.attendanceType in attendanceTypeList }
        } else list
        attendanceList = listAdjusted
            .flatMap { item -> item.items.values.map { item2 -> item.week to item2 } }
            .groupBy { item -> item.second.id }
            .mapValues { item ->
                val list2 = item.value
                    .sortedBy { item2 -> item2.first }
                    .groupBy { item2 -> item2.first }
                    .mapValues { item2 ->
                        val checked =
                            item2.value.firstOrNull { item3 -> item3.second.checked == 1 }?.second?.checked
                        if (checked != null) Pair(true, "") else {
                            val reason =
                                item2.value.firstOrNull { item3 -> item3.second.reason.isNotEmpty() }?.second?.reason.orEmpty()
                            Pair(false, reason)
                        }
                    }
                Pair(
                    list2.mapValues { item2 -> item2.value.first },
                    dateList.lastOrNull()?.second?.let { item2 -> list2[item2]?.second }.orEmpty()
                )
            }
    }

    fun createFile(): File? {
        /** Title */
        setCell(ws.createRow(0), 0, title, styleTitle)
        setMerge(ws, 0, 2, 0, lastColumnIdx)

        /** Column */
        val dateLine = arrayListOf<Int>()
        ws.createRow(3).run {
            setCell(this, 0, "지역", styleCategoryBold)
            setCell(this, 1, null, styleCategoryBold)
            setMerge(ws, 3, 3, 0, 1)
            setCell(this, 2, "이름", styleCategoryBold)
            var month = ""
            dateList.forEachIndexed { idx, pair ->
                val mMonth = pair.first.split("/").first()
                setCell(this, idx + 3, pair.first, if (month != mMonth) styleTBL else styleTB)
                if (month != mMonth) dateLine.add(idx)
                month = mMonth
            }
            setCell(this, lastColumnIdx, "결석사유", styleCategoryNormal)
        }
        ws.setColumnBreak(lastColumnIdx)
        ws.createFreezePane(0, 4)

        /** Attendance */
        var rowIdx = 4
        var category = ""
        var countTemp = 0
        var countTempList = IntArray(dateList.size) { 0 }
        var countGe = 0
        val countGeList = IntArray(dateList.size) { 0 }
        var countGr = 0
        val countGrList = IntArray(dateList.size) { 0 }
        var countBc = 0
        val countBcList = IntArray(dateList.size) { 0 }
        OrganizationUtil.mapOrganization.values
            .filter { if (UserUtil.isGumi()) it.region == 1 else it.region == 2 }
            .sortedBy { it.id }
            .sortedBy { it.orgId }
            .forEach { org ->
                if (((UserUtil.isGumi() && org.category1 != "일반남여전도회") || (!UserUtil.isGumi() && org.category1 != "서울" && org.category1 != "분당")) && org.category2 != category) {
                    if (countTemp != 0) addTotalLine(rowIdx++, countTemp, countTempList, dateLine)
                    addCategoryLine(rowIdx++, org)
                    countTemp = 0
                    countTempList = IntArray(dateList.size) { 0 }
                }
                val count = addPeopleLine(rowIdx, org, dateLine)
                rowIdx += count.first
                countTemp += count.first
                count.second.forEachIndexed { idx, c -> countTempList[idx] += c }
                category = org.category2
                when (org.category1) {
                    "일반남여전도회", "서울", "분당" -> {
                        countGe += count.first
                        count.second.forEachIndexed { idx, c -> countGeList[idx] += c }
                    }

                    "기관" -> {
                        countGr += count.first
                        count.second.forEachIndexed { idx, c -> countGrList[idx] += c }
                    }

                    "지교회" -> {
                        countBc += count.first
                        count.second.forEachIndexed { idx, c -> countBcList[idx] += c }
                    }
                }
            }
        if (countTemp != 0) addTotalLine(rowIdx++, countTemp, countTempList, dateLine)

        if (countGe != 0) {
            rowIdx++
            addTotalLine(rowIdx++, countGe, countGeList, dateLine, "일반 총계")
        }
        if (countGr != 0) {
            rowIdx++
            addTotalLine(rowIdx++, countGr, countGrList, dateLine, "기관 총계")
        }
        if (countBc != 0) {
            rowIdx++
            addTotalLine(rowIdx++, countBc, countBcList, dateLine, "지교회 총계")
        }
        if (countGe + countGr + countBc != 0) {
            rowIdx++
            addTotalLine(
                rowIdx,
                countGe + countGr + countBc,
                IntArray(dateList.size) { countGeList[it] + countGrList[it] + countBcList[it] },
                dateLine,
                "전체 총계"
            )
        }

        /** setWidth */
        ws.setColumnWidth(0, 1500)
        ws.setColumnWidth(1, 2000)
        ws.setColumnWidth(2, 3000)
        for (idx in 3 until lastColumnIdx) ws.setColumnWidth(idx, 1300)
        ws.setColumnWidth(lastColumnIdx, 4000)

        return generateFile()
    }

    private fun addPeopleLine(
        rowIdx: Int, org: DataOrganization, dateLine: List<Int>
    ): Pair<Int, IntArray> {
        val peopleList = PeopleUtil.mapPeopleByFirstOrg[org.id]?.sortedBy { item -> item.name }
        val checkedList = IntArray(dateList.size) { 0 }
        peopleList?.forEachIndexed { idx, people ->
            ws.createRow(rowIdx + idx).run {
                setCell(
                    this,
                    0,
                    if (idx == 0) org.category3.ifEmpty { org.category2 } else null,
                    styleCategoryBold
                )
                setCell(this, 1, null, styleCategoryBold)
                setCell(this, 2, people.name, styleTLR)
                dateList.forEachIndexed { idx2, pair ->
                    val checked = attendanceList[people.id]?.first?.get(pair.second) == true
                    if (checked) checkedList[idx2]++
                    val mStyle = if (idx2 in dateLine) styleTL else styleT
                    setCell(this, 3 + idx2, if (checked) "○" else null, mStyle)
                }
                val reason = attendanceList[people.id]?.second.orEmpty()
                setCell(this, lastColumnIdx, reason, styleTLR)
            }
        }
        if (!peopleList.isNullOrEmpty())
            setMerge(ws, rowIdx, rowIdx + peopleList.size - 1, 0, 1)
        return Pair(peopleList?.size ?: 0, checkedList)
    }

    private fun addTotalLine(
        rowIdx: Int, count: Int, countList: IntArray, dateLine: List<Int>, title: String? = null
    ) {
        ws.createRow(rowIdx).run {
            setCell(this, 0, title ?: "소계", styleCategoryBold)
            setCell(this, 1, null, styleCategoryBold)
            setMerge(ws, rowIdx, rowIdx, 0, 1)
            setCell(this, 2, count.toString(), styleCategoryNormal)
            countList.forEachIndexed { idx, c ->
                val mStyle = if (idx in dateLine) styleTBL else styleTB
                setCell(this, 3 + idx, "$c", mStyle)
            }
            setCell(this, lastColumnIdx, null, styleCategoryNormal)
        }
    }

    private fun addCategoryLine(rowIdx: Int, org: DataOrganization) {
        ws.createRow(rowIdx).run {
            setCell(
                this,
                0,
                org.category2.toList().joinToString(" ") { it.toString() },
                styleCategoryBold
            )
            for (idx in 1..lastColumnIdx) setCell(this, idx, null, styleCategoryBold)
            setMerge(ws, rowIdx, rowIdx, 0, lastColumnIdx)
        }
    }
}