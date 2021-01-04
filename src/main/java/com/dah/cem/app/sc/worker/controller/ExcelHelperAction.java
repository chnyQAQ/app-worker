package com.dah.cem.app.sc.worker.controller;

import com.dah.cem.app.sc.worker.domain.target.BaseTarget;
import com.dah.cem.app.sc.worker.domain.target.BaseTargetService;
import com.dah.cem.app.sc.worker.infrastructure.exception.ServiceException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ExcelHelperAction {

    @Autowired
    private BaseTargetService baseTargetService;

    private Map<String, String> unitCodes = new HashMap<>();

    private int totalRows = 0;

    private int totalCells = 0;

    public void readExcelFile(MultipartFile file) throws ParseException {
        if (file == null || file.getSize() == 0) {
            throw new ServiceException("操作失败，文件错误！");
        }
        List<BaseTarget> list = getExcelInfo(file);
        for (String unitCode : unitCodes.keySet()) {
            baseTargetService.removeByUnitCode(unitCode);
        }
        unitCodes = new HashMap<>();
        if (list != null && !list.isEmpty()) {
            for (BaseTarget baseTarget : list) {
                baseTargetService.save(baseTarget);
            }
        }
    }

    public List<BaseTarget> getExcelInfo(MultipartFile file) throws ParseException {
        List<BaseTarget> list = new ArrayList<>();
        String fileName = file.getOriginalFilename();
        try {
            if (!validataExcel(fileName)) {
                return null;
            }
            boolean isExcel2003 = true;
            if (isExcel2007(fileName)) {
                isExcel2003 = false;
            }
            list = createExcel(file.getInputStream(), isExcel2003);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<BaseTarget> createExcel(InputStream inputStream, boolean isExcel2003) throws ParseException {
        List<BaseTarget> list = new ArrayList<>();
        try {
            Workbook workbook = null;
            if (isExcel2003) {
                workbook = new HSSFWorkbook(inputStream);
            } else {
                workbook = new XSSFWorkbook(inputStream);
            }
            list = readExcelValue(workbook);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<BaseTarget> readExcelValue(Workbook wb) throws ParseException {
        Sheet sheet = wb.getSheetAt(0);
        this.totalRows = sheet.getPhysicalNumberOfRows();
        if (totalRows > 1 && sheet.getRow(0) != null) {
            this.totalCells = sheet.getRow(0).getPhysicalNumberOfCells();
        }
        List<BaseTarget> list = new ArrayList<>();
        for (int i = 1; i < totalRows; i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }
            BaseTarget baseTarget = new BaseTarget();
            for (int c = 0; c < this.totalCells; c++) {
                Cell cell = row.getCell(c);
                if (cell != null) {
                    cell.setCellType(CellType.STRING);
                    if (c == 0) {
                        if (!StringUtils.isEmpty(cell.getStringCellValue())) {
                            baseTarget.setProbeCode(cell.getStringCellValue());
                        } else {
                            break;
                        }
                    } else if (c == 1) {
                        if (!StringUtils.isEmpty(cell.getStringCellValue())) {
                            baseTarget.setEquipCode(cell.getStringCellValue());
                        } else {
                            break;
                        }
                    } else if (c == 2) {
                        if (!StringUtils.isEmpty(cell.getStringCellValue())) {
                            baseTarget.setTargetCode(cell.getStringCellValue());
                        }
                    } else if (c == 3) {
                        if (!StringUtils.isEmpty(cell.getStringCellValue())) {
                            baseTarget.setGatewayCode(cell.getStringCellValue());
                        }
                    } else if (c == 4) {
                        if (!StringUtils.isEmpty(cell.getStringCellValue())) {
                            baseTarget.setFrequncey(Integer.valueOf(cell.getStringCellValue()));
                        }
                    } else if (c == 5) {
                        if (!StringUtils.isEmpty(cell.getStringCellValue())) {
                            baseTarget.setPointCode(cell.getStringCellValue());
                        }
                    } else if (c == 6) {
                        if (!StringUtils.isEmpty(cell.getStringCellValue())) {
                            baseTarget.setPointName(cell.getStringCellValue());
                        }
                    }  else if (c == 7) {
                        if (!StringUtils.isEmpty(cell.getStringCellValue())) {
                            String unitCode = cell.getStringCellValue();
                            baseTarget.setUnitCode(unitCode);
                            unitCodes.put(unitCode, unitCode);
                        }
                    } else if (c == 8) {
                        if (!StringUtils.isEmpty(cell.getStringCellValue())) {
                            baseTarget.setUnitName(cell.getStringCellValue());
                        }
                    }
                }
            }
            list.add(baseTarget);
        }
        return list;

    }

    public boolean validataExcel(String filePath) {
        return filePath != null && (isExcel2003(filePath) || isExcel2007(filePath));
    }

    public static boolean isExcel2003(String filePath) {
        return filePath.matches("^.+\\.(?i)(xls)$");
    }

    public static boolean isExcel2007(String filePath) {
        return filePath.matches("^.+\\.(?i)(xlsx)$");
    }

}
