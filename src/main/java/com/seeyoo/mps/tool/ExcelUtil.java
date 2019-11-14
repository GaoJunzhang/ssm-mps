package com.seeyoo.mps.tool;

import cn.hutool.core.date.*;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.Date;
import java.util.List;

/**
 * Created by alexis on 2019/8/27.
 */
public class ExcelUtil {
    public static HSSFCellStyle createInfoTitleStyle(HSSFWorkbook wb) {
        HSSFFont boldFont = wb.createFont();
        boldFont.setFontHeight((short) 200);
        boldFont.setColor(HSSFColor.HSSFColorPredefined.GREEN.getIndex());
        boldFont.setBold(true);

        HSSFCellStyle style = wb.createCellStyle();
        style.setFont(boldFont);

        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    public static HSSFCellStyle createRowTitleStyle(HSSFWorkbook wb) {
        HSSFFont boldFont = wb.createFont();
        boldFont.setFontHeight((short) 200);
        boldFont.setColor(HSSFColor.HSSFColorPredefined.GREEN.getIndex());
        boldFont.setBold(true);

        HSSFCellStyle style = wb.createCellStyle();
        style.setFont(boldFont);
        style.setAlignment(HorizontalAlignment.CENTER);

        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    public static HSSFCellStyle createStyle(HSSFWorkbook wb, boolean isAlignCenter) {
        HSSFFont boldFont = wb.createFont();
        boldFont.setFontHeight((short) 200);
        HSSFCellStyle style = wb.createCellStyle();
        style.setFont(boldFont);
        if (isAlignCenter) {
            style.setAlignment(HorizontalAlignment.CENTER);
        }
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setDataFormat(HSSFDataFormat.getBuiltinFormat("###,##0.00"));
        return style;
    }
}