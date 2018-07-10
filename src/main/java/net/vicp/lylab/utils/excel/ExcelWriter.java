package net.vicp.lylab.utils.excel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Objects;

import net.vicp.lylab.core.exceptions.LYException;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelWriter implements AutoCloseable {
	private OutputStream outputStream;
	private Workbook workbook;

	public ExcelWriter(String filePath) {
		Objects.requireNonNull(filePath);
		File outputFile = new File(filePath);
		try {
			outputStream = new FileOutputStream(outputFile);
		} catch (FileNotFoundException e) {
			throw new LYException("无法创建文件并写入？", e);
		}
		// 获取Excel文件对象
		if (filePath.endsWith("xls")) {
			workbook = new HSSFWorkbook();
		} else {
			workbook = new XSSFWorkbook();
		}
	}

	public void writeExcelContent(String value, int rowNo, int columnNo) {
		writeExcelContent(value, 0, rowNo, columnNo);
	}

	public void writeExcelContent(String value, int sheetNo, int rowNo, int columnNo) {
		try {
			while (workbook.getNumberOfSheets() <= sheetNo) {
				workbook.createSheet(String.valueOf(sheetNo));
			}
			Sheet sheet = workbook.getSheetAt(sheetNo);
			Row row = sheet.getRow(rowNo);
			if (row == null)
				row = sheet.createRow(rowNo);
			Cell cell = row.getCell(columnNo);
			if (cell == null)
				cell = row.createCell(columnNo);
			cell.setCellValue(value);
			// 写入数据
			workbook.write(outputStream);
		} catch (Exception e) {
			throw new LYException("写入失败，工作表(" + sheetNo + ")，行(" + rowNo + ")，列(" + columnNo + ")", e);
		}
	}

	@Override
	public void close() throws Exception {
		try {
			outputStream.close();
		} catch (Exception e) {
		}
		try {
			workbook.close();
		} catch (Exception e) {
		}
	}

}