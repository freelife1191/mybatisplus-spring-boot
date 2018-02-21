package com.baomidou.springboot.excel.config;

import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * Excel 필드 정의
 *
 * @author lisuo
 *
 */
public class FieldValue {

    //가져 오기 및 내보내기가 유효합니다.
    /** 필수 속성 이름 */
    private String name;
    /** 제목, 필수 항목 */
    private String title;
    /** 날짜 패턴, 날짜가 설정되지 않은 경우 register, 예외를 throw합니다.*/
    private String pattern;
    /** 예를 들어 (1 : 남성, 2 : 여성)이라는 표현은 값이 1이고 (남성) 값이 값이고 값이 2 (여성) 인 것을 의미합니다. */
    private String format;
    /** Excel 값 해결 인터페이스 정의 : 사용자 정의 구현 (전체 클래스 이름) */
    private String cellValueConverterName;

    //Excel로 가져올 때 효과적입니다.
    /** null 일까? */
    private boolean isNull = true;
    /** 정규 표현식, 수입 효과*/
    private String regex;
    /** 정규 표현식이 전달되지 않습니다. 오류 메시지 */
    private String regexErrMsg;

    //효과적인 수출
    /** 셀의 너비,이 속성은 자체 값을 너무 크게 쓰는 등 enableStyle의 영향을받지 않습니다. 해결할 org.apache.poi.ss.usermodel.Sheet.setColumnWidth (int, int) */
    private Integer columnWidth;
    /** 도중에 셀 : 지원,center,left,right */
    private Short align ;
    /** 제목 셀 배경색 : org.apache.poi.ss.usermodel.IndexedColors 사용 가능한 색상 참조*/
    private Short titleBgColor;
    /** 제목 셀 글꼴 색상 : 사용 가능한 색상은 org.apache.poi.ss.usermodel.IndexedColors를 참조하십시오.*/
    private Short titleFountColor;
    /** 셀 스타일이 제목 스타일과 같은지 여부 */
    private boolean uniformStyle = false;

    /** Number 형의 DecimalFormat 패턴 */
    private String decimalFormatPattern;
    /** 구성 할 수없는 DecimalFormat 인스턴스는 decimalFormatPattern 속성을 기반으로 만들어집니다. */
    private DecimalFormat decimalFormat;
    /** DecimalFormat 예제, RoundingMode, 문자를 다룰 때, 소수점 두 자리를 예약 한 다음 처리하는 방법이 세 자리 이상인 경우? 이 구성을 통해 처리 방법을 지정할 수 있으며, 기본값은 반올림됩니다. */
    private RoundingMode roundingMode = RoundingMode.DOWN;
    /** 값이 비어 있으면 필드의 기본값 */
    private String defaultValue;


    /*
     * 기타 구성 항목 :
     * Excel 가져 오기 및 내보내기는 사용자 지정 변환기에서 아마도이 매개 변수를 활용할 수있는 할 일이 없으며 일부 다른 정보의 더 유연한 구성이 될 수 있습니다.
     * 예를 들어 변환기가 자동으로 두 개 이상 매핑되면 구성 매개 변수가보다 유연 해지고 필요에 따라 JSON 및 기타 유형의 데이터로 구성 될 수 있습니다
     */
    private String otherConfig;


    public FieldValue() {
    }

    public FieldValue(String name, String title, String pattern, String format) {
        this.name = name;
        this.title = title;
        this.pattern = pattern;
        this.format = format;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public boolean isNull() {
        return isNull;
    }

    public void setNull(boolean isNull) {
        this.isNull = isNull;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public String getRegexErrMsg() {
        return regexErrMsg;
    }

    public void setRegexErrMsg(String regexErrMsg) {
        this.regexErrMsg = regexErrMsg;
    }

    public String getCellValueConverterName() {
        return cellValueConverterName;
    }

    public void setCellValueConverterName(String cellValueConverterName) {
        this.cellValueConverterName = cellValueConverterName;
    }

    public Short getAlign() {
        return align;
    }

    public void setAlign(Short align) {
        this.align = align;
    }

    public Integer getColumnWidth() {
        return columnWidth;
    }

    public void setColumnWidth(Integer columnWidth) {
        this.columnWidth = columnWidth;
    }

    public Short getTitleBgColor() {
        return titleBgColor;
    }

    public void setTitleBgColor(Short titleBgColor) {
        this.titleBgColor = titleBgColor;
    }

    public Short getTitleFountColor() {
        return titleFountColor;
    }

    public void setTitleFountColor(Short titleFountColor) {
        this.titleFountColor = titleFountColor;
    }

    public boolean isUniformStyle() {
        return uniformStyle;
    }

    public void setUniformStyle(boolean uniformStyle) {
        this.uniformStyle = uniformStyle;
    }

    public String getOtherConfig() {
        return otherConfig;
    }

    public void setOtherConfig(String otherConfig) {
        this.otherConfig = otherConfig;
    }

    public String getDecimalFormatPattern() {
        return decimalFormatPattern;
    }

    public void setDecimalFormatPattern(String decimalFormatPattern) {
        this.decimalFormatPattern = decimalFormatPattern;
    }

    public DecimalFormat getDecimalFormat() {
        return decimalFormat;
    }

    public void setDecimalFormat(DecimalFormat decimalFormat) {
        this.decimalFormat = decimalFormat;
    }

    public RoundingMode getRoundingMode() {
        return roundingMode;
    }

    public void setRoundingMode(RoundingMode roundingMode) {
        this.roundingMode = roundingMode;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }



}
