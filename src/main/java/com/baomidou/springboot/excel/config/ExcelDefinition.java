package com.baomidou.springboot.excel.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Excel 정의
 *
 * @author lisuo
 *
 */
public class ExcelDefinition {

    /** ID,필수 */
    private String id;

    /** Class 이름, 필수 */
    private String className;

    /** Class */
    private Class<?> clazz;

    /**내보내기, 시트 이름을 설정할 수 없습니다.*/
    private String sheetname;

    /**내보내기, 모든 기본 열 너비 페이지, 설정할 수 없습니다.*/
    private Integer defaultColumnWidth;

    /**내보내는 동안 기본 cell : 지원,center,left,right*/
    private Short defaultAlign;

    /** Field 속성의 전체 정의 */
    private List<FieldValue> fieldValues = new ArrayList<FieldValue>();

    /** 내보내기 스타일 지원을 열 것인지 (많은 양의 데이터, 그것은 열 것을 권장하지 않습니다), 기본 구현 예외를 던질 수 있습니다 구현 WorkBook.createCellStyle */
    // 큰 데이터의 양에 관해서, 고정되었으므로, 스타일의 사용, 현재 테스트 된 (백만) 데이터가 오류가되지 않을 것이라는 것을 안심할 수 있습니다
    private Boolean enableStyle = false;

    /** 엑셀 파일 시트 색인, 기본값은 0, 첫 번째 */
    private int sheetIndex = 0;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }

    public List<FieldValue> getFieldValues() {
        return fieldValues;
    }

    public void setFieldValues(List<FieldValue> fieldValues) {
        this.fieldValues = fieldValues;
    }

    public String getSheetname() {
        return sheetname;
    }

    public void setSheetname(String sheetname) {
        this.sheetname = sheetname;
    }

    public Integer getDefaultColumnWidth() {
        return defaultColumnWidth;
    }

    public void setDefaultColumnWidth(Integer defaultColumnWidth) {
        this.defaultColumnWidth = defaultColumnWidth;
    }

    public Boolean getEnableStyle() {
        return enableStyle;
    }

    public void setEnableStyle(Boolean enableStyle) {
        this.enableStyle = enableStyle;
    }

    public Short getDefaultAlign() {
        return defaultAlign;
    }

    public void setDefaultAlign(Short defaultAlign) {
        this.defaultAlign = defaultAlign;
    }

    public int getSheetIndex() {
        return sheetIndex;
    }

    public void setSheetIndex(int sheetIndex) {
        this.sheetIndex = sheetIndex;
    }
}
