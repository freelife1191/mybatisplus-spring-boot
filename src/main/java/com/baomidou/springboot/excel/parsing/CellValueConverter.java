package com.baomidou.springboot.excel.parsing;


import com.baomidou.springboot.excel.config.FieldValue;

/**
 * CellValue 변환기, 셀 값 해결 규칙
 * @author lisuo
 *
 */
public interface CellValueConverter {

    /**
     * 운영 유형, 가져 오기 또는 내보내기 유형
     */
    enum Type {
        EXPORT, IMPORT
    }

    /**
     * 셀 값 변환
     * @param bean Excel로 구성된 JavaBean 객체
     * @param value Excel 원래 값
     * @param fieldValue FieldValue 정보
     * @param type 가져 오기 또는 내보내기
     * @param rowNum 줄 번호
     * @return 결과에 해당하는 값 분석
     * @throws Exception
     */
    public Object convert(Object bean, Object value, FieldValue fieldValue, Type type, int rowNum) throws Exception;
}
