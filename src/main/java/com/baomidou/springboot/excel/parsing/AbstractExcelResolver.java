package com.baomidou.springboot.excel.parsing;


import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.poi.ss.usermodel.Cell;

import com.baomidou.springboot.excel.ExcelDefinitionReader;
import com.baomidou.springboot.excel.config.FieldValue;
import com.baomidou.springboot.excel.exception.ExcelException;
import com.baomidou.springboot.excel.util.ExcelUtil;
import com.baomidou.springboot.excel.util.ReflectUtil;
import com.baomidou.springboot.excel.util.SpringUtil;

/**
 * Excel 추상 구문 분석기
 *
 * @author lisuo
 *
 */
public abstract class AbstractExcelResolver implements CellValueConverter{

    protected ExcelDefinitionReader definitionReader;

    /** 등록 필드 해결 정보 */
    private Map<String,CellValueConverter> cellValueConverters = new HashMap<String, CellValueConverter>();

    public AbstractExcelResolver(ExcelDefinitionReader definitionReader) {
        this.definitionReader = definitionReader;
    }

    /**
     * 표현 형식 속성을 구문 분석합니다.
     *
     * @param value
     * @param format
     * @param fieldValue
     * @param rowNum
     * @return
     */
    protected String resolverExpression(String value, String format, Type type,FieldValue fieldValue,int rowNum) {
        try {
            String[] expressions = StringUtils.split(format, ",");
            for (String expression : expressions) {
                String[] val = StringUtils.split(expression, ":");
                String v1 = val[0];
                String v2 = val[1];
                if (Type.EXPORT == type) {
                    if (value.equals(v1)) {
                        return v2;
                    }
                } else if (Type.IMPORT == type) {
                    if (value.equals(v2)) {
                        return v1;
                    }
                }
            }
        } catch (Exception e) {
            throw new ExcelException(getErrorMsg(fieldValue, "표현:" + format + "틀린, 올바른 형식은 [,] 분리, [:] 값이어야합니다.", rowNum));
        }
        throw new ExcelException(getErrorMsg(fieldValue, "["+value+"]잘못된 값", rowNum));
    }

    /**
     * 셀 값 설정
     *
     * @param cell
     * @param value
     */
    protected void setCellValue(Cell cell, Object value) {
        ExcelUtil.setCellValue(cell, value);
    }

    /**
     * 셀 값 가져 오기
     *
     * @param cell
     * @return
     */
    protected Object getCellValue(Cell cell) {
        return ExcelUtil.getCellValue(cell);
    }

    //기본 구현
    @Override
    public Object convert(Object bean,Object value, FieldValue fieldValue, Type type,int rowNum) throws Exception {
        if(value !=null){
            //paser가 달성하기 위해 데이터를 읽습니다.
            String convName = fieldValue.getCellValueConverterName();
            if(convName==null){
                //행정상 기본
                String name = fieldValue.getName();
                String pattern = fieldValue.getPattern();
                String format = fieldValue.getFormat();
                DecimalFormat decimalFormat = fieldValue.getDecimalFormat();
                if (StringUtils.isNotBlank(pattern)) {
                    String [] patterns = StringUtils.split(pattern, ",");
                    if (Type.EXPORT == type) {
                        //내보내기는 첫 번째 패턴을 사용합니다.
                        return DateFormatUtils.format((Date) value, patterns[0]);
                    } else if (Type.IMPORT == type) {
                        if (value instanceof String) {
                            Date date = DateUtils.parseDate((String) value, patterns);
                            if(date==null){
                                StringBuilder errMsg = new StringBuilder("[");
                                errMsg.append(value.toString()).append("]")
                                .append("날짜로 변환 할 수 없으므로 올바른 형식이어야합니다.:[").append(pattern+"]");
                                String err = getErrorMsg(fieldValue, errMsg.toString(), rowNum);
                                throw new ExcelException(err);
                            }
                            return date;
                        } else if (value instanceof Date) {
                            return value;
                        } else if(value instanceof Number){
                            Number val = (Number) value;
                            return new Date(val.longValue());
                        } else {
                            throw new ExcelException(getErrorMsg(fieldValue, "데이터 형식 오류,[ " + name + " ]유형은입니다.:" + value.getClass()+",현재 날짜로 변환 할 수 없습니다.", rowNum));
                        }
                    }
                } else if (format != null) {
                    return resolverExpression(value.toString(), format, type,fieldValue,rowNum);
                } else if (decimalFormat!=null) {
                    if (Type.IMPORT == type) {
                        if(value instanceof String){
                            return decimalFormat.parse(value.toString());
                        }
                    }else if(Type.EXPORT == type){
                        if(value instanceof String){
                            value = ConvertUtils.convert(value, BigDecimal.class);
                        }
                        return decimalFormat.format(value);
                    }
                } else {
                    return value;
                }
            }else{
                //맞춤
                CellValueConverter conv = cellValueConverters.get(convName);
                if(conv == null){
                    synchronized(this){
                        if(conv == null){
                            conv = getBean(convName);
                            cellValueConverters.put(convName, conv);
                        }
                    }
                    conv = cellValueConverters.get(convName);
                }
                value = conv.convert(bean,value, fieldValue, type, rowNum);
            }
        }
        return fieldValue.getDefaultValue();

    }

    //bean 가져 오기
    private CellValueConverter getBean(String convName) throws ClassNotFoundException {
        CellValueConverter bean = null;
        if(SpringUtil.isInited()){
            bean = (CellValueConverter) SpringUtil.getBean(Class.forName(convName));
        }else{
            bean =  (CellValueConverter) ReflectUtil.newInstance(Class.forName(convName));
        }
        return bean;
    }


    /**
     * 오류 메시지 가져 오기
     * @param fieldValue
     * @param errMsg 메시지 프롬프트 내용
     * @param rowNum
     * @return
     */
    protected String getErrorMsg(FieldValue fieldValue,String errMsg,int rowNum){
        StringBuilder err = new StringBuilder();
        err.append("아니[").append(rowNum).append("확인],[")
        .append(fieldValue.getTitle()).append("]").append(errMsg);
        return err.toString();
    }

}
