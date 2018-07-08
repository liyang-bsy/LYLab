package net.vicp.lylab.utils.excel;

import java.io.File;
import java.io.FileInputStream;
import java.util.Objects;

import net.vicp.lylab.core.exceptions.LYException;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelReader implements AutoCloseable {
	private Workbook workbook;

	public ExcelReader(String filePath) {
		Objects.requireNonNull(filePath);
		if (!new File(filePath).exists()) {
			throw new LYException("文件不存在，无法读取：" + filePath);
		}
		// 获取Excel文件对象
		try {
			if (filePath.endsWith("xls")) {
				workbook = new HSSFWorkbook(new FileInputStream(new File(filePath)));
			} else {
				workbook = new XSSFWorkbook(filePath);
			}
		} catch (Exception e) {
			// 不需要处理
		}
		// 反过来读取一次
		if (workbook == null) {
			try {
				if (filePath.endsWith("xls")) {
					workbook = new XSSFWorkbook(filePath);
				} else {
					workbook = new HSSFWorkbook(new FileInputStream(new File(filePath)));
				}
			} catch (Exception e) {
				// 不需要处理
			}
		}
		// 都读取不成功
		if (workbook == null) {
			throw new LYException("这个文件的内容可能不是excel：" + filePath);
		}
	}

	public String readExcelContent(int rowNo, int columnNo) {
		return readExcelContent(0, rowNo, columnNo);
	}

	public String readExcelContent(int sheetNo, int rowNo, int columnNo) {
		try {
			Sheet sheet = workbook.getSheetAt(sheetNo);
			if(sheet == null)
				return null;
			Row row = sheet.getRow(rowNo);
			if(row == null)
				return null;
			Cell cell = row.getCell(columnNo);
			if(cell == null)
				return null;
			return cell.getStringCellValue();
		} catch (Exception e) {
			throw new LYException("读取失败，工作表(" + sheetNo + ")，行(" + rowNo + ")，列(" + columnNo + ")", e);
		}
	}

	@Override
	public void close() throws Exception {
		try {
			workbook.close();
		} catch (Exception e) {
		}
	}

}