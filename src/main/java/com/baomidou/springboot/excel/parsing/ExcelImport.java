package com.baomidou.springboot.excel.parsing;


import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.baomidou.springboot.excel.ExcelDefinitionReader;
import com.baomidou.springboot.excel.config.ExcelDefinition;
import com.baomidou.springboot.excel.config.FieldValue;
import com.baomidou.springboot.excel.exception.ExcelException;
import com.baomidou.springboot.excel.result.ExcelImportResult;
import com.baomidou.springboot.excel.util.ReflectUtil;

/**
 * Excel 가져 오기 구현 클래스
 * @author lisuo
 *
 */
public class ExcelImport extends AbstractExcelResolver{


    public ExcelImport(ExcelDefinitionReader definitionReader) {
        super(definitionReader);
    }

    /**
     * Excel 정보 읽기
     * @param id 등록 된 ID
     * @param titleIndex 제목 색인
     * @param excelStream Excel 파일 스트림
     * @param sheetIndex 시트 색인 위치
     * @param multivalidate 하나씩 확인할지 여부, 기본 단일 행 오류는 즉시 ExcelException을 throw하고 일괄 확인의 경우 true, ExcelImportResult.hasErrors 및 getErrors가 특정 오류 메시지를받습니다.
     * @return
     * @throws Exception
     */
    public ExcelImportResult readExcel(String id, int titleIndex,InputStream excelStream,Integer sheetIndex,boolean multivalidate) throws Exception {
        //등록 정보에서 빈 정보 얻기
        ExcelDefinition excelDefinition = definitionReader.getRegistry().get(id);
        if(excelDefinition==null){
            throw new ExcelException("["+id+"]에 대한 구성 정보가 없습니다.");
        }
        return doReadExcel(excelDefinition,titleIndex,excelStream,sheetIndex,multivalidate);
    }

    protected ExcelImportResult doReadExcel(ExcelDefinition excelDefinition,int titleIndex,InputStream excelStream,Integer sheetIndex,boolean multivalidate) throws Exception {
        Workbook workbook = WorkbookFactory.create(excelStream);
        ExcelImportResult result = new ExcelImportResult();
        //시트, SheetIndex 매개 변수 우선 순위가 ExcelDefinition 구성 sheetIndex보다 큽니다.
        Sheet sheet = workbook.getSheetAt(sheetIndex==null?excelDefinition.getSheetIndex():sheetIndex);
        //데이터 처리 전에 제목
        List<List<Object>> header = readHeader(excelDefinition, sheet,titleIndex);
        result.setHeader(header);
        //제목 가져 오기
        List<String> titles = readTitle(excelDefinition,sheet,titleIndex);
        //Bean 가져 오기
        List<Object> listBean = readRows(result.getErrors(),excelDefinition,titles, sheet,titleIndex,multivalidate);
        result.setListBean(listBean);
        return result;
    }

    /**
     * ExcelDefinition에서 titleIndex가 0이 아닌 경우 제목 앞에 내용 파싱
     * @param excelDefinition
     * @param sheet
     * @return
     */
    protected List<List<Object>> readHeader(ExcelDefinition excelDefinition,Sheet sheet,int titleIndex){
        List<List<Object>> header = null;
        if(titleIndex!=0){
            header = new ArrayList<List<Object>>(titleIndex);
            for(int i=0;i<titleIndex;i++){
                Row row = sheet.getRow(i);
                short cellNum = row.getLastCellNum();
                List<Object> item = new ArrayList<Object>(cellNum);
                for(int j=0;j<cellNum;j++){
                    Cell cell = row.getCell(j);
                    Object value = getCellValue(cell);
                    item.add(value);
                }
                header.add(item);
            }
        }
        return header;
    }

    /**
     * 여러 줄 읽기
     * @param result
     * @param excelDefinition
     * @param titles
     * @param sheet
     * @param titleIndex
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    protected <T> List<T> readRows(List<ExcelError> errors,ExcelDefinition excelDefinition, List<String> titles, Sheet sheet,int titleIndex,boolean multivalidate)throws Exception {
        int rowNum = sheet.getLastRowNum();
        //전체 읽기 데이터 수
        int totalNum = rowNum - titleIndex;
        int startRow =  -titleIndex;
        List<T> listBean = new ArrayList<T>(totalNum);
        for (int i = titleIndex+1; i <= rowNum; i++) {
            Row row = sheet.getRow(i);
            Object bean = readRow(errors,excelDefinition,row,titles,startRow+i,multivalidate);
            listBean.add((T) bean);
        }
        return listBean;
    }

    /**
     * 한 줄 읽기
     * @param excelDefinition
     * @param row
     * @param titles
     * @param rowNum 처음 몇 줄
     * @return
     * @throws Exception
     */
    protected Object readRow(List<ExcelError> errors,ExcelDefinition excelDefinition, Row row, List<String> titles,int rowNum,boolean multivalidate) throws Exception {
        //등록 된 bean 유형 생성하기
        Object bean = ReflectUtil.newInstance(excelDefinition.getClazz());
        for(FieldValue fieldValue:excelDefinition.getFieldValues()){
            String title = fieldValue.getTitle();
            for (int j = 0; j < titles.size(); j++) {
                if(title.equals(titles.get(j))){
                    try{
                        Cell cell = row.getCell(j);
                        //Excel 기본 값의 가치 얻기
                        Object value = getCellValue(cell);
                        //확인
                        validate(fieldValue, value, rowNum);
                        if(value != null){
                            if(value instanceof String){
                                //앞뒤 공간 제거
                                value = value.toString().trim();
                            }
                            value = super.convert(bean,value, fieldValue, Type.IMPORT,rowNum);
                            ReflectUtil.setProperty(bean, fieldValue.getName(), value);
                        }
                        break;
                    }catch(ExcelException e){
                        //multivalidate 적용
                        if(multivalidate){
                            errors.add(new ExcelError(rowNum,e.getMessage()));
                            continue;
                        }else{
                            errors.add(new ExcelError(rowNum,e.getMessage()));
                            throw e;
                        }
                    }
                }
            }
        }
        return bean;
    }

    protected List<String> readTitle(ExcelDefinition excelDefinition, Sheet sheet,int titleIndex) {
        // 获取Excel标题数据
        Row hssfRowTitle = sheet.getRow(titleIndex);
        int cellNum = hssfRowTitle.getLastCellNum();
        List<String> titles = new ArrayList<String>(cellNum);
        // 获取标题数据
        for (int i = 0; i < cellNum; i++) {
            Cell cell = hssfRowTitle.getCell(i);
            Object value = getCellValue(cell);
            if(value==null){
                throw new ExcelException("id 为:["+excelDefinition.getId()+"]的标题不能为[ null ]");
            }
            titles.add(value.toString());
        }
        return titles;
    }

    /**
     * 数据有效性校验
     * @param fieldValue
     * @param value
     * @param rowNum
     */
    private void validate(FieldValue fieldValue,Object value,int rowNum){
        if(value == null || StringUtils.isBlank(value.toString())){
            //空校验
            if(!fieldValue.isNull()){
                String err = getErrorMsg(fieldValue, "不能为空", rowNum);
                throw new ExcelException(err);
            }
        }else{
            //正则校验
            String regex = fieldValue.getRegex();
            if(StringUtils.isNotBlank(regex)){
                String val = value.toString().trim();
                if(!val.matches(regex)){
                    String errMsg = fieldValue.getRegexErrMsg()==null?"格式错误":fieldValue.getRegexErrMsg();
                    String err = getErrorMsg(fieldValue, errMsg, rowNum);
                    throw new ExcelException(err);
                }
            }
        }
    }


}
