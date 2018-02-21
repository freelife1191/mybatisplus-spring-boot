package com.baomidou.springboot.excel.parsing;


import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;

import com.baomidou.springboot.excel.config.ExcelDefinition;

/**
 * 제목 정보 앞에 Excel을 내 보냅니다.
 * @author lisuo
 *
 */
public interface ExcelHeader {

    /**
     * 제목 앞에 데이터를 작성하는 방법
     * @param sheet Excel 시트 페이지
     * @param excelDefinition XML 정의 정보
     * @param beans 데이터를 반출 함
     */
    void buildHeader(Sheet sheet, ExcelDefinition excelDefinition, List<?> beans);
}
