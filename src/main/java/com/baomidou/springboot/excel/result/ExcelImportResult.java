package com.baomidou.springboot.excel.result;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.baomidou.springboot.excel.parsing.ExcelError;

/**
 * Excel 결과 가져 오기
 *
 * @author lisuo
 *
 */
public class ExcelImportResult {

    /** 헤더 정보, 제목 줄 앞의 데이터, 각 행은 List<Object>를 나타내며, 각 Object는 셀 값을 보유합니다. */
    private List<List<Object>> header = null;

    /** 아래 헤더 행의 데이터를 파싱하는 JavaBean 콜렉션 */
    private List<?> listBean;

    /** Errors */
    private List<ExcelError> errors = new ArrayList<ExcelError>();

    public List<List<Object>> getHeader() {
        return header;
    }

    public void setHeader(List<List<Object>> header) {
        this.header = header;
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getListBean() {
        return (List<T>) listBean;
    }

    public void setListBean(List<?> listBean) {
        this.listBean = listBean;
    }

    public List<ExcelError> getErrors() {
        return errors;
    }

    /**
     * 가져 오기에 오류가 있습니다.
     * @return true : 오류, false : 오류 없음
     */
    public boolean hasErrors(){
        return CollectionUtils.isNotEmpty(errors);
    }

}
