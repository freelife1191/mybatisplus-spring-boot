package com.baomidou.springboot.excel.result;

import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.baomidou.springboot.excel.config.ExcelDefinition;
import com.baomidou.springboot.excel.exception.ExcelException;
import com.baomidou.springboot.excel.parsing.ExcelExport;

/**
 * Excel 결과 내보내기
 *
 * @author lisuo
 *
 */
public class ExcelExportResult {
    private ExcelDefinition excelDefinition;
    private Sheet sheet;
    private Workbook workbook ;
    private Row titleRow ;
    private ExcelExport excelExport;

    public ExcelExportResult(ExcelDefinition excelDefinition, Sheet sheet, Workbook workbook, Row titleRow,ExcelExport excelExport) {
        super();
        this.excelDefinition = excelDefinition;
        this.sheet = sheet;
        this.workbook = workbook;
        this.titleRow = titleRow;
        this.excelExport = excelExport;
    }

    /**
     * 추가 데이터
     * @param beans ListBean
     * @return ExcelExportResult
     */
    public ExcelExportResult append(List<?> beans){
        try {
            excelExport.createRows(excelDefinition, sheet, beans, workbook, titleRow);
        } catch (Exception e) {
            throw new ExcelException(e);
        }
        return this;
    }

    /**
     * 내보내기가 완료되면 WorkBook 가져 오기
     * @return
     */
    public Workbook build(){
        return workbook;
    }

}
