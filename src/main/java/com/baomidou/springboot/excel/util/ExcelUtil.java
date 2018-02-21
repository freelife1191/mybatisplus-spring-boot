package com.baomidou.springboot.excel.util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.NumberToTextConverter;

/**
 * Excel 운영 도구
 * @author lisuo
 *
 */
public abstract class ExcelUtil {

    /**
     * 엑셀 읽기, 불규칙 엑셀 파일 지원,
     * 외부 목록은 모든 데이터 라인, 내부 목록 셀 라인 셀 데이터 위치
     * Excel 데이터의 세 번째 줄에있는 두 번째 셀인 예제 코드를 얻는다 고 가정합니다.
     * FileInputStream excelStream = new FileInputStream(path);
     * List<List<Object>> list = ExcelUtil.readExcel(excelStream);
     * System.out.println(list.get(2).get(1));//두 번째 열의 세 번째 줄, 인덱스 줄 위치는 2, 열 인덱스 위치는 1입니다.
     * @param excelStream Excel 파일 스트림
     * @param sheetIndex Excel-Sheet 색인
     * @return List<List<Object>>
     * @throws Exception
     */
    public static List<List<Object>> readExcel(InputStream excelStream,int sheetIndex)throws Exception {
        List<List<Object>> datas = new ArrayList<List<Object>>();
        Workbook workbook = WorkbookFactory.create(excelStream);
        //첫 번째 sheet 만 읽으십시오.
        Sheet sheet = workbook.getSheetAt(sheetIndex);
        int rows = sheet.getPhysicalNumberOfRows();
        for (int i = 0; i < rows; i++) {
            Row row = sheet.getRow(i);
            short cellNum = row.getLastCellNum();
            List<Object> item = new ArrayList<Object>(cellNum);
            for(int j=0;j<cellNum;j++){
                Cell cell = row.getCell(j);
                Object value = ExcelUtil.getCellValue(cell);
                item.add(value);
            }
            datas.add(item);
        }
        return datas;
    }

    /**
     * 읽기 엑셀, 불규칙 엑셀 파일에 대한 지원, 기본은 첫 번째 시트 페이지를 읽습니다
     * 외부 목록은 모든 데이터 라인, 내부 목록 셀 라인 셀 데이터 위치
     * Excel 데이터의 세 번째 줄에있는 두 번째 셀인 예제 코드를 얻는다 고 가정합니다.
     * FileInputStream excelStream = new FileInputStream(path);
     * List<List<Object>> list = ExcelUtil.readExcel(excelStream);
     * System.out.println(list.get(2).get(1));//두 번째 열의 세 번째 줄, 인덱스 줄 위치는 2, 열 인덱스 위치는 1입니다.
     * @param excelStream Excel文件流
     * @return List<List<Object>>
     * @throws Exception
     */
    public static List<List<Object>> readExcel(InputStream excelStream)throws Exception {
        return readExcel(excelStream,0);
    }

    /**
     * 셀 값 설정
     *
     * @param cell
     * @param value
     */
    public static void setCellValue(Cell cell, Object value) {
        if (value != null) {
            if (value instanceof String) {
                cell.setCellValue((String) value);
            } else if (value instanceof Number) {
                cell.setCellValue(Double.parseDouble(String.valueOf(value)));
            } else if (value instanceof Boolean) {
                cell.setCellValue((Boolean) value);
            } else if (value instanceof Date) {
                cell.setCellValue((Date) value);
            } else {
                cell.setCellValue(value.toString());
            }
        }
    }

    /**
     * 셀 값 가져 오기
     *
     * @param cell
     * @return
     */
    public static Object getCellValue(Cell cell) {
        Object value = null;
        if (null != cell) {
            switch (cell.getCellType()) {
                // 공란
                case Cell.CELL_TYPE_BLANK:
                    break;
                    // Boolean
                case Cell.CELL_TYPE_BOOLEAN:
                    value = cell.getBooleanCellValue();
                    break;
                    // 잘못된 형식
                case Cell.CELL_TYPE_ERROR:
                    break;
                    // 수식
                case Cell.CELL_TYPE_FORMULA:
                    Workbook wb = cell.getSheet().getWorkbook();
                    CreationHelper crateHelper = wb.getCreationHelper();
                    FormulaEvaluator evaluator = crateHelper.createFormulaEvaluator();
                    value = getCellValue(evaluator.evaluateInCell(cell));
                    break;
                    // 숫자타입
                case Cell.CELL_TYPE_NUMERIC:
                    // 처리 날짜 형식
                    if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                        value = cell.getDateCellValue();
                    } else {
                        value = NumberToTextConverter.toText(cell.getNumericCellValue());
                    }
                    break;
                case Cell.CELL_TYPE_STRING:
                    value = cell.getRichStringCellValue().getString();
                    break;
                default:
                    value = null;
            }
        }
        return value;
    }
}
