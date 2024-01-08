package com.jin.attendance_archive.util.file

import com.jin.attendance_archive.model.data.DataAttendance
import com.jin.attendance_archive.model.data.DataAttendanceItem
import com.jin.attendance_archive.model.data.DataFruit
import com.jin.attendance_archive.model.data.DataPeople
import com.jin.attendance_archive.model.util.DutyUtil
import com.jin.attendance_archive.model.util.OrganizationUtil
import com.jin.attendance_archive.model.util.PeopleUtil
import com.jin.attendance_archive.model.util.UserUtil
import com.jin.attendance_archive.util.DateTimeUtil
import com.jin.attendance_archive.util.Debug
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.VerticalAlignment
import org.apache.poi.ss.util.WorkbookUtil
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFSheet
import java.io.File
import kotlin.math.ceil
import kotlin.math.max

class FruitFileGenerator(title: String) : FileGenerator(title) {
    private val ws1: XSSFSheet by lazy {
        wb.createSheet(WorkbookUtil.createSafeSheetName("출석")).apply { isDisplayGridlines = false }
    }
    private val ws2: XSSFSheet by lazy {
        wb.createSheet(WorkbookUtil.createSafeSheetName("열매")).apply { isDisplayGridlines = false }
    }

    // Map<week, Map<peopleId, Pair<DataAttendanceItem, category3>>
    private var attendanceList = emptyMap<Int, Map<String, Pair<DataAttendanceItem, String>>>()
    private var dateList = emptyArray<Pair<String, Int>>()
    private var withPeriod = false

    private var peopleNotBc = emptyMap<String, List<DataPeople>>()
    private var peopleBc = emptyMap<String, List<DataPeople>>()
    private var peopleOtherNotBc = emptyList<DataPeople>()
    private var peopleOtherBc = emptyList<DataPeople>()

    private val dutyListWithoutOther = DutyUtil.mapDuty.values
        .filter { item -> item.id != "id000011" }
        .sortedBy { item -> item.id }
    private val countByDuty = HashMap<String, HashMap<Int, Int>>()
    private val countByIsBc = HashMap<Boolean, HashMap<Int, Int>>()

    private val rowPerPage = 64
    private val breakRows = arrayListOf<Int>()

    private var fruitAttendanceList = emptyMap<String, List<Pair<DataPeople, Boolean>>>()
    private var fruitAttendanceCountList = emptyMap<String, Int>()
    private var fruitList0 = emptyMap<String, List<DataFruit>>()
    private var fruitList1 = emptyMap<String, List<DataFruit>>()
    private var fruitAttendanceRows = 0
    private var fruitResultRows = 0

    fun setData(list: List<DataAttendance>, startWeek: Int, endWeek: Int, withPeriod: Boolean) {
        this.withPeriod = withPeriod
        dateList = DateTimeUtil.getDateArrayOfWeek(startWeek, endWeek)
        attendanceList = list
            .groupBy(
                { item -> item.week },
                { item ->
                    item.items.values.map { item2 ->
                        Pair(
                            item2,
                            OrganizationUtil.mapOrganizationByOrgId[item.org]?.firstOrNull()?.category3.orEmpty()
                        )
                    }
                })
            .mapValues { item ->
                item.value.flatten()
                    .filter { item2 -> item2.second.isNotEmpty() }
                    .associateBy { item2 -> item2.first.id }
            }

        peopleNotBc = PeopleUtil.mapPeopleByFirstOrg
            .filter { item ->
                OrganizationUtil.mapOrganization[item.key]?.category1?.let { it != "지교회" } ?: true
            }
            .values
            .asSequence()
            .flatten()
            .filter { item -> item.duty.isNotEmpty() && item.duty != "id000011" }
            .sortedBy { item -> item.name }
            .sortedBy { item -> item.duty }
            .groupBy { item -> DutyUtil.mapDuty[item.duty]?.name.orEmpty() }
            .filterKeys { key -> key.isNotEmpty() }
        peopleBc = PeopleUtil.mapPeopleByFirstOrg
            .mapNotNull { item ->
                val org = OrganizationUtil.mapOrganization[item.key]
                if (org?.category1 == "지교회") Pair(Pair(org.orgId, org.category2), item.value)
                else null
            }
            .sortedBy { item -> item.first.first }
            .groupBy({ item -> item.first.second }, { item -> item.second })
            .mapValues { item ->
                item.value.flatten()
                    .filter { item2 -> item2.duty.isNotEmpty() && item2.duty != "id000011" }
                    .sortedBy { item2 -> item2.name }
            }

        if (!withPeriod) {
            val region = UserUtil.getRegion()
            val peopleOther = attendanceList[startWeek]
                ?.filter { item ->
                    item.value.first.checked == 1 && (PeopleUtil.mapPeople[item.key]?.duty == "id000011"
                            || (PeopleUtil.mapPeople[item.key]?.let { PeopleUtil.getRegion(it) }
                        ?: region) != region)
                }
                ?.values
                .orEmpty()
            peopleOtherNotBc = peopleOther
                .mapNotNull { item ->
                    val people = PeopleUtil.mapPeople[item.first.id] ?: return@mapNotNull null
                    val peopleOrg = people.org.firstOrNull() ?: return@mapNotNull null
                    val org = OrganizationUtil.mapOrganization[peopleOrg] ?: return@mapNotNull null
                    if (org.category1 != "지교회" || PeopleUtil.getRegion(people) != region) people else null
                }
                .sortedBy { item -> item.name }
            peopleOtherBc = peopleOther
                .mapNotNull { item ->
                    val people = PeopleUtil.mapPeople[item.first.id] ?: return@mapNotNull null
                    val peopleOrg = people.org.firstOrNull() ?: return@mapNotNull null
                    val org = OrganizationUtil.mapOrganization[peopleOrg] ?: return@mapNotNull null
                    if (org.category1 == "지교회" && PeopleUtil.getRegion(people) == region) people else null
                }
                .sortedBy { item -> item.name }
        }

        var countCategory = peopleNotBc.size + peopleBc.size
        if (peopleOtherNotBc.isNotEmpty()) countCategory++
        if (peopleOtherBc.isNotEmpty()) countCategory++
        val countPeople =
            peopleNotBc.values.sumOf { it.size } + peopleBc.values.sumOf { it.size } + (peopleOtherNotBc.size + 1) / 2 + (peopleOtherBc.size + 1) / 2
        var rowNeeded = ceil((countCategory + countPeople) / 4f).toInt()

        val rowForSummary = if (!withPeriod) dutyListWithoutOther.size + 6 else 0
        breakRows.add(if (rowNeeded <= rowPerPage - 4 - rowForSummary) rowNeeded else (rowPerPage - 4))
        rowNeeded -= (rowPerPage - 4)
        while (rowNeeded > 0) {
            breakRows.add(if (rowNeeded <= rowPerPage - 1 - rowForSummary) rowNeeded else (rowPerPage - 1))
            rowNeeded -= (rowPerPage - 1)
        }

        if (!withPeriod) {
            val fruitAttendanceNotBcList = list
                .filter { item -> OrganizationUtil.mapOrganizationByOrgId[item.org]?.firstOrNull()?.category1 == "현장" }
                .associate { item ->
                    OrganizationUtil.mapOrganizationByOrgId[item.org]?.firstOrNull()?.id.orEmpty() to item.items.values.mapNotNull { item2 ->
                        if (item2.checked == 1) PeopleUtil.mapPeople[item2.id]
                            ?.let { item3 -> Pair(item3, false) } else null
                    }.sortedBy { item2 -> item2.first.name }
                }
                .filter { item -> item.key.isNotEmpty() && item.value.isNotEmpty() }
            val fruitAttendanceBcList = list
                .filter { item -> OrganizationUtil.mapOrganizationByOrgId[item.org]?.firstOrNull()?.category1 == "지교회현장" }
                .flatMap { item -> item.items.values }
                .mapNotNull { item ->
                    if (item.checked == 1) PeopleUtil.mapPeople[item.id]
                        ?.let { item2 -> Pair(item2, true) } else null
                }
                .sortedBy { item2 -> item2.first.name }
            fruitAttendanceList = DutyUtil.mapDuty.values
                .sortedBy { item -> item.id }
                .associate { item ->
                    item.name to (fruitAttendanceNotBcList.values.flatten() + fruitAttendanceBcList)
                        .filter { item2 -> item2.first.duty == item.id }
                        .distinctBy { item2 -> item2.first.id }
                }
            fruitAttendanceCountList = OrganizationUtil.mapOrganization.values
                .sortedBy { item -> item.id }
                .filter { item -> (item.region == if (UserUtil.isGumi()) 3 else 4) && item.category1 == "현장" }
                .map { item ->
                    item.category3 to (fruitAttendanceNotBcList[item.id]?.size ?: 0)
                }
                .let { item -> if (UserUtil.isGumi()) item + ("지교회(본교회포함)" to fruitAttendanceBcList.size) else item }
                .toMap()
            fruitAttendanceRows =
                fruitAttendanceList.values.sumOf { item -> (item.size - 1).let { if (it < 0) 0 else it } / 10 + 1 } + 3
            val fruitList = list
                .associate { item -> item.org to item.fruits.values }
                .toSortedMap()
                .mapKeys { item -> OrganizationUtil.mapOrganizationByOrgId[item.key]?.firstOrNull()?.category3.orEmpty() }
                .filter { item -> item.key.isNotEmpty() && item.value.isNotEmpty() }

            fruitList0 = fruitList
                .mapValues { item -> item.value.filter { item2 -> item2.type == 0 } }
                .filter { item -> item.value.isNotEmpty() }
            fruitList1 = fruitList
                .mapValues { item -> item.value.filter { item2 -> item2.type == 1 } }
                .filter { item -> item.value.isNotEmpty() }
            fruitResultRows =
                fruitList0.values.sumOf { item -> (item.size - 1) / 4 + 1 } + fruitList1.values.sumOf { item -> (item.size - 1) / 4 + 1 } + 7
        }
    }

    private fun createSheet1() {
        /** Title */
        setCell(ws1.createRow(0), 0, title, styleTitle)
        setMerge(ws1, 0, 2, 0, 11)

        /** Attendance */
        var rowIdx = 3
        var itemIdx = 0
        var breakRowIdx = 0
        val getPosition = {
            var breakRow = breakRows[breakRowIdx]
            if (itemIdx >= breakRow * 4) {
                itemIdx = 0
                breakRowIdx++
                rowIdx += (breakRow + 1)
                breakRow = breakRows[breakRowIdx]
            }
            val row = rowIdx + itemIdx % breakRow
            val column = itemIdx / breakRow
            Pair(row, column)
        }
        val isTop = { itemIdx % breakRows[breakRowIdx] == 1 }
        val isBottom = { itemIdx % breakRows[breakRowIdx] == 0 }
        peopleNotBc.forEach { item ->
            val p = getPosition()
            itemIdx++
            addCategory(p.first, p.second, isBottom(), item.key)
            item.value.forEachIndexed { idx, people ->
                val p2 = getPosition()
                itemIdx++
                val checked = addPeopleLine(
                    p2.first,
                    p2.second,
                    idx,
                    isTop(),
                    isBottom() || idx == item.value.lastIndex,
                    people
                )
                countByDuty.getOrPut(people.duty) { HashMap() }.getOrPut(checked) { 0 }
                    .let { count -> countByDuty[people.duty]?.set(checked, count + 1) }
                countByIsBc.getOrPut(false) { HashMap() }.getOrPut(checked) { 0 }
                    .let { count -> countByIsBc[false]?.set(checked, count + 1) }
            }
        }
        if (peopleOtherNotBc.isNotEmpty()) {
            val p = getPosition()
            itemIdx++
            addCategory(p.first, p.second, isBottom(), "기타")
        }
        peopleOtherNotBc.forEachIndexed { idx, people ->
            val p = getPosition()
            if (idx % 2 == 1) itemIdx++
            addOtherPeopleLine(
                p.first,
                p.second,
                idx,
                isTop(),
                isBottom() || idx == peopleOtherNotBc.lastIndex,
                people
            )
        }
        if (peopleOtherNotBc.isNotEmpty() && peopleOtherNotBc.size % 2 == 1) {
            val p = getPosition()
            setCell(ws1.getRow(p.first), p.second * 3 + 2, null, styleBR)
            itemIdx++
        }
        peopleBc.forEach { item ->
            val p = getPosition()
            itemIdx++
            addCategory(p.first, p.second, isBottom(), item.key)
            item.value.forEachIndexed { idx, people ->
                val p2 = getPosition()
                itemIdx++
                val checked = addPeopleLine(
                    p2.first,
                    p2.second,
                    idx,
                    isTop(),
                    isBottom() || idx == item.value.lastIndex,
                    people
                )
                countByDuty.getOrPut(people.duty) { HashMap() }.getOrPut(checked) { 0 }
                    .let { count -> countByDuty[people.duty]?.set(checked, count + 1) }
                countByIsBc.getOrPut(true) { HashMap() }.getOrPut(checked) { 0 }
                    .let { count -> countByIsBc[true]?.set(checked, count + 1) }
            }
        }
        if (peopleOtherBc.isNotEmpty()) {
            val p = getPosition()
            itemIdx++
            addCategory(p.first, p.second, isBottom(), "지교회 기타")
        }
        peopleOtherBc.forEachIndexed { idx, people ->
            val p = getPosition()
            if (idx % 2 == 1) itemIdx++
            addOtherPeopleLine(
                p.first,
                p.second,
                idx,
                isTop(),
                isBottom() || idx == peopleOtherBc.lastIndex,
                people
            )
        }
        if (peopleOtherBc.isNotEmpty() && peopleOtherBc.size % 2 == 1) {
            val p = getPosition()
            setCell(ws1.getRow(p.first), p.second * 3 + 2, null, styleBR)
            itemIdx++
        }

        if (!withPeriod) {
            rowIdx += (breakRows[breakRowIdx] + 1)
            ws1.createRow(rowIdx).run {
                setCell(this, 0, "구분", styleBoldTBL)
                setCell(this, 1, "직분", styleBoldTB)
                setCell(this, 2, "참석", styleBoldTB)
                setCell(this, 3, null, styleBoldTB)
                setCell(this, 4, null, styleBoldTB)
                setCell(this, 5, "헌신", styleBoldTB)
                setCell(this, 6, null, styleBoldTB)
                setCell(this, 7, null, styleBoldTB)
                setCell(this, 8, "불참", styleBoldTB)
                setCell(this, 9, "참석 + 헌신", styleBoldTB)
                setCell(this, 10, null, styleBoldTB)
                setCell(this, 11, null, styleBoldTBR)
                setMerge(ws1, rowIdx, rowIdx, 2, 4)
                setMerge(ws1, rowIdx, rowIdx, 5, 7)
                setMerge(ws1, rowIdx, rowIdx, 9, 11)
            }
            dutyListWithoutOther.forEachIndexed { idx, duty ->
                val count = countByDuty[duty.id].orEmpty()
                val count0 = count[0] ?: 0
                val count1 = count[1] ?: 0
                val count2 = count[2] ?: 0
                val totalCount = count0 + count1 + count2
                ws1.createRow(++rowIdx).run {
                    setCell(this, 0, (idx + 1).toString(), styleL)
                    setCell(this, 1, duty.name, styleX)
                    setCell(
                        this,
                        2,
                        "$count1 / $totalCount (${if (totalCount == 0) 0 else (count1 * 100 / totalCount)}%)",
                        styleX
                    )
                    setCell(this, 3, null, styleX)
                    setCell(this, 4, null, styleX)
                    setCell(
                        this,
                        5,
                        "$count2 / $totalCount (${if (totalCount == 0) 0 else (count2 * 100 / totalCount)}%)",
                        styleX
                    )
                    setCell(this, 6, null, styleX)
                    setCell(this, 7, null, styleX)
                    setCell(this, 8, count0.toString(), styleX)
                    setCell(
                        this,
                        9,
                        "${count1 + count2} / $totalCount (${if (totalCount == 0) 0 else ((count1 + count2) * 100 / totalCount)}%)",
                        styleX
                    )
                    setCell(this, 10, null, styleX)
                    setCell(this, 11, null, styleR)
                    setMerge(ws1, rowIdx, rowIdx, 2, 4)
                    setMerge(ws1, rowIdx, rowIdx, 5, 7)
                    setMerge(ws1, rowIdx, rowIdx, 9, 11)
                }
            }
            var summaryIdx = dutyListWithoutOther.size
            val count = countByDuty.values
                .flatMap { item -> item.map { item2 -> item2.key to item2.value } }
                .groupBy({ item -> item.first }, { item -> item.second })
                .mapValues { item -> item.value.sum() }
            val count0 = count[0] ?: 0
            val count1 = count[1] ?: 0
            val count2 = count[2] ?: 0
            val totalCount = count0 + count1 + count2
            ws1.createRow(++rowIdx).run {
                setCell(this, 0, (++summaryIdx).toString(), styleFruitTotalL)
                setCell(this, 1, "총계", styleFruitTotalX)
                setCell(
                    this,
                    2,
                    "$count1 / $totalCount (${if (totalCount == 0) 0 else (count1 * 100 / totalCount)}%)",
                    styleFruitTotalX
                )
                setCell(this, 3, null, styleFruitTotalX)
                setCell(this, 4, null, styleFruitTotalX)
                setCell(
                    this,
                    5,
                    "$count2 / $totalCount (${if (totalCount == 0) 0 else (count2 * 100 / totalCount)}%)",
                    styleFruitTotalX
                )
                setCell(this, 6, null, styleFruitTotalX)
                setCell(this, 7, null, styleFruitTotalX)
                setCell(this, 8, count0.toString(), styleFruitTotalX)
                setCell(
                    this,
                    9,
                    "${count1 + count2} / $totalCount (${if (totalCount == 0) 0 else ((count1 + count2) * 100 / totalCount)}%)",
                    styleFruitTotalX
                )
                setCell(this, 10, null, styleFruitTotalX)
                setCell(this, 11, null, styleFruitTotalR)
                setMerge(ws1, rowIdx, rowIdx, 2, 4)
                setMerge(ws1, rowIdx, rowIdx, 5, 7)
                setMerge(ws1, rowIdx, rowIdx, 9, 11)
            }
            val countNotBc = countByIsBc[false]?.get(1) ?: 0
            val countBc = countByIsBc[true]?.get(1) ?: 0
            ws1.createRow(++rowIdx).run {
                setCell(this, 0, (++summaryIdx).toString(), styleL)
                setCell(this, 1, "전도 참석자", styleX)
                setCell(
                    this,
                    2,
                    "${countNotBc + countBc}명 (${if (UserUtil.isGumi()) "구미" else "서울/분당"}: ${countNotBc}명, 지교회: ${countBc}명)",
                    styleX
                )
                (3..10).forEach { idx -> setCell(this, idx, null, styleX) }
                setCell(this, 11, null, styleR)
                setMerge(ws1, rowIdx, rowIdx, 2, 11)
            }
            val countOtherNotBc = peopleOtherNotBc.size
            val countOtherBc = peopleOtherBc.size
            ws1.createRow(++rowIdx).run {
                setCell(this, 0, (++summaryIdx).toString(), styleL)
                setCell(this, 1, "기타", styleX)
                setCell(
                    this,
                    2,
                    "${countOtherNotBc + countOtherBc}명 (${if (UserUtil.isGumi()) "구미" else "서울/분당"}: ${countOtherNotBc}명, 지교회: ${countOtherBc}명)",
                    styleX
                )
                (3..10).forEach { idx -> setCell(this, idx, null, styleX) }
                setCell(this, 11, null, styleR)
                setMerge(ws1, rowIdx, rowIdx, 2, 11)
            }
            ws1.createRow(++rowIdx).run {
                setCell(
                    this,
                    0,
                    "전체 전도 참석자: ${countNotBc + countBc + countOtherNotBc + countOtherBc}명",
                    styleCategoryBold
                )
                (1..11).forEach { idx -> setCell(this, idx, null, styleCategoryBold) }
                setMerge(ws1, rowIdx, rowIdx, 0, 11)
            }
            setCell(
                ws1.createRow(++rowIdx),
                0,
                "* 기타: 유치, 유년, 초등, 중등, 고등 (전도 참석자 수만 기록)",
                styleNull
            )
            setMerge(ws1, rowIdx, rowIdx, 0, 11)
        } else {
            rowIdx += breakRows[breakRowIdx]
        }

        /** setWidth */
        (0..3).forEach { idx ->
            ws1.setColumnWidth(0 + idx * 3, 1000)
            ws1.setColumnWidth(1 + idx * 3, 4000)
            ws1.setColumnWidth(2 + idx * 3, 4000)
        }
        ws1.autobreaks = false
        (1..(rowIdx / rowPerPage + 1)).forEach { idx -> ws1.setRowBreak(rowPerPage * idx - 1) }
        ws1.setColumnBreak(11)

    }

    private fun createSheet2() {
        /** Title */
        setCell(ws2.createRow(0), 0, title, styleTitle)
        setMerge(ws2, 0, 2, 0, 29)
        ws2.createRow(4).run {
            if (UserUtil.isGumi()) {
                setCell(this, 15, null, styleFruitBcLegend)
                setCell(this, 16, "지교회", styleFruitLegend)
            }
            setCell(this, 19, null, styleFruitRemeetLegend)
            setCell(this, 20, "영접+재만남", styleFruitLegend)
            setCell(this, 23, null, styleX)
            setCell(this, 24, "영접", styleFruitLegend)
        }

        /** Attendance */
        val lastRowIdx = max(fruitAttendanceRows, fruitResultRows) + 5
        (6..lastRowIdx).forEach { idx -> ws2.createRow(idx) }

        var rowIdx = 6
        ws2.getRow(rowIdx).run {
            setCell(this, 0, "현 장 동 역 자", styleFruitCategoryTBL)
            (1..10).forEach { idx -> setCell(this, idx, null, styleFruitCategoryTB) }
            setMerge(ws2, rowIdx, rowIdx, 0, 10)
            setCell(this, 11, "계", styleFruitCategoryTBR)
        }
        fruitAttendanceList.forEach { item ->
            rowIdx++
            val innerRowIdx = item.value.lastIndex / 10
            (0..innerRowIdx).forEach { idx ->
                ws2.getRow(rowIdx + idx).run {
                    setCell(
                        this,
                        0,
                        if (idx == 0) item.key else null,
                        styleL
                    )
                    setCell(
                        this,
                        11,
                        if (idx == 0) item.value.size.toString() else null,
                        styleR
                    )
                }
            }
            if (innerRowIdx != 0) {
                setMerge(ws2, rowIdx, rowIdx + innerRowIdx, 0, 0)
                setMerge(ws2, rowIdx, rowIdx + innerRowIdx, 11, 11)
            }
            item.value.forEachIndexed { idx, item2 ->
                ws2.getRow(rowIdx + idx / 10).run {
                    if (idx < 10) setCell(
                        this,
                        idx + 1,
                        item2.first.name,
                        if (item2.second) styleFruitBcT else styleFruitNotBcT
                    ) else setCell(
                        this,
                        idx % 10 + 1,
                        item2.first.name,
                        if (item2.second) styleFruitBcX else styleNull
                    )
                }
            }
            ws2.getRow(rowIdx + innerRowIdx).run {
                ((item.value.size)..9).forEach { idx ->
                    setCell(this, idx + 1, null, styleFruitNotBcT)
                }
            }
            rowIdx += innerRowIdx
        }
        if (++rowIdx < lastRowIdx - 1) {
            (rowIdx until (lastRowIdx - 1)).forEach { idx ->
                ws2.getRow(idx).run {
                    if (idx == rowIdx) (0..11).forEach { idx2 ->
                        setCell(this, idx2, null, styleCategoryNormal)
                    } else {
                        setCell(this, 0, null, styleCategoryNormal)
                        setCell(this, 11, null, styleCategoryNormal)
                    }
                }
            }
            setMerge(ws2, rowIdx, lastRowIdx - 2, 0, 11)
        }
        ws2.getRow(lastRowIdx - 1).run {
            setCell(this, 0, "전체참석인원", styleFruitCategoryTL)
            fruitAttendanceCountList.keys.take(10).forEachIndexed { idx, key ->
                setCell(
                    this,
                    idx + 1,
                    key,
                    if (idx == 9) styleFruitCategoryTR else styleFruitCategoryT
                )
            }
            if (fruitAttendanceCountList.size < 10) {
                ((fruitAttendanceCountList.size + 1)..10).forEach { idx ->
                    setCell(this, idx, null, styleFruitCategoryTR)
                }
                setMerge(ws2, lastRowIdx - 1, lastRowIdx - 1, fruitAttendanceCountList.size, 10)
            }
            setCell(
                this,
                11,
                fruitAttendanceList.values.sumOf { it.size }.toString(),
                styleFruitCategoryTR
            )
        }
        ws2.getRow(lastRowIdx).run {
            setCell(this, 0, null, styleFruitCategoryBL)
            fruitAttendanceCountList.values.take(10).forEachIndexed { idx, count ->
                setCell(
                    this,
                    idx + 1,
                    count.toString(),
                    if (idx == 9) styleFruitCategoryBR else styleFruitCategoryB
                )
            }
            if (fruitAttendanceCountList.size < 10) {
                ((fruitAttendanceCountList.size + 1)..10).forEach { idx ->
                    setCell(this, idx, null, styleFruitCategoryBR)
                }
                setMerge(ws2, lastRowIdx, lastRowIdx, fruitAttendanceCountList.size, 10)
            }
            setCell(this, 11, null, styleFruitCategoryBR)
        }
        setMerge(ws2, lastRowIdx - 1, lastRowIdx, 0, 0)
        setMerge(ws2, lastRowIdx - 1, lastRowIdx, 11, 11)

        rowIdx = 6
        ws2.getRow(rowIdx).run {
            setCell(this, 12, "영 접 자", styleFruitCategoryTBLR)
            (13..28).forEach { idx -> setCell(this, idx, null, styleFruitCategoryTBLR) }
            setMerge(ws2, rowIdx, rowIdx, 12, 28)
            setCell(this, 29, "계", styleFruitCategoryTBLR)
        }
        ws2.getRow(++rowIdx).run {
            setCell(this, 12, "구분", styleFruitCategoryTBLR)
            (0 until 4).forEach { idx ->
                setCell(this, 13 + idx * 4, "전도자", styleFruitCategoryTBL)
                setCell(this, 14 + idx * 4, "영접자", styleFruitCategoryTB)
                setCell(this, 15 + idx * 4, "사역자", styleFruitCategoryTB)
                setCell(this, 16 + idx * 4, "나이/전화번호", styleFruitCategoryTBR)
            }
            setCell(this, 29, null, styleFruitCategoryTBLR)
            setMerge(ws2, rowIdx - 1, rowIdx, 29, 29)
        }
        fruitList0.forEach { item ->
            ws2.getRow(++rowIdx).run {
                item.value.forEachIndexed { idx, item2 ->
                    val row = ws2.getRow(rowIdx + idx / 4)
                    setCell(
                        row,
                        13 + (idx % 4) * 4,
                        item2.people,
                        if (idx / 4 == 0) styleTL else styleL
                    )
                    setCell(
                        row,
                        14 + (idx % 4) * 4,
                        item2.believer,
                        if (item2.remeet) {
                            if (idx / 4 == 0) styleFruitRemeetT else styleFruitRemeetX
                        } else {
                            if (idx / 4 == 0) styleT else styleX
                        }
                    )
                    setCell(
                        row,
                        15 + (idx % 4) * 4,
                        item2.teacher,
                        if (idx / 4 == 0) styleT else styleX
                    )
                    val ageAndPhoneText = if (item2.age >= 0) {
                        if (item2.phone.isEmpty()) item2.age.toString() else "${item2.age}/${item2.phone}"
                    } else item2.phone
                    setCell(
                        row,
                        16 + (idx % 4) * 4,
                        ageAndPhoneText,
                        if (idx / 4 == 0) styleTR else styleR
                    )
                }
                val innerRowIdx = item.value.lastIndex / 4
                val innerColumnIdx = 17 + (item.value.lastIndex % 4) * 4
                (innerColumnIdx..28).forEach { idx2 ->
                    val row = ws2.getRow(rowIdx + innerRowIdx)
                    setCell(row, idx2, null, if (innerRowIdx == 0) styleTLR else styleLR)
                }
                if (innerColumnIdx < 28) setMerge(
                    ws2,
                    rowIdx + innerRowIdx,
                    rowIdx + innerRowIdx,
                    innerColumnIdx,
                    28
                )
                (0..innerRowIdx).forEach { idx2 ->
                    val row = ws2.getRow(rowIdx + idx2)
                    setCell(row, 12, if (idx2 == 0) item.key else null, styleCategoryNormal)
                    setCell(
                        row,
                        29,
                        if (idx2 == 0) item.value.size.toString() else null,
                        styleFruitCategoryTBLR
                    )
                }
                if (innerRowIdx != 0) {
                    setMerge(ws2, rowIdx, rowIdx + innerRowIdx, 12, 12)
                    setMerge(ws2, rowIdx, rowIdx + innerRowIdx, 29, 29)
                    rowIdx += innerRowIdx
                }
            }
        }
        ws2.getRow(++rowIdx).run {
            setCell(this, 12, "영접자 합계: ${fruitList0.values.flatten().size}명", styleFruitTotalTBLR)
            (13..29).forEach { idx ->
                setCell(this, idx, null, styleFruitTotalTBLR)
            }
            setMerge(ws2, rowIdx, rowIdx, 12, 29)
        }

        ws2.getRow(++rowIdx).run {
            (12..29).forEach { idx ->
                setCell(this, idx, null, styleCategoryNormal)
            }
            setMerge(ws2, rowIdx, rowIdx, 12, 29)
        }

        ws2.getRow(++rowIdx).run {
            setCell(this, 12, "말 씀 운 동", styleFruitCategoryTBLR)
            (13..28).forEach { idx -> setCell(this, idx, null, styleFruitCategoryTBLR) }
            setMerge(ws2, rowIdx, rowIdx, 12, 28)
            setCell(this, 29, "계", styleFruitCategoryTBLR)
        }
        ws2.getRow(++rowIdx).run {
            setCell(this, 12, "구분", styleFruitCategoryTBLR)
            (0 until 4).forEach { idx ->
                setCell(this, 13 + idx * 4, "사역자", styleFruitCategoryTBL)
                setCell(this, 14 + idx * 4, "영접자/만난횟수", styleFruitCategoryTB)
                setCell(this, 15 + idx * 4, null, styleFruitCategoryTB)
                setMerge(ws2, rowIdx, rowIdx, 14 + idx * 4, 15 + idx * 4)
                setCell(this, 16 + idx * 4, "장소", styleFruitCategoryTBR)
            }
            setCell(this, 29, null, styleFruitCategoryTBLR)
            setMerge(ws2, rowIdx - 1, rowIdx, 29, 29)
        }
        fruitList1.forEach { item ->
            ws2.getRow(++rowIdx).run {
                item.value.forEachIndexed { idx, item2 ->
                    val row = ws2.getRow(rowIdx + idx / 4)
                    setCell(
                        row,
                        13 + (idx % 4) * 4,
                        item2.teacher,
                        if (idx / 4 == 0) styleTL else styleL
                    )
                    setCell(
                        row,
                        14 + (idx % 4) * 4,
                        "${item2.believer}/${item2.frequency.let { if (it <= 0) 1 else it }}",
                        if (idx / 4 == 0) styleT else styleX
                    )
                    setCell(row, 15 + (idx % 4) * 4, null, if (idx / 4 == 0) styleT else styleX)
                    setMerge(
                        ws2,
                        rowIdx + idx / 4,
                        rowIdx + idx / 4,
                        14 + (idx % 4) * 4,
                        15 + (idx % 4) * 4
                    )
                    setCell(
                        row,
                        16 + (idx % 4) * 4,
                        item2.place,
                        if (idx / 4 == 0) styleTR else styleR
                    )
                }
                val innerRowIdx = item.value.lastIndex / 4
                val innerColumnIdx = 17 + (item.value.lastIndex % 4) * 4
                (innerColumnIdx..28).forEach { idx2 ->
                    val row = ws2.getRow(rowIdx + innerRowIdx)
                    setCell(row, idx2, null, if (innerRowIdx == 0) styleTLR else styleLR)
                }
                if (innerColumnIdx < 28) setMerge(
                    ws2,
                    rowIdx + innerRowIdx,
                    rowIdx + innerRowIdx,
                    innerColumnIdx,
                    28
                )
                (0..innerRowIdx).forEach { idx2 ->
                    val row = ws2.getRow(rowIdx + idx2)
                    setCell(row, 12, if (idx2 == 0) item.key else null, styleCategoryNormal)
                    setCell(
                        row,
                        29,
                        if (idx2 == 0) item.value.size.toString() else null,
                        styleFruitCategoryTBLR
                    )
                }
                if (innerRowIdx != 0) {
                    setMerge(ws2, rowIdx, rowIdx + innerRowIdx, 12, 12)
                    setMerge(ws2, rowIdx, rowIdx + innerRowIdx, 29, 29)
                    rowIdx += innerRowIdx
                }
            }
        }
        ws2.getRow(++rowIdx).run {
            setCell(this, 12, "말씀운동 합계: ${fruitList1.values.flatten().size}명", styleFruitTotalTBLR)
            (13..29).forEach { idx ->
                setCell(this, idx, null, styleFruitTotalTBLR)
            }
            setMerge(ws2, rowIdx, rowIdx, 12, 29)
        }
        if (++rowIdx <= lastRowIdx) {
            (rowIdx..lastRowIdx).forEach { idx ->
                ws2.getRow(idx).run {
                    if (idx == rowIdx || idx == lastRowIdx) (12..29).forEach { idx2 ->
                        setCell(this, idx2, null, styleCategoryNormal)
                    } else {
                        setCell(this, 12, null, styleCategoryNormal)
                        setCell(this, 29, null, styleCategoryNormal)
                    }
                }
            }
            setMerge(ws2, rowIdx, lastRowIdx, 12, 29)
        }

        /** setWidth */
        (0..29).forEach { idx ->
            when (idx) {
                0 -> ws2.setColumnWidth(idx, 3000)
                11, 29 -> ws2.setColumnWidth(idx, 1000)
                16, 20, 24, 28 -> ws2.setColumnWidth(idx, 4000)
                else -> ws2.setColumnWidth(idx, 2000)
            }
        }
        ws2.autobreaks = false
        ws2.setRowBreak(lastRowIdx)
        ws2.setColumnBreak(29)
    }

    fun createFile(): File? {
        createSheet1()
        if (!withPeriod) createSheet2()

        return generateFile()
    }

    private fun addPeopleLine(
        row: Int,
        column: Int,
        idx: Int,
        isTop: Boolean,
        isBottom: Boolean,
        people: DataPeople
    ): Int {
        val xssfRow = if (column == 0) ws1.createRow(row) else ws1.getRow(row)
        val style = arrayOf(
            if (isTop && isBottom) arrayOf(styleTBL, styleTB, styleTBR)
            else if (isTop) arrayOf(styleTL, styleT, styleTR)
            else if (isBottom) arrayOf(styleBL, styleB, styleBR)
            else arrayOf(styleL, styleX, styleR),
            if (isTop && isBottom) arrayOf(
                styleFruitChecked1TBL, styleFruitChecked1TB, styleFruitChecked1TBR
            )
            else if (isTop) arrayOf(styleFruitChecked1TL, styleFruitChecked1T, styleFruitChecked1TR)
            else if (isBottom) arrayOf(
                styleFruitChecked1BL, styleFruitChecked1B, styleFruitChecked1BR
            )
            else arrayOf(styleFruitChecked1L, styleFruitChecked1X, styleFruitChecked1R),
            if (isTop && isBottom) arrayOf(
                styleFruitChecked2TBL, styleFruitChecked2TB, styleFruitChecked2TBR
            )
            else if (isTop) arrayOf(styleFruitChecked2TL, styleFruitChecked2T, styleFruitChecked2TR)
            else if (isBottom) arrayOf(
                styleFruitChecked2BL, styleFruitChecked2B, styleFruitChecked2BR
            )
            else arrayOf(styleFruitChecked2L, styleFruitChecked2X, styleFruitChecked2R)
        )
        if (!withPeriod) {
            val attendance = attendanceList[dateList[0].second]?.get(people.id)
            val checked = attendance?.first?.checked ?: 0
            val reason = attendance?.first?.reason.orEmpty()
            val category = attendance?.second.orEmpty()
            setCell(xssfRow, column * 3, (idx + 1).toString(), style[checked][0])
            setCell(xssfRow, column * 3 + 1, people.name, style[checked][1])
            setCell(
                xssfRow,
                column * 3 + 2,
                if (checked == 1) category else if (checked == 2) reason else "",
                style[checked][2]
            )
            return checked
        } else {
            val countChecked = dateList.count {
                (attendanceList[it.second]?.get(people.id)?.first?.checked ?: 0) != 0
            }
            setCell(xssfRow, column * 3, (idx + 1).toString(), style[0][0])
            setCell(xssfRow, column * 3 + 1, people.name, style[0][1])
            setCell(
                xssfRow, column * 3 + 2, "$countChecked / ${dateList.size}", style[0][2]
            )
            return 0
        }
    }

    private fun addOtherPeopleLine(
        row: Int,
        column: Int,
        idx: Int,
        isTop: Boolean,
        isBottom: Boolean,
        people: DataPeople
    ) {
        if (!withPeriod) {
            val xssfRow = if (column == 0 && idx % 2 == 0) ws1.createRow(row) else ws1.getRow(row)
            val style = if (isTop && isBottom) arrayOf(
                styleFruitChecked1TBL, styleFruitChecked1TB, styleFruitChecked1TBR
            )
            else if (isTop) arrayOf(styleFruitChecked1TL, styleFruitChecked1T, styleFruitChecked1TR)
            else if (isBottom) arrayOf(
                styleFruitChecked1BL, styleFruitChecked1B, styleFruitChecked1BR
            )
            else arrayOf(styleFruitChecked1L, styleFruitChecked1X, styleFruitChecked1R)
            if (idx % 2 == 0) {
                setCell(xssfRow, column * 3, (idx + 1).toString(), style[0])
                setCell(xssfRow, column * 3 + 1, people.name, style[1])
            } else {
                setCell(xssfRow, column * 3 + 2, people.name, style[2])
            }
        }
    }

    private fun addCategory(
        row: Int,
        column: Int,
        isBottom: Boolean,
        category: String
    ) {
        val xssfRow = if (column == 0) ws1.createRow(row) else ws1.getRow(row)
        val style = if (isBottom) arrayOf(
            styleFruitCategoryTBL, styleFruitCategoryTB, styleFruitCategoryTBR
        )
        else arrayOf(styleFruitCategoryTL, styleFruitCategoryT, styleFruitCategoryTR)
        setCell(xssfRow, column * 3, "NO", style[0])
        setCell(xssfRow, column * 3 + 1, category, style[1])
        setCell(xssfRow, column * 3 + 2, null, style[2])
        setMerge(ws1, row, row, column * 3 + 1, column * 3 + 2)
    }

    private val colorCategory = IndexedColors.PALE_BLUE.index
    private val colorChecked1 = IndexedColors.LIME.index
    private val colorChecked2 = IndexedColors.LIGHT_ORANGE.index
    private val colorTotal = IndexedColors.YELLOW.index
    private val colorBc = IndexedColors.LIGHT_ORANGE.index
    private val colorRemeet = IndexedColors.LIME.index

    private val styleFruitCategoryT: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontBold)
            fillForegroundColor = colorCategory
            setFillPattern(FillPatternType.SOLID_FOREGROUND)
            setBorder(t = true, b = false, l = false, r = false)
        }
    }
    private val styleFruitCategoryB: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontBold)
            fillForegroundColor = colorCategory
            setFillPattern(FillPatternType.SOLID_FOREGROUND)
            setBorder(t = false, b = true, l = false, r = false)
        }
    }
    private val styleFruitCategoryTB: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontBold)
            fillForegroundColor = colorCategory
            setFillPattern(FillPatternType.SOLID_FOREGROUND)
            setBorder(t = true, b = true, l = false, r = false)
        }
    }
    private val styleFruitCategoryTL: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontBold)
            fillForegroundColor = colorCategory
            setFillPattern(FillPatternType.SOLID_FOREGROUND)
            setBorder(t = true, b = false, l = true, r = false)
        }
    }
    private val styleFruitCategoryTR: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontBold)
            fillForegroundColor = colorCategory
            setFillPattern(FillPatternType.SOLID_FOREGROUND)
            setBorder(t = true, b = false, l = false, r = true)
        }
    }
    private val styleFruitCategoryBL: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontBold)
            fillForegroundColor = colorCategory
            setFillPattern(FillPatternType.SOLID_FOREGROUND)
            setBorder(t = false, b = true, l = true, r = false)
        }
    }
    private val styleFruitCategoryBR: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontBold)
            fillForegroundColor = colorCategory
            setFillPattern(FillPatternType.SOLID_FOREGROUND)
            setBorder(t = false, b = true, l = false, r = true)
        }
    }
    private val styleFruitCategoryTBL: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontBold)
            fillForegroundColor = colorCategory
            setFillPattern(FillPatternType.SOLID_FOREGROUND)
            setBorder(t = true, b = true, l = true, r = false)
        }
    }
    private val styleFruitCategoryTBR: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontBold)
            fillForegroundColor = colorCategory
            setFillPattern(FillPatternType.SOLID_FOREGROUND)
            setBorder(t = true, b = true, l = false, r = true)
        }
    }
    private val styleFruitCategoryTBLR: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontBold)
            fillForegroundColor = colorCategory
            setFillPattern(FillPatternType.SOLID_FOREGROUND)
            setBorder(t = true, b = true, l = true, r = true)
        }
    }

    private val styleFruitChecked1X: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontNormal)
            fillForegroundColor = colorChecked1
            setFillPattern(FillPatternType.SOLID_FOREGROUND)
            setBorder(t = false, b = false, l = false, r = false)
        }
    }
    private val styleFruitChecked1T: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontNormal)
            fillForegroundColor = colorChecked1
            setFillPattern(FillPatternType.SOLID_FOREGROUND)
            setBorder(t = true, b = false, l = false, r = false)
        }
    }
    private val styleFruitChecked1B: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontNormal)
            fillForegroundColor = colorChecked1
            setFillPattern(FillPatternType.SOLID_FOREGROUND)
            setBorder(t = false, b = true, l = false, r = false)
        }
    }
    private val styleFruitChecked1L: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontNormal)
            fillForegroundColor = colorChecked1
            setFillPattern(FillPatternType.SOLID_FOREGROUND)
            setBorder(t = false, b = false, l = true, r = false)
        }
    }
    private val styleFruitChecked1R: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontNormal)
            fillForegroundColor = colorChecked1
            setFillPattern(FillPatternType.SOLID_FOREGROUND)
            setBorder(t = false, b = false, l = false, r = true)
        }
    }
    private val styleFruitChecked1TB: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontNormal)
            fillForegroundColor = colorChecked1
            setFillPattern(FillPatternType.SOLID_FOREGROUND)
            setBorder(t = true, b = true, l = false, r = false)
        }
    }
    private val styleFruitChecked1TL: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontNormal)
            fillForegroundColor = colorChecked1
            setFillPattern(FillPatternType.SOLID_FOREGROUND)
            setBorder(t = true, b = false, l = true, r = false)
        }
    }
    private val styleFruitChecked1TR: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontNormal)
            fillForegroundColor = colorChecked1
            setFillPattern(FillPatternType.SOLID_FOREGROUND)
            setBorder(t = true, b = false, l = false, r = true)
        }
    }
    private val styleFruitChecked1BL: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontNormal)
            fillForegroundColor = colorChecked1
            setFillPattern(FillPatternType.SOLID_FOREGROUND)
            setBorder(t = false, b = true, l = true, r = false)
        }
    }
    private val styleFruitChecked1BR: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontNormal)
            fillForegroundColor = colorChecked1
            setFillPattern(FillPatternType.SOLID_FOREGROUND)
            setBorder(t = false, b = true, l = false, r = true)
        }
    }
    private val styleFruitChecked1LR: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontNormal)
            fillForegroundColor = colorChecked1
            setFillPattern(FillPatternType.SOLID_FOREGROUND)
            setBorder(t = false, b = false, l = true, r = true)
        }
    }
    private val styleFruitChecked1TBL: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontNormal)
            fillForegroundColor = colorChecked1
            setFillPattern(FillPatternType.SOLID_FOREGROUND)
            setBorder(t = true, b = true, l = true, r = false)
        }
    }
    private val styleFruitChecked1TBR: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontNormal)
            fillForegroundColor = colorChecked1
            setFillPattern(FillPatternType.SOLID_FOREGROUND)
            setBorder(t = true, b = true, l = false, r = true)
        }
    }
    private val styleFruitChecked1TLR: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontNormal)
            fillForegroundColor = colorChecked1
            setFillPattern(FillPatternType.SOLID_FOREGROUND)
            setBorder(t = true, b = false, l = true, r = true)
        }
    }
    private val styleFruitChecked1BLR: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontNormal)
            fillForegroundColor = colorChecked1
            setFillPattern(FillPatternType.SOLID_FOREGROUND)
            setBorder(t = false, b = true, l = true, r = true)
        }
    }

    private val styleFruitChecked2X: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontNormal)
            fillForegroundColor = colorChecked2
            setFillPattern(FillPatternType.SOLID_FOREGROUND)
            setBorder(t = false, b = false, l = false, r = false)
        }
    }
    private val styleFruitChecked2T: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontNormal)
            fillForegroundColor = colorChecked2
            setFillPattern(FillPatternType.SOLID_FOREGROUND)
            setBorder(t = true, b = false, l = false, r = false)
        }
    }
    private val styleFruitChecked2B: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontNormal)
            fillForegroundColor = colorChecked2
            setFillPattern(FillPatternType.SOLID_FOREGROUND)
            setBorder(t = false, b = true, l = false, r = false)
        }
    }
    private val styleFruitChecked2L: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontNormal)
            fillForegroundColor = colorChecked2
            setFillPattern(FillPatternType.SOLID_FOREGROUND)
            setBorder(t = false, b = false, l = true, r = false)
        }
    }
    private val styleFruitChecked2R: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontNormal)
            fillForegroundColor = colorChecked2
            setFillPattern(FillPatternType.SOLID_FOREGROUND)
            setBorder(t = false, b = false, l = false, r = true)
        }
    }
    private val styleFruitChecked2TB: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontNormal)
            fillForegroundColor = colorChecked2
            setFillPattern(FillPatternType.SOLID_FOREGROUND)
            setBorder(t = true, b = true, l = false, r = false)
        }
    }
    private val styleFruitChecked2TL: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontNormal)
            fillForegroundColor = colorChecked2
            setFillPattern(FillPatternType.SOLID_FOREGROUND)
            setBorder(t = true, b = false, l = true, r = false)
        }
    }
    private val styleFruitChecked2TR: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontNormal)
            fillForegroundColor = colorChecked2
            setFillPattern(FillPatternType.SOLID_FOREGROUND)
            setBorder(t = true, b = false, l = false, r = true)
        }
    }
    private val styleFruitChecked2BL: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontNormal)
            fillForegroundColor = colorChecked2
            setFillPattern(FillPatternType.SOLID_FOREGROUND)
            setBorder(t = false, b = true, l = true, r = false)
        }
    }
    private val styleFruitChecked2BR: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontNormal)
            fillForegroundColor = colorChecked2
            setFillPattern(FillPatternType.SOLID_FOREGROUND)
            setBorder(t = false, b = true, l = false, r = true)
        }
    }
    private val styleFruitChecked2LR: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontNormal)
            fillForegroundColor = colorChecked2
            setFillPattern(FillPatternType.SOLID_FOREGROUND)
            setBorder(t = false, b = false, l = true, r = true)
        }
    }
    private val styleFruitChecked2TBL: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontNormal)
            fillForegroundColor = colorChecked2
            setFillPattern(FillPatternType.SOLID_FOREGROUND)
            setBorder(t = true, b = true, l = true, r = false)
        }
    }
    private val styleFruitChecked2TBR: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontNormal)
            fillForegroundColor = colorChecked2
            setFillPattern(FillPatternType.SOLID_FOREGROUND)
            setBorder(t = true, b = true, l = false, r = true)
        }
    }
    private val styleFruitChecked2TLR: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontNormal)
            fillForegroundColor = colorChecked2
            setFillPattern(FillPatternType.SOLID_FOREGROUND)
            setBorder(t = true, b = false, l = true, r = true)
        }
    }
    private val styleFruitChecked2BLR: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontNormal)
            fillForegroundColor = colorChecked2
            setFillPattern(FillPatternType.SOLID_FOREGROUND)
            setBorder(t = false, b = true, l = true, r = true)
        }
    }

    private val styleFruitTotalX: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontBold)
            fillForegroundColor = colorTotal
            setFillPattern(FillPatternType.SOLID_FOREGROUND)
            setBorder(t = false, b = false, l = false, r = false)
        }
    }
    private val styleFruitTotalL: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontBold)
            fillForegroundColor = colorTotal
            setFillPattern(FillPatternType.SOLID_FOREGROUND)
            setBorder(t = false, b = false, l = true, r = false)
        }
    }
    private val styleFruitTotalR: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontBold)
            fillForegroundColor = colorTotal
            setFillPattern(FillPatternType.SOLID_FOREGROUND)
            setBorder(t = false, b = false, l = false, r = true)
        }
    }
    private val styleFruitTotalTBLR: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontBold)
            fillForegroundColor = colorTotal
            setFillPattern(FillPatternType.SOLID_FOREGROUND)
            setBorder(t = true, b = true, l = true, r = true)
        }
    }

    private val styleFruitLegend: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.LEFT)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontNormal)
        }
    }
    private val styleFruitBcLegend: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontNormal)
            fillForegroundColor = colorBc
            setFillPattern(FillPatternType.SOLID_FOREGROUND)
            setBorder(t = false, b = false, l = false, r = false)
        }
    }
    private val styleFruitBcX: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontNormal)
            fillForegroundColor = colorBc
            setFillPattern(FillPatternType.SOLID_FOREGROUND)
        }
    }
    private val styleFruitBcT: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontNormal)
            fillForegroundColor = colorBc
            setFillPattern(FillPatternType.SOLID_FOREGROUND)
            setBorder(t = false, b = null, l = null, r = null)
        }
    }
    private val styleFruitNotBcT: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontNormal)
            setBorder(t = false, b = null, l = null, r = null)
        }
    }
    private val styleFruitRemeetLegend: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontNormal)
            fillForegroundColor = colorRemeet
            setFillPattern(FillPatternType.SOLID_FOREGROUND)
            setBorder(t = false, b = false, l = false, r = false)
        }
    }
    private val styleFruitRemeetT: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontNormal)
            fillForegroundColor = colorRemeet
            setFillPattern(FillPatternType.SOLID_FOREGROUND)
            setBorder(t = true, b = false, l = false, r = false)
        }
    }
    private val styleFruitRemeetX: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontNormal)
            fillForegroundColor = colorRemeet
            setFillPattern(FillPatternType.SOLID_FOREGROUND)
            setBorder(t = false, b = false, l = false, r = false)
        }
    }
}