package com.baomidou.springboot.excel.parsing;

/**
 * Excel 가져올 때 오류 메시지가 표시됩니다.
 * @author lisuo
 */
public class ExcelError {

    /** 처음 몇 줄 */
    private int row;
    /** 오류 메시지 */
    private String errorMsg;

    public ExcelError(int row,String errorMsg) {
        this.row = row;
        this.errorMsg = errorMsg;
    }

    public int getRow() {
        return row;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

}
