package com.baomidou.springboot.excel.xml;


import java.io.InputStream;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.baomidou.springboot.excel.ExcelDefinitionReader;
import com.baomidou.springboot.excel.config.ExcelDefinition;
import com.baomidou.springboot.excel.config.FieldValue;
import com.baomidou.springboot.excel.exception.ExcelException;
import com.baomidou.springboot.excel.parsing.CellValueConverter;
import com.baomidou.springboot.excel.util.ReflectUtil;

/**
 * Excel XML 정의 읽기 등록
 * @author lisuo
 *
 */
public class XMLExcelDefinitionReader implements ExcelDefinitionReader{

    /** 등록 정보 */
    private Map<String, ExcelDefinition> registry;

    /** 프로필 경로 */
    private String location;

    /**
     *
     * @param location xml 경로 구성
     * @throws Exception
     */
    public XMLExcelDefinitionReader(String location) throws Exception {
        this.location = location;
        registry = new HashMap<String, ExcelDefinition>();
        Resource resource = new ClassPathResource(location);
        loadExcelDefinitions(resource.getInputStream());
    }

    /**
     * 로드 해상도 프로파일 정보
     * @param inputStream
     * @throws Exception
     */
    protected void loadExcelDefinitions(InputStream inputStream) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = factory.newDocumentBuilder();
        Document doc = docBuilder.parse(inputStream);
        registerExcelDefinitions(doc);
        inputStream.close();
    }

    /**
     * Excel 정의 정보 등록
     * @param doc
     */
    protected void registerExcelDefinitions(Document doc) {
        Element root = doc.getDocumentElement();
        NodeList nl = root.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            if (node instanceof Element) {
                Element ele = (Element) node;
                processExcelDefinition(ele);
            }
        }
    }

    /**
     * Excel의 정의를 분석하고 확인하십시오.
     * @param ele
     */
    protected void processExcelDefinition(Element ele) {
        ExcelDefinition excelDefinition = new ExcelDefinition();
        String id = ele.getAttribute("id");
        Validate.notNull(id, "Excel 프로필[" + location + "] , id는[ null ] ");
        if (registry.containsKey(id)) {
            throw new ExcelException("Excel 프로필[" + location + "] , id는 [" + id + "] 둘 이상");
        }
        excelDefinition.setId(id);
        String className = ele.getAttribute("class");
        Validate.notNull(className, "Excel 프로필[" + location + "] , id는[" + id + "] class는 [ null ]");
        Class<?> clazz = null;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new ExcelException("Excel 프로필[" + location + "] , id는 [" + id + "] class는 [" + className + "] 클래스가 존재하지 않습니다. ");
        }
        excelDefinition.setClassName(className);
        excelDefinition.setClazz(clazz);
        if(StringUtils.isNotBlank(ele.getAttribute("defaultColumnWidth"))){
            try{
                excelDefinition.setDefaultColumnWidth(Integer.parseInt(ele.getAttribute("defaultColumnWidth")));
            }catch(NumberFormatException e){
                throw new ExcelException("Excel 프로필[" + location + "] , id는 [ " + excelDefinition.getId()
                + " ] defaultColumnWidth 속성을 사용할 수 없습니다. [ "+ele.getAttribute("defaultColumnWidth")+" ],int 유형 만");
            }
        }
        if(StringUtils.isNotBlank(ele.getAttribute("sheetname"))){
            excelDefinition.setSheetname(ele.getAttribute("sheetname"));
        }
        if(StringUtils.isNotBlank(ele.getAttribute("enableStyle"))){
            excelDefinition.setEnableStyle(Boolean.parseBoolean(ele.getAttribute("enableStyle")));
        }
        //기본 정렬
        String defaultAlign = ele.getAttribute("defaultAlign");
        if(StringUtils.isNotBlank(defaultAlign)){
            try{
                //cell 정렬 상수 값 가져 오기
                short constValue = ReflectUtil.getConstValue(CellStyle.class, "ALIGN_"+defaultAlign.toUpperCase());
                excelDefinition.setDefaultAlign(constValue);
            }catch(Exception e){
                throw new ExcelException("Excel 프로필[" + location + "] , id는 [ " + excelDefinition.getId()
                + " ] defaultAlign 속성을 사용할 수 없습니다. [ "+defaultAlign+" ],현재 지원되는 left,center,right");
            }
        }
        //Sheet Index
        if(StringUtils.isNotBlank(ele.getAttribute("sheetIndex"))){
            try{
                int sheetIndex = Integer.parseInt(ele.getAttribute("sheetIndex"));
                if(sheetIndex<0){
                    throw new ExcelException("Excel 프로필[" + location + "] , id는 [ " + excelDefinition.getId()
                    + " ] sheetIndex 속성을 사용할 수 없습니다. [ "+ele.getAttribute("sheetIndex")+" ],인덱스 위치는 최소 0부터 시작합니다.");
                }
                excelDefinition.setSheetIndex(sheetIndex);
            }catch(NumberFormatException e){
                throw new ExcelException("Excel 프로필[" + location + "] , id는 [ " + excelDefinition.getId()
                + " ] sheetIndex 속성을 사용할 수 없습니다. [ "+ele.getAttribute("sheetIndex")+" ],int 유형 만");
            }
        }
        processField(ele, excelDefinition);
        registry.put(id, excelDefinition);
    }

    /**
     * Field 속성 정의를 구문 분석하고 확인합니다.
     * @param ele
     * @param excelDefinition
     */
    protected void processField(Element ele, ExcelDefinition excelDefinition) {
        NodeList fieldNode = ele.getElementsByTagName("field");
        if (fieldNode != null) {
            for (int i = 0; i < fieldNode.getLength(); i++) {
                Node node = fieldNode.item(i);
                if (node instanceof Element) {
                    FieldValue fieldValue = new FieldValue();
                    Element fieldEle = (Element) node;
                    String name = fieldEle.getAttribute("name");
                    Validate.isTrue(StringUtils.isNotBlank(name), "Excel 프로필[" + location + "] , id는 [ " + excelDefinition.getId()
                    + " ] name 속성을 사용할 수 없습니다. [ null ]");
                    fieldValue.setName(name);
                    String title = fieldEle.getAttribute("title");
                    Assert.isTrue(StringUtils.isNotBlank(title), "Excel 프로필[" + location + "] , id는 [ " + excelDefinition.getId()
                    + " ] title 속성을 사용할 수 없습니다. [ null ]");
                    fieldValue.setTitle(title);
                    String pattern = fieldEle.getAttribute("pattern");
                    if(StringUtils.isNotBlank(pattern)){
                        fieldValue.setPattern(pattern);
                    }
                    String format = fieldEle.getAttribute("format");
                    if(StringUtils.isNotBlank(format)){
                        fieldValue.setFormat(format);;
                    }
                    String isNull = fieldEle.getAttribute("isNull");
                    if(StringUtils.isNotBlank(isNull)){
                        fieldValue.setNull(Boolean.parseBoolean(isNull));
                    }
                    String regex = fieldEle.getAttribute("regex");
                    if(StringUtils.isNotBlank(regex)){
                        fieldValue.setRegex(regex);
                    }
                    String regexErrMsg = fieldEle.getAttribute("regexErrMsg");
                    if(StringUtils.isNotBlank(regexErrMsg)){
                        fieldValue.setRegexErrMsg(regexErrMsg);
                    }
                    //정렬
                    String align = fieldEle.getAttribute("align");
                    if(StringUtils.isNotBlank(align)){
                        try{
                            //cell 정렬 상수 값 가져 오기
                            short constValue = ReflectUtil.getConstValue(CellStyle.class, "ALIGN_"+align.toUpperCase());
                            fieldValue.setAlign(constValue);
                        }catch(Exception e){
                            throw new ExcelException("Excel 프로필[" + location + "] , id는 [ " + excelDefinition.getId()
                            + " ] align 속성을 사용할 수 없습니다. [ "+align+" ],현재 지원되는 left,center,right");
                        }
                    }
                    //cell 너비
                    String columnWidth = fieldEle.getAttribute("columnWidth");
                    if(StringUtils.isNotBlank(columnWidth)){
                        try{
                            int intVal = Integer.parseInt(columnWidth);
                            fieldValue.setColumnWidth(intVal);
                        }catch(NumberFormatException e){
                            throw new ExcelException("Excel 프로필[" + location + "] , id는 [ " + excelDefinition.getId()
                            + " ] columnWidth 속성[ "+columnWidth+" ] 법적 가치가 없다.");
                        }
                    }
                    //cell 제목 배경색
                    String titleBgColor = fieldEle.getAttribute("titleBgColor");
                    if(StringUtils.isNotBlank(titleBgColor)){
                        try{
                            //cell 정렬의 상수 값 가져 오기
                            IndexedColors color = ReflectUtil.getConstValue(IndexedColors.class,titleBgColor.toUpperCase());
                            fieldValue.setTitleBgColor(color.index);
                        }catch(Exception e){
                            throw new ExcelException("Excel 프로필[" + location + "] , id는 [ " + excelDefinition.getId()
                            + " ] titleBgColor 속성을 사용할 수 없습니다. [ "+titleBgColor+" ],[org.apache.poi.ss.usermodel.IndexedColors]의 구체적인 모습 지원되는 색상");
                        }
                    }
                    //cell 제목 글꼴 색상
                    String titleFountColor = fieldEle.getAttribute("titleFountColor");
                    if(StringUtils.isNotBlank(titleFountColor)){
                        try{
                            //cell 정렬 상수 값 가져 오기
                            IndexedColors color = ReflectUtil.getConstValue(IndexedColors.class,titleFountColor.toUpperCase());
                            if(color==null){
                                throw new ExcelException("Excel 프로필[" + location + "] , id는 [ " + excelDefinition.getId()
                                + " ] titleFountColor 속성을 사용할 수 없습니다. [ "+titleFountColor+" ],[org.apache.poi.ss.usermodel.IndexedColors]의 구체적인 모습 지원되는 색상");
                            }
                            fieldValue.setTitleFountColor(color.index);
                        }catch(Exception e){
                            throw new ExcelException(e);
                        }
                    }
                    //cell 스타일이 제목 스타일과 일치하는지 여부
                    String uniformStyle = fieldEle.getAttribute("uniformStyle");
                    if(StringUtils.isNotBlank(uniformStyle)){
                        fieldValue.setUniformStyle(Boolean.parseBoolean(uniformStyle));
                    }

                    //사용자 지정 변환기 구문 분석
                    String cellValueConverterName = fieldEle.getAttribute("cellValueConverter");
                    if(StringUtils.isNotBlank(cellValueConverterName)){
                        try {
                            Class<?> clazz = Class.forName(cellValueConverterName);
                            if(!CellValueConverter.class.isAssignableFrom(clazz)){
                                throw new ExcelException("구성된："+cellValueConverterName+"오류 ["+CellValueConverter.class.getName()+"]의 표준 구현이 아닙니다.");
                            }
                            fieldValue.setCellValueConverterName(cellValueConverterName);
                        } catch (ClassNotFoundException e) {
                            throw new ExcelException("정의 된 구문 분석기를 찾을 수 없습니다.：["+cellValueConverterName+"]"+"구성 정보를 확인하십시오.");
                        }
                    }

                    //roundingMode 분석
                    String roundingMode = fieldEle.getAttribute("roundingMode");
                    if(StringUtils.isNotBlank(roundingMode)){
                        try{
                            //roundingMode 정수 값을 가져옵니다.
                            RoundingMode mode =  ReflectUtil.getConstValue(RoundingMode.class,roundingMode.toUpperCase());
                            if(mode == null){
                                throw new ExcelException("Excel 프로필[" + location + "] , id는 [ " + excelDefinition.getId()
                                + " ] roundingMode 속성을 사용할 수 없습니다. [ "+roundingMode+" ],특정모양[java.math.RoundingMode]지원되는 값");
                            }
                            fieldValue.setRoundingMode(mode);
                        }catch(Exception e){
                            throw new ExcelException(e);
                        }
                    }

                    //decimalFormat 분석
                    String decimalFormatPattern = fieldEle.getAttribute("decimalFormatPattern");
                    if(StringUtils.isNotBlank(decimalFormatPattern)){
                        try{
                            fieldValue.setDecimalFormatPattern(decimalFormatPattern);
                            fieldValue.setDecimalFormat(new DecimalFormat(decimalFormatPattern));
                            fieldValue.getDecimalFormat().setRoundingMode(fieldValue.getRoundingMode());
                        }catch(Exception e){
                            throw new ExcelException("Excel 프로필[" + location + "] , id는 [ " + excelDefinition.getId()
                            + " ] decimalFormatPattern 속성을 사용할 수 없습니다. [ "+decimalFormatPattern+" ],표준 Java 형식을 구성하십시오.");
                        }
                    }

                    //다른 구성 항목을 구문 분석합니다.
                    String otherConfig = fieldEle.getAttribute("otherConfig");
                    if(StringUtils.isNotBlank(otherConfig)){
                        fieldValue.setOtherConfig(otherConfig);
                    }

                    //구문 분석, 값이 비어있는, 필드의 기본값
                    String defaultValue = fieldEle.getAttribute("defaultValue");
                    if(StringUtils.isNotBlank(defaultValue)){
                        fieldValue.setDefaultValue(defaultValue);
                    }

                    excelDefinition.getFieldValues().add(fieldValue);
                }
            }
        }
    }

    /**
     * @return 수정할 수없는 등록 정보
     */
    @Override
    public Map<String, ExcelDefinition> getRegistry() {
        return Collections.unmodifiableMap(registry);
    }

    public String getLocation() {
        return location;
    }
}
