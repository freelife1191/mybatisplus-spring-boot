package com.baomidou.springboot.excel;


import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.poi.ss.usermodel.Workbook;

import com.baomidou.springboot.excel.config.ExcelDefinition;
import com.baomidou.springboot.excel.config.FieldValue;
import com.baomidou.springboot.excel.exception.ExcelException;
import com.baomidou.springboot.excel.parsing.ExcelExport;
import com.baomidou.springboot.excel.parsing.ExcelHeader;
import com.baomidou.springboot.excel.parsing.ExcelImport;
import com.baomidou.springboot.excel.result.ExcelExportResult;
import com.baomidou.springboot.excel.result.ExcelImportResult;
import com.baomidou.springboot.excel.util.ReflectUtil;
import com.baomidou.springboot.excel.xml.XMLExcelDefinitionReader;

/**
 * Excel 컨텍스트 지원, 위치 구성 파일 경로를 지정하면 사용할 수 있습니다.
 *
 * @author lisuo
 */
public class ExcelContext {

    private ExcelDefinitionReader definitionReader;

    /**
     * Excel 구성을 캐시하는 데 사용됩니다.
     */
    private Map<String, List<FieldValue>> fieldValueMap = new HashMap<String, List<FieldValue>>();

    /**
     * 내보내기
     */
    private ExcelExport excelExport;
    /**
     * 가져 오기
     */
    private ExcelImport excelImport;

    /**
     * @param location 프로필 클래스 경로
     */
    public ExcelContext(String location) {
        try {
            //기본값은 XMLExcelDefinitionReader를 사용하는 것입니다.
            definitionReader = new XMLExcelDefinitionReader(location);
            excelExport = new ExcelExport(definitionReader);
            excelImport = new ExcelImport(definitionReader);
        } catch (ExcelException e) {
            throw e;
        } catch (Exception e) {
            throw new ExcelException(e);
        }
    }

    /**
     * @param definitionReader 사용자 지정 구현 ExcelDefinitionReader
     */
    public ExcelContext(ExcelDefinitionReader definitionReader) {
        try {
            if (definitionReader == null) {
                throw new ExcelException("definitionReader 비워 둘 수 없다.");
            }
            if (MapUtils.isEmpty(this.definitionReader.getRegistry())) {
                throw new ExcelException("definitionReader Registry 비워 둘 수 없다.");
            }
            this.definitionReader = definitionReader;
            excelExport = new ExcelExport(definitionReader);
            excelImport = new ExcelImport(definitionReader);
        } catch (ExcelException e) {
            throw e;
        } catch (Exception e) {
            throw new ExcelException(e);
        }

    }

    /**
     * Excel 만들기
     *
     * @param id    구성 ID
     * @param beans 구성 class 해당 List
     * @return Workbook
     * @throws Exception
     */
    public Workbook createExcel(String id, List<?> beans) throws Exception {
        return createExcel(id, beans, null, null);
    }

    /**
     * Excel 섹션 정보 만들기
     *
     * @param id    구성 ID
     * @param beans 구성 class 해당 List
     * @return Workbook
     * @throws Exception
     */
    public ExcelExportResult createExcelForPart(String id, List<?> beans) throws Exception {
        return createExcelForPart(id, beans, null, null);
    }

    /**
     * Excel 만들기
     *
     * @param id     구성 ID
     * @param beans  구성 class 해당 List
     * @param header 내보내기 전에 문서 설명 추가와 같이 머리글 앞에서 추가 작업을 수행하십시오.이 작업은 null 일 수 있습니다
     * @return Workbook
     * @throws Exception
     */
    public Workbook createExcel(String id, List<?> beans, ExcelHeader header) throws Exception {
        return createExcel(id, beans, header, null);
    }

    /**
     * Excel 섹션 정보 만들기
     *
     * @param id     구성 ID
     * @param beans  구성 class 해당 List
     * @param header 내보내기 전에 문서 설명 추가와 같이 머리글 앞에서 추가 작업을 수행하십시오.이 작업은 null 일 수 있습니다
     * @return Workbook
     * @throws Exception
     */
    public ExcelExportResult createExcelForPart(String id, List<?> beans, ExcelHeader header) throws Exception {
        return createExcelForPart(id, beans, header, null);
    }

    /**
     * Excel 만들기
     *
     * @param id     구성 ID
     * @param beans  구성 class 해당 List
     * @param header 내보내기 전에 문서 설명 추가와 같이 머리글 앞에서 추가 작업을 수행하십시오.이 작업은 null 일 수 있습니다
     * @param fields Excel 내보내기 필드 (bean 해당 필드 이름)를 지정합니다. null 일 수 있습니다.
     * @return Workbook
     * @throws Exception
     */
    public Workbook createExcel(String id, List<?> beans, ExcelHeader header, List<String> fields) throws Exception {
        return excelExport.createExcel(id, beans, header, fields).build();
    }

    /**
     * Excel 섹션 정보 만들기
     *
     * @param id     구성 ID
     * @param beans  구성 class 해당 List
     * @param header 내보내기 전에 문서 설명 추가와 같이 머리글 앞에서 추가 작업을 수행하십시오.이 작업은 null 일 수 있습니다
     * @param fields Excel 내보내기 필드 (bean 해당 필드 이름)를 지정합니다. null 일 수 있습니다.
     * @return Workbook
     * @throws Exception
     */
    public ExcelExportResult createExcelForPart(String id, List<?> beans, ExcelHeader header, List<String> fields) throws Exception {
        return excelExport.createExcel(id, beans, header, fields);
    }

    /**
     * Excel, 템플릿 정보 만들기
     *
     * @param id     ExcelXML 구성 bean의 ID
     * @param header Excel 헤더 정보 (제목 앞)
     * @param fields 내 보낸 필드 지정
     * @return
     * @throws Exception
     */
    public Workbook createExcelTemplate(String id, ExcelHeader header, List<String> fields) throws Exception {
        return excelExport.createExcelTemplate(id, header, fields);
    }

    /***
     * Excel 정보 읽기
     * @param id 구성 ID
     * @param excelStream Excel 파일 스트림
     * @return ExcelImportResult
     * @throws Exception
     */
    public ExcelImportResult readExcel(String id, InputStream excelStream) throws Exception {
        return excelImport.readExcel(id, 0, excelStream, null, false);
    }

    /***
     * Excel 정보 읽기
     * @param id 구성 ID
     * @param excelStream Excel 파일 스트림
     * @param sheetIndex Sheet색인 비트
     * @return ExcelImportResult
     * @throws Exception
     */
    public ExcelImportResult readExcel(String id, InputStream excelStream, int sheetIndex) throws Exception {
        return excelImport.readExcel(id, 0, excelStream, sheetIndex, false);
    }

    /***
     * Excel 정보 읽기
     * @param id 구성 ID
     * @param titleIndex 0에서 시작하는 제목 색인
     * @param excelStream Excel 파일 스트림
     * @return ExcelImportResult
     * @throws Exception
     */
    public ExcelImportResult readExcel(String id, int titleIndex, InputStream excelStream) throws Exception {
        return excelImport.readExcel(id, titleIndex, excelStream, null, false);
    }

    /***
     * Excel 정보 읽기
     * @param id 구성 ID
     * @param titleIndex 0에서 시작하는 제목 색인
     * @param excelStream Excel 파일 스트림
     * @param multivalidate 하나씩 확인할지 여부, 기본 단일 행 오류는 즉시 ExcelException을 throw하고 일괄 확인의 경우 true, ExcelImportResult.hasErrors 및 getErrors가 특정 오류 메시지를받습니다.
     * @return ExcelImportResult
     * @throws Exception
     */
    public ExcelImportResult readExcel(String id, int titleIndex, InputStream excelStream, boolean multivalidate) throws Exception {
        return excelImport.readExcel(id, titleIndex, excelStream, null, multivalidate);
    }

    /***
     *Excel 정보 읽기
     * @param id 구성 ID
     * @param titleIndex 0에서 시작하는 제목 색인
     * @param excelStream Excel 파일 스트림
     * @param sheetIndex Sheet색인 비트
     * @return ExcelImportResult
     * @throws Exception
     */
    public ExcelImportResult readExcel(String id, int titleIndex, InputStream excelStream, int sheetIndex) throws Exception {
        return excelImport.readExcel(id, titleIndex, excelStream, sheetIndex, false);
    }

    /***
     * Excel 정보 읽기
     * @param id 配置ID
     * @param titleIndex 0에서 시작하는 제목 색인
     * @param excelStream Excel 파일 스트림
     * @param sheetIndex Sheet색인 비트
     * @param multivalidate 하나씩 확인할지 여부, 기본 단일 행 오류는 즉시 ExcelException을 throw하고 일괄 확인의 경우 true, ExcelImportResult.hasErrors 및 getErrors가 특정 오류 메시지를받습니다.
     * @return ExcelImportResult
     * @throws Exception
     */
    public ExcelImportResult readExcel(String id, int titleIndex, InputStream excelStream, int sheetIndex, boolean multivalidate) throws Exception {
        return excelImport.readExcel(id, titleIndex, excelStream, sheetIndex, multivalidate);
    }

    /**
     *Excel 구성 파일에서 필드 가져 오기
     *
     * @param key
     * @return
     */
    public List<FieldValue> getFieldValues(String key) {
        List<FieldValue> list = fieldValueMap.get(key);
        if (list == null) {
            ExcelDefinition def = definitionReader.getRegistry().get(key);
            if (def == null) {
                throw new ExcelException("[" + key + "]에 대한 구성 정보가 없습니다.");
            }
            //사용자가 기본 구성 정보를 수정하지 못하게하려면 copy 메소드를 사용하십시오.
            List<FieldValue> fieldValues = def.getFieldValues();
            list = new ArrayList<FieldValue>(fieldValues.size());
            for (FieldValue fieldValue : fieldValues) {
                FieldValue val = new FieldValue();
                ReflectUtil.copyProps(fieldValue, val);
                list.add(val);
            }
            fieldValueMap.put(key, list);
        }
        return list;
    }

}
