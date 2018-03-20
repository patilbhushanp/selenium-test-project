package com.bing.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public class ExcelWriter {
	private String excelFilePath;
	private String spreadSheetName;
	private HSSFWorkbook hssfWorkbook;
	private HSSFSheet hssfSheet;
	private FileInputStream fileInputStream = null;

	public ExcelWriter(final String excelFilePath, final String spreadSheetName) {
		this.excelFilePath = excelFilePath;
		this.spreadSheetName = spreadSheetName;
		hssfWorkbook = getWorkbookAvailable(this.excelFilePath);
		if (hssfWorkbook == null) {
			hssfWorkbook = createNewWorkbook();
		}
		hssfSheet = getSpreadSheetAvailable(this.spreadSheetName);
		if (hssfSheet == null) {
			hssfSheet = createNewSpreadSheet();
		}

	}

	private HSSFWorkbook createNewWorkbook() {
		return new HSSFWorkbook();
	}

	private HSSFSheet createNewSpreadSheet() {
		return hssfWorkbook.createSheet(this.spreadSheetName);
	}

	private HSSFSheet getSpreadSheetAvailable(String spreadSheetName) {
		return hssfWorkbook.getSheet(spreadSheetName);
	}

	private HSSFWorkbook getWorkbookAvailable(String excelFilePath) {
		HSSFWorkbook hssfWorkbook = null;
		File file = new File(excelFilePath);
		if (file.isFile()) {
			try {
				this.fileInputStream = new FileInputStream(file);
				hssfWorkbook = new HSSFWorkbook(fileInputStream);
			} catch (Exception exception) {
				System.err.println("Failed to get Excel File with reason " + exception.getMessage());
			}
		}
		return hssfWorkbook;
	}

	public void writeRecord(List<String> rowData) {
		int rowNumber = this.hssfSheet.getLastRowNum();
		HSSFRow row = hssfSheet.createRow(rowNumber + 1);
		int counter = 0;
		for (String cellData : rowData) {
			HSSFCell cell = row.createCell(counter);
			cell.setCellValue(cellData);
			counter++;
		}
	}

	public void closeExcelFile() {
		FileOutputStream fileOutputStream = null;
		try {
			if (fileInputStream != null) {
				try {
					fileInputStream.close();
				} catch (IOException exception) {
					System.err.println("Failed to open excel file with reason " + exception.getMessage());
				}
			}

			fileOutputStream = new FileOutputStream(this.excelFilePath);
			this.hssfWorkbook.write(fileOutputStream);
			fileOutputStream.flush();
			fileOutputStream.close();
		} catch (Exception exception) {
			System.err.println("Failed to close excel file with reason " + exception.getMessage());
		} finally {
			if (fileOutputStream != null) {
				try {
					fileOutputStream.close();
				} catch (IOException exception) {
					System.err.println("Failed to close excel file with reason " + exception.getMessage());
				}
			}
		}
	}
}
