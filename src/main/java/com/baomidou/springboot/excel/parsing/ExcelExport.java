package com.baomidou.springboot.excel.parsing;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.util.TypeUtils;

import com.baomidou.springboot.excel.ExcelDefinitionReader;
import com.baomidou.springboot.excel.config.ExcelDefinition;
import com.baomidou.springboot.excel.config.FieldValue;
import com.baomidou.springboot.excel.exception.ExcelException;
import com.baomidou.springboot.excel.result.ExcelExportResult;
import com.baomidou.springboot.excel.util.ReflectUtil;

/**
 * Excel 내보내기 구현 클래스
 * @author lisuo
 *
 */
public class ExcelExport extends AbstractExcelResolver{


    public ExcelExport(ExcelDefinitionReader definitionReader) {
        super(definitionReader);
    }

    /**
     * 내보내기 Excel을 만듭니다. 컬렉션에 데이터가 없으면 null을 반환합니다.
     * @param id	 ExcelXML 구성 bean의 ID
     * @param beans  ExcelXML 구성 bean 콜렉션
     * @param header Excel 헤더 정보 (제목 앞)
     * @param fields 내 보낸 필드 지정
     * @return
     * @throws Exception
     */
    public ExcelExportResult createExcel(String id,List<?> beans,ExcelHeader header,List<String> fields) throws Exception{
        ExcelExportResult exportResult = null;
        if(CollectionUtils.isNotEmpty(beans)){
            //등록 정보에서 bean 정보 얻기
            ExcelDefinition excelDefinition = definitionReader.getRegistry().get(id);
            if(excelDefinition==null){
                throw new ExcelException(" ["+id+"]에 대한 구성 정보가 없습니다.");
            }
            //실제 들어오는 bean 유형
            Class<?> realClass = beans.get(0).getClass();
            //들어오는 유형은 직접 생성 된 Excel 구성 클래스 또는 그 하위 클래스의 유형입니다.
            if(realClass==excelDefinition.getClazz() || TypeUtils.isAssignable(excelDefinition.getClazz(),realClass)){
                //지정된 필드의 내보내기 제목이 null이 아니며 동적으로 생성 된 Excel 정의
                excelDefinition = dynamicCreateExcelDefinition(excelDefinition,fields);
            }
            //들어오는 유형은 상위 클래스 구성 유형의 상위 클래스이며 상위 클래스 구성의 속성을 가져 오는 것입니다.
            else if(TypeUtils.isAssignable(realClass,excelDefinition.getClazz())){
                excelDefinition = extractSuperClassFields(excelDefinition, fields, realClass);
            }else{
                //들어오는 콜렉션을 판단하고 구성 파일의 유형이 공통 상위 클래스를 갖는 경우, 전환이있는 경우
                Object superClass = ReflectUtil.getEqSuperClass(realClass, excelDefinition.getClazz());
                if(superClass!=Object.class){
                    excelDefinition = extractSuperClassFields(excelDefinition, fields, realClass);
                }else{
                    throw new ExcelException("전달 된 인수의 유형은 다음과 같습니다.:"+beans.get(0).getClass().getName()
                            +"그러나 구성 파일의 유형은 다음과 같습니다.: "+excelDefinition.getClazz().getName()+",인수는 동일한 수퍼 클래스에서 수퍼 클래스도 아니고 하위 클래스도 아니므로 변환 할 수 없습니다.");
                }

            }
            exportResult = doCreateExcel(excelDefinition,beans,header);
        }
        return exportResult;
    }

    /**
     * Excel, 템플릿 정보 만들기
     * @param id	 ExcelXML 구성 bean의 ID
     * @param header Excel 헤더 정보 (제목 앞)
     * @param fields 내 보낸 필드 지정
     * @return
     * @throws Exception
     */
    public Workbook createExcelTemplate(String id,ExcelHeader header,List<String> fields) throws Exception{
        //등록 정보에서 bean 정보 얻기
        ExcelDefinition excelDefinition = definitionReader.getRegistry().get(id);
        if(excelDefinition==null){
            throw new ExcelException("["+id+"] 에 대한 구성 정보가 없습니다.");
        }
        excelDefinition = dynamicCreateExcelDefinition(excelDefinition,fields);
        return doCreateExcel(excelDefinition, null, header).build();
    }

    //부모가 소유 한 필드를 필터링 용으로 지정된 필드에만 기초로 추출합니다.
    private ExcelDefinition extractSuperClassFields(ExcelDefinition excelDefinition,List<String> fields,Class<?> realClass){
        //부모 클래스가 소유 한 필드 추출
        List<String> fieldNames = ReflectUtil.getFieldNames(realClass);
        excelDefinition = dynamicCreateExcelDefinition(excelDefinition, fieldNames);
        //지정된 필드를 추출합니다.
        //지정된 필드의 제목을 내 보내면 동적으로 생성 된 Excel 정의가 null이 아닙니다.
        excelDefinition = dynamicCreateExcelDefinition(excelDefinition,fields);
        return excelDefinition;
    }

    /**
     * 동적으로 ExcelDefinition 만들기
     * @param excelDefinition 원본 ExcelDefinition
     * @param fields
     * @return
     */
    private ExcelDefinition dynamicCreateExcelDefinition(ExcelDefinition excelDefinition, List<String> fields) {
        if(CollectionUtils.isNotEmpty(fields)){
            ExcelDefinition newDef = new ExcelDefinition();
            ReflectUtil.copyProps(excelDefinition, newDef,"fieldValues");
            List<FieldValue> oldValues = excelDefinition.getFieldValues();
            List<FieldValue> newValues = new ArrayList<FieldValue>(oldValues.size());
            //순서에 따라
            for(String name:fields){
                for(FieldValue field:oldValues){
                    String fieldName = field.getName();
                    if(fieldName.equals(name)){
                        newValues.add(field);
                        break;
                    }
                }
            }
            newDef.setFieldValues(newValues);
            return newDef;
        }
        return excelDefinition;

    }

    protected ExcelExportResult doCreateExcel(ExcelDefinition excelDefinition, List<?> beans,ExcelHeader header) throws Exception {
        // Workbook 만들기
        Workbook workbook = new SXSSFWorkbook();
        Sheet sheet = null;
        if(excelDefinition.getSheetname()!=null){
            sheet = workbook.createSheet(excelDefinition.getSheetname());
        }else{
            sheet = workbook.createSheet();
        }
        //제목을 만들기 전에 buildHeader 메서드를 호출하여 다른 데이터 생성 정보 중 일부를 완료하십시오.
        if(header!=null){
            header.buildHeader(sheet,excelDefinition,beans);
        }

        Row titleRow = createTitle(excelDefinition,sheet,workbook);
        //listBean이 비어 있지 않으면 데이터 라인을 생성하십시오.
        if(beans!=null){
            createRows(excelDefinition, sheet, beans,workbook,titleRow);
        }
        ExcelExportResult exportResult = new ExcelExportResult(excelDefinition, sheet, workbook, titleRow,this);
        return exportResult;
    }

    /**
     * Excel 제목 만들기
     * @param excelDefinition
     * @param sheet
     * @return 제목 줄
     */
    protected Row createTitle(ExcelDefinition excelDefinition,Sheet sheet,Workbook workbook) {
        //제목 색인 번호
        int titleIndex = sheet.getPhysicalNumberOfRows();
        Row titleRow = sheet.createRow(titleIndex);
        List<FieldValue> fieldValues = excelDefinition.getFieldValues();
        for(int i=0;i<fieldValues.size();i++){
            FieldValue fieldValue = fieldValues.get(i);
            //셀 너비 설정
            if(fieldValue.getColumnWidth() !=null){
                sheet.setColumnWidth(i, fieldValue.getColumnWidth());
            }
            //기본 너비가 비어 있지 않으면 기본 너비
            else if(excelDefinition.getDefaultColumnWidth()!=null){
                sheet.setColumnWidth(i, excelDefinition.getDefaultColumnWidth());
            }
            Cell cell = titleRow.createCell(i);
            if(excelDefinition.getEnableStyle()){
                if(fieldValue.getAlign()!=null || fieldValue.getTitleBgColor()!=null || fieldValue.getTitleFountColor() !=null || excelDefinition.getDefaultAlign()!=null){
                    cell.setCellStyle(workbook.createCellStyle());
                    //셀 정렬 설정
                    setAlignStyle(fieldValue, workbook, cell,excelDefinition);
                    //제목 배경색 설정
                    setTitleBgColorStyle(fieldValue, workbook, cell);
                    //제목 글꼴 색상 설정
                    setTitleFountColorStyle(fieldValue, workbook, cell);
                }
            }
            setCellValue(cell,fieldValue.getTitle());
        }
        return titleRow;
    }

    /**
     * 선 만들기
     * @param excelDefinition
     * @param sheet
     * @param beans
     * @param workbook
     * @param titleIndex
     * @throws Exception
     */
    public void createRows(ExcelDefinition excelDefinition,Sheet sheet,List<?> beans,Workbook workbook,Row titleRow) throws Exception{
        int num = sheet.getPhysicalNumberOfRows();
        int startRow = num ;
        for(int i=0;i<beans.size();i++){
            Row row = sheet.createRow(i+num);
            createRow(excelDefinition,row,beans.get(i),workbook,sheet,titleRow,startRow++);
        }
    }


    /**
     * 선 만들기
     * @param excelDefinition
     * @param row
     * @param bean
     * @param workbook
     * @param sheet
     * @param titleRow
     * @param rowNum
     * @throws Exception
     */
    protected void createRow(ExcelDefinition excelDefinition, Row row, Object bean,Workbook workbook,Sheet sheet,Row titleRow,int rowNum) throws Exception {
        List<FieldValue> fieldValues = excelDefinition.getFieldValues();
        for(int i=0;i<fieldValues.size();i++){
            FieldValue fieldValue = fieldValues.get(i);
            String name = fieldValue.getName();
            Object value = ReflectUtil.getProperty(bean, name);
            //paser로부터 가치를 얻으십시오.
            Object val = convert(bean,value, fieldValue, Type.EXPORT,rowNum);
            Cell cell = row.createCell(i);
            //셀 스타일은 제목과 일치합니다. 일치하는 경우 해당 제목 스타일 설정을 찾습니다.
            if(excelDefinition.getEnableStyle()){
                if(fieldValue.isUniformStyle()){
                    //제목 줄 가져 오기
                    //해당 헤더 행 스타일 가져 오기
                    Cell titleCell = titleRow.getCell(i);
                    CellStyle cellStyle = titleCell.getCellStyle();
                    cell.setCellStyle(cellStyle);
                }
            }
            setCellValue(cell, val);
        }
    }

    //셀 정렬을 설정합니다.
    private void setAlignStyle(FieldValue fieldValue,Workbook workbook,Cell cell,ExcelDefinition excelDefinition){
        if(fieldValue.getAlign()!=null){
            CellStyle cellStyle = cell.getCellStyle();
            cellStyle.setAlignment(fieldValue.getAlign());
            cell.setCellStyle(cellStyle);
        }else if(excelDefinition.getDefaultAlign()!=null){
            CellStyle cellStyle = cell.getCellStyle();
            cellStyle.setAlignment(excelDefinition.getDefaultAlign());
            cell.setCellStyle(cellStyle);
        }
    }

    //셀 배경색 모드를 설정합니다.
    private void setTitleBgColorStyle(FieldValue fieldValue,Workbook workbook,Cell cell){
        if(fieldValue.getTitleBgColor()!=null){
            CellStyle cellStyle = cell.getCellStyle();
            cellStyle.setFillForegroundColor(fieldValue.getTitleBgColor());
            cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        }
    }

    //셀 글꼴 색상을 설정합니다.
    private void setTitleFountColorStyle(FieldValue fieldValue,Workbook workbook,Cell cell){
        if(fieldValue.getTitleFountColor()!=null){
            CellStyle cellStyle = cell.getCellStyle();
            Font font = workbook.createFont();
            font.setColor(fieldValue.getTitleFountColor());
            cellStyle.setFont(font);
        }
    }


}
