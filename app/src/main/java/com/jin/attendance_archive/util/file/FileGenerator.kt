package com.jin.attendance_archive.util.file

import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.*
import java.io.File
import java.io.FileOutputStream

abstract class FileGenerator(protected val title: String) {
    private val fileExtension = ".xlsx"

    protected val wb: XSSFWorkbook by lazy { XSSFWorkbook() }
    private val fileTemp by lazy {
        File.createTempFile(title, fileExtension).also { it.deleteOnExit() }
    }

    protected fun generateFile(): File? {
        val outputStream = FileOutputStream(fileTemp)
        wb.write(outputStream)
        outputStream.flush()
        outputStream.close()
        return fileTemp
    }

    protected fun setCell(row: XSSFRow, cell: Int, value: String?, style: XSSFCellStyle) {
        row.createCell(cell).run {
            if (value != null) setCellValue(value)
            cellType = Cell.CELL_TYPE_STRING
            cellStyle = style
        }
    }

    protected fun setMerge(
        ws: XSSFSheet,
        firstRow: Int,
        lastRow: Int,
        firstCol: Int,
        lastCol: Int
    ) {
        ws.addMergedRegion(CellRangeAddress(firstRow, lastRow, firstCol, lastCol))
    }

    protected fun XSSFCellStyle.setBorder(t: Boolean?, b: Boolean?, l: Boolean?, r: Boolean?) {
        if (t != null) {
            setBorderTop(if (t) BorderStyle.MEDIUM else BorderStyle.THIN)
            topBorderColor = IndexedColors.BLACK.index
        }
        if (b != null) {
            setBorderBottom(if (b) BorderStyle.MEDIUM else BorderStyle.THIN)
            bottomBorderColor = IndexedColors.BLACK.index
        }
        if (l != null) {
            setBorderLeft(if (l) BorderStyle.MEDIUM else BorderStyle.THIN)
            leftBorderColor = IndexedColors.BLACK.index
        }
        if (r != null) {
            setBorderRight(if (r) BorderStyle.MEDIUM else BorderStyle.THIN)
            rightBorderColor = IndexedColors.BLACK.index
        }
    }

    private val mFontSizeTitle: Short = 20
    private val mFontSizeNormal: Short = 10
    private val mFontName = "맑은 고딕"

    private val fontTitle by lazy {
        wb.createFont().apply {
            fontHeightInPoints = mFontSizeTitle
            fontName = mFontName
            color = IndexedColors.BLACK.index
            boldweight = Font.BOLDWEIGHT_BOLD
        }
    }
    protected val fontBold: XSSFFont by lazy {
        wb.createFont().apply {
            fontHeightInPoints = mFontSizeNormal
            fontName = mFontName
            color = IndexedColors.BLACK.index
            boldweight = Font.BOLDWEIGHT_BOLD
        }
    }
    protected val fontNormal: XSSFFont by lazy {
        wb.createFont().apply {
            fontHeightInPoints = mFontSizeNormal
            fontName = mFontName
            color = IndexedColors.BLACK.index
            boldweight = Font.BOLDWEIGHT_NORMAL
        }
    }

    protected val styleTitle: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.TOP)
            setFont(fontTitle)
        }
    }
    protected val styleCategoryBold: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontBold)
            setBorder(t = true, b = true, l = true, r = true)
        }
    }
    protected val styleCategoryNormal: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontNormal)
            setBorder(t = true, b = true, l = true, r = true)
        }
    }
    protected val styleNull: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontNormal)
        }
    }
    protected val styleX: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontNormal)
            setBorder(t = false, b = false, l = false, r = false)
        }
    }
    protected val styleT: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontNormal)
            setBorder(t = true, b = false, l = false, r = false)
        }
    }
    protected val styleB: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontNormal)
            setBorder(t = false, b = true, l = false, r = false)
        }
    }
    protected val styleL: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontNormal)
            setBorder(t = false, b = false, l = true, r = false)
        }
    }
    protected val styleR: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontNormal)
            setBorder(t = false, b = false, l = false, r = true)
        }
    }
    protected val styleTB: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontNormal)
            setBorder(t = true, b = true, l = false, r = false)
        }
    }
    protected val styleTL: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontNormal)
            setBorder(t = true, b = false, l = true, r = false)
        }
    }
    protected val styleTR: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontNormal)
            setBorder(t = true, b = false, l = false, r = true)
        }
    }
    protected val styleBL: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontNormal)
            setBorder(t = false, b = true, l = true, r = false)
        }
    }
    protected val styleBR: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontNormal)
            setBorder(t = false, b = true, l = false, r = true)
        }
    }
    protected val styleLR: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontNormal)
            setBorder(t = false, b = false, l = true, r = true)
        }
    }
    protected val styleTBL: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontNormal)
            setBorder(t = true, b = true, l = true, r = false)
        }
    }
    protected val styleTBR: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontNormal)
            setBorder(t = true, b = true, l = false, r = true)
        }
    }
    protected val styleTLR: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontNormal)
            setBorder(t = true, b = false, l = true, r = true)
        }
    }
    protected val styleBLR: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontNormal)
            setBorder(t = false, b = true, l = true, r = true)
        }
    }
    protected val styleBoldX: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontBold)
            setBorder(t = false, b = false, l = false, r = false)
        }
    }
    protected val styleBoldT: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontBold)
            setBorder(t = true, b = false, l = false, r = false)
        }
    }
    protected val styleBoldB: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontBold)
            setBorder(t = false, b = true, l = false, r = false)
        }
    }
    protected val styleBoldL: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontBold)
            setBorder(t = false, b = false, l = true, r = false)
        }
    }
    protected val styleBoldR: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontBold)
            setBorder(t = false, b = false, l = false, r = true)
        }
    }
    protected val styleBoldTB: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontBold)
            setBorder(t = true, b = true, l = false, r = false)
        }
    }
    protected val styleBoldTL: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontBold)
            setBorder(t = true, b = false, l = true, r = false)
        }
    }
    protected val styleBoldTR: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontBold)
            setBorder(t = true, b = false, l = false, r = true)
        }
    }
    protected val styleBoldBL: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontBold)
            setBorder(t = false, b = true, l = true, r = false)
        }
    }
    protected val styleBoldBR: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontBold)
            setBorder(t = false, b = true, l = false, r = true)
        }
    }
    protected val styleBoldLR: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontBold)
            setBorder(t = false, b = false, l = true, r = true)
        }
    }
    protected val styleBoldTBL: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontBold)
            setBorder(t = true, b = true, l = true, r = false)
        }
    }
    protected val styleBoldTBR: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontBold)
            setBorder(t = true, b = true, l = false, r = true)
        }
    }
    protected val styleBoldTLR: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontBold)
            setBorder(t = true, b = false, l = true, r = true)
        }
    }
    protected val styleBoldBLR: XSSFCellStyle by lazy {
        wb.createCellStyle().apply {
            setAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.CENTER)
            setFont(fontBold)
            setBorder(t = false, b = true, l = true, r = true)
        }
    }
}