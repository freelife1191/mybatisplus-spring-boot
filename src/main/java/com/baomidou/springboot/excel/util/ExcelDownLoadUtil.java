package com.baomidou.springboot.excel.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Workbook;

/**
 * Excel 원시 Servlet 다운로드 환경을 제공하는 도구 다운로드
 * @author lisuo
 *
 */
public class ExcelDownLoadUtil {

    /** 파일 접미사 */
    public static final String FILE_SUFFIX = ".xlsx";

    /** 문서 인코딩 */
    public static final String UTF8 = "UTF-8";
    /** 사용자 브라우저 키워드：IE */
    private static final String USER_AGENT_IE = "MSIE";

    private static final String CONTENT_TYPE = "application/vnd.ms-excel";

    /**
     * Workbook이 비어 있으면 Excel을 다운로드하고 Alert를 실행합니다.(emptyMessage);
     * @param workbook POI Workbook
     * @param excelName Excel 이름 (접미사 없음, 중국어 처리 지원)
     * @param
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws IOException
     */
    public static void downLoadExcel(Workbook workbook,String excelName,String emptyMessage,HttpServletRequest request,HttpServletResponse response)throws IOException{
        if (workbook != null) {
            String excelFileName = encodeDownloadFileName(request, excelName + FILE_SUFFIX);
            response.setContentType(CONTENT_TYPE);
            response.setHeader("Content-Disposition", "attachment; filename=\"" + excelFileName + "\";target=_blank");
            OutputStream out = response.getOutputStream();
            workbook.write(out);
            out.flush();
            out.close();
        } else {
            response.setContentType("text/html; charset=utf-8");
            PrintWriter writer = response.getWriter();
            writer.print("<script language='javascript'>alert('"+emptyMessage+"');</script>");
            writer.flush();
            writer.close();
        }
    }

    /**
     * 다른 브라우저 설정에 따라 파일 이름 인코딩 다운로드
     *
     * @param request
     * @param fileName
     * @return 파일 이름
     */
    public static String encodeDownloadFileName(HttpServletRequest request, String fileName) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent.indexOf(USER_AGENT_IE) > 0) {// 사용자가 IE를 사용 중입니다.
            try {
                return URLEncoder.encode(fileName, UTF8);
            } catch (UnsupportedEncodingException ignore) {}
        } else {
            try {
                return new String(fileName.getBytes(UTF8), "ISO-8859-1");
            } catch (UnsupportedEncodingException ignore) {
            }
        }
        return fileName;
    }

}
