package com.jin.attendance_archive.util.file

import com.jin.attendance_archive.model.data.DataAttendance
import com.jin.attendance_archive.model.data.DataOrganization
import com.jin.attendance_archive.model.util.AttendanceTypeUtil
import com.jin.attendance_archive.model.util.OrganizationUtil
import com.jin.attendance_archive.model.util.PeopleUtil
import com.jin.attendance_archive.model.util.UserUtil
import org.apache.poi.ss.util.WorkbookUtil
import org.apache.poi.xssf.usermodel.XSSFSheet
import java.io.File

class WeeklyFileGenerator(title: String) : FileGenerator(title) {
    private val ws: XSSFSheet by lazy {
        wb.createSheet(WorkbookUtil.createSafeSheetName(title)).apply { isDisplayGridlines = false }
    }

    // Map<attendanceTypeId, Map<peopleId, Pair<checked, reason>>
    private var attendanceList = emptyMap<String, Map<String, Pair<Boolean, String>>>()
    private val attendanceTypeList = listOf(
        "id000000",
        "id000001",
        "id000002",
        "id000003",
        if (UserUtil.isGumi()) "id000005" else "id000004"
    )

    fun setData(list: List<DataAttendance>) {
        attendanceList = list
            .groupBy { item -> item.attendanceType }
            .mapValues { item ->
                item.value
                    .flatMap { item2 -> item2.items.values }
                    .groupBy { item2 -> item2.id }
                    .mapValues { item2 ->
                        val checked =
                            item2.value.firstOrNull { item3 -> item3.checked == 1 }?.checked
                        if (checked != null) Pair(true, "") else {
                            val reason =
                                item2.value.firstOrNull { item3 -> item3.reason.isNotEmpty() }?.reason.orEmpty()
                            Pair(false, reason)
                        }
                    }
            }
    }

    fun createFile(): File? {
        /** Title */
        setCell(ws.createRow(0), 0, title, styleTitle)
        setMerge(ws, 0, 2, 0, 12)

        /** Column */
        ws.createRow(3).run {
            setCell(this, 0, "지역", styleCategoryBold)
            setCell(this, 1, null, styleCategoryBold)
            setMerge(ws, 3, 3, 0, 1)
            setCell(this, 2, "이름", styleCategoryBold)
            attendanceTypeList.forEachIndexed { idx, attendanceType ->
                setCell(
                    this,
                    3 + idx * 2,
                    AttendanceTypeUtil.mapAttendanceType[attendanceType]?.name.orEmpty(),
                    styleCategoryBold
                )
                setCell(this, 4 + idx * 2, null, styleCategoryBold)
                setMerge(ws, 3, 3, 3 + idx * 2, 4 + idx * 2)
            }
        }
        ws.setColumnBreak(12)
        ws.createFreezePane(0, 4)

        /** Attendance */
        var rowIdx = 4
        var category = ""
        var countTemp = 0
        var countTempList = intArrayOf(0, 0, 0, 0, 0)
        var countGe = 0
        val countGeList = intArrayOf(0, 0, 0, 0, 0)
        var countGr = 0
        val countGrList = intArrayOf(0, 0, 0, 0, 0)
        var countBc = 0
        val countBcList = intArrayOf(0, 0, 0, 0, 0)
        OrganizationUtil.mapOrganization.values
            .filter { if (UserUtil.isGumi()) it.region == 1 else it.region == 2 }
            .sortedBy { it.id }
            .sortedBy { it.orgId }
            .forEach { org ->
                if (((UserUtil.isGumi() && org.category1 != "일반남여전도회") || (!UserUtil.isGumi() && org.category1 != "서울" && org.category1 != "분당")) && org.category2 != category) {
                    if (countTemp != 0) addTotalLine(rowIdx++, countTemp, countTempList)
                    addCategoryLine(rowIdx++, org)
                    countTemp = 0
                    countTempList = intArrayOf(0, 0, 0, 0, 0)
                }
                val count = addPeopleLine(rowIdx, org)
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
        if (countTemp != 0) addTotalLine(rowIdx++, countTemp, countTempList)
        if (countGe != 0) {
            rowIdx++
            addTotalLine(rowIdx++, countGe, countGeList, "일반 총계")
        }
        if (countGr != 0) {
            rowIdx++
            addTotalLine(rowIdx++, countGr, countGrList, "기관 총계")
        }
        if (countBc != 0) {
            rowIdx++
            addTotalLine(rowIdx++, countBc, countBcList, "지교회 총계")
        }
        if (countGe + countGr + countBc != 0) {
            rowIdx++
            addTotalLine(
                rowIdx,
                countGe + countGr + countBc,
                IntArray(5) { countGeList[it] + countGrList[it] + countBcList[it] },
                "전체 총계"
            )
        }

        /** setWidth */
        ws.setColumnWidth(0, 1500)
        ws.setColumnWidth(1, 2000)
        ws.setColumnWidth(2, 3000)
        for (idx in 3..12) ws.setColumnWidth(idx, 4000 - idx % 2 * 2700)

        return generateFile()
    }

    private fun addPeopleLine(rowIdx: Int, org: DataOrganization): Pair<Int, IntArray> {
        val peopleList = PeopleUtil.mapPeopleByFirstOrg[org.id]?.sortedBy { item -> item.name }
        val checkedList = intArrayOf(0, 0, 0, 0, 0)
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
                attendanceTypeList.forEachIndexed { idx2, attendanceType ->
                    val checked = attendanceList[attendanceType]?.get(people.id)?.first == true
                    val reason = attendanceList[attendanceType]?.get(people.id)?.second.orEmpty()
                    if (checked) checkedList[idx2]++
                    setCell(this, 3 + idx2 * 2, if (checked) "○" else null, styleTL)
                    setCell(this, 4 + idx2 * 2, reason, styleTR)
                }
            }
        }
        if (!peopleList.isNullOrEmpty())
            setMerge(ws, rowIdx, rowIdx + peopleList.size - 1, 0, 1)
        return Pair(peopleList?.size ?: 0, checkedList)
    }

    private fun addTotalLine(rowIdx: Int, count: Int, countList: IntArray, title: String? = null) {
        ws.createRow(rowIdx).run {
            setCell(this, 0, title ?: "소계", styleCategoryBold)
            setCell(this, 1, null, styleCategoryBold)
            setMerge(ws, rowIdx, rowIdx, 0, 1)
            setCell(this, 2, count.toString(), styleCategoryNormal)
            countList.forEachIndexed { idx, c ->
                setCell(this, 3 + idx * 2, "$c (${c * 100 / count}%)", styleCategoryNormal)
                setCell(this, 4 + idx * 2, null, styleCategoryNormal)
                setMerge(ws, rowIdx, rowIdx, 3 + idx * 2, 4 + idx * 2)
            }
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
            for (idx in 1..12) setCell(this, idx, null, styleCategoryBold)
            setMerge(ws, rowIdx, rowIdx, 0, 12)
        }
    }
}