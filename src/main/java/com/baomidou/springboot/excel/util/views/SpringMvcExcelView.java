package com.baomidou.springboot.excel.util.views;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.AbstractView;

import com.baomidou.springboot.excel.util.ExcelDownLoadUtil;

/**
 * SpringMvc Excel 다운로드보기
 * 유스 케이스, 표준 Spring 커스텀 뷰, 뷰는 두 개의 파라미터를 필요로한다.
 * Excel.excelName String
 * Excel.workbook POI Workbook
 *
 * 예제 코드：
 * public ModelAndView downloadExcel(){
 * 		//1.ExcelContent를 사용하여 비즈니스 논리를 실행하여 데이터를 가져와 Workbook 생성
 * 		Workbook workbook = ...;
 * 		//2.Excel 다운로드보기로 이동
 * 		ModelAndView view = new ModelAndView("springMvcExcelView");
 * 		view.addObject(SpringMvcExcelView.EXCEL_NAME,"Excel 다운로드 테스트");
 * 		view.addObject(SpringMvcExcelView.EXCEL_WORKBOOK,workbook);
 * 		view.addObject(SpringMvcExcelView.EXCEL_EMPTY_MESSAGE,"XXX관련 데이터를 내보낼 수 없습니다.");
 * 		return view;
 * }
 *
 */
public class SpringMvcExcelView extends AbstractView {

    /** Excel 이름 */
    public static final String EXCEL_NAME = "Excel.excelName";

    /** POI Workbook */
    public static final String EXCEL_WORKBOOK = "Excel.workbook";

    /** 데이터가 없을 때 확인 */
    public static final String EXCEL_EMPTY_MESSAGE = "Excel.emptyMessage";

    @Override
    protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        Workbook workbook = (Workbook) model.get(EXCEL_WORKBOOK);
        String excelName = MapUtils.getString(model, EXCEL_NAME);
        String emptyMessage = MapUtils.getString(model, EXCEL_EMPTY_MESSAGE);
        if(StringUtils.isBlank(emptyMessage)){
            emptyMessage="내보낼 수있는 데이터가 없습니다.";
        }
        ExcelDownLoadUtil.downLoadExcel(workbook, excelName,emptyMessage, request, response);
    }

}