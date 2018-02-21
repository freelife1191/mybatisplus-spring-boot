package com.baomidou.springboot.excel;

import java.util.Map;

import com.baomidou.springboot.excel.config.ExcelDefinition;

/**
 * Excel은 인터페이스를 정의합니다
 * @author lisuo
 *
 */
public interface ExcelDefinitionReader {
    /**
     * ExcelDefinition 등록 정보 얻기
     * @return
     */
    Map<String, ExcelDefinition> getRegistry();
}
