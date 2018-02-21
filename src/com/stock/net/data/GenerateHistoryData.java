package com.stock.net.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.Select;

/**
 * Hello world!
 *
 */
public class GenerateHistoryData {

	public static void main(String[] args) throws Exception {
		System.out.println("Started Program");
		String excelFilePath = args[0]; // "D:/output.xls"
		String reportDownloadDirectory = args[1]; //"C:/Users/WellWisher/Downloads"
		List<String> copiedDataList = KeywordReader.getAllKeywords();
		GenerateHistoryDataExecutor generateHistoryDataExecutor = new GenerateHistoryDataExecutor(excelFilePath,
				reportDownloadDirectory, copiedDataList);
		generateHistoryDataExecutor.execute();
		System.out.println("End Program");
	}

}

class GenerateHistoryDataExecutor {
	private List<String> dataList;
	private String excelFilePath;
	private String reportDownloadDirectory;

	public GenerateHistoryDataExecutor(final String excelFilePath, final String reportDownloadDirectory,
			final List<String> dataList) {
		this.excelFilePath = excelFilePath;
		this.reportDownloadDirectory = reportDownloadDirectory;
		this.dataList = dataList;
	}

	public void execute() {
		if (!this.dataList.isEmpty()) {
			System.setProperty("webdriver.chrome.driver", "browser/chromedriver.exe");
			WebDriver driver = new ChromeDriver();
			driver.get("https://www.nseindia.com/products/content/equities/equities/eq_security.htm");
			for (String keyword : this.dataList) {
				System.out.println("Starting processing for Search Text : " + keyword);
				
				try {
					driver.findElement(By.id("symbol")).clear();
					driver.findElement(By.id("symbol")).sendKeys(keyword);
					Select dropdown = new Select(driver.findElement(By.id("dateRange")));
					dropdown.selectByIndex(6);
					Select seriesDropdown = new Select(driver.findElement(By.id("series")));
					seriesDropdown.selectByVisibleText("EQ");
					driver.findElement(By.id("get")).click();
					Thread.sleep(5000);
					driver.findElement(By.partialLinkText("Download file in csv format")).click();
					Thread.sleep(10000);
					File latestFile = getLatestFilefromDownlaods();
					List<List<String>> fileData = getFileData(latestFile);
					final ExcelWriter excelWriter = new ExcelWriter(excelFilePath, keyword);
					for (List<String> rowData : fileData) {
						excelWriter.writeRecord(rowData);
					}
					excelWriter.closeExcelFile();
				} catch (Exception exception) {
					System.err.println("Failed to get keyword data - " + keyword + " with reason " + exception.getMessage());
				}
				
			}
			driver.quit();
		}

	}

	private List<List<String>> getFileData(File latestFile) {
		List<List<String>> fileData = new ArrayList<List<String>>();
		try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(latestFile));
			while (true) {
				String temp = bufferedReader.readLine();
				if (temp == null)
					break;
				List<String> rowData = Arrays.asList(temp.split(","));
				List<String> newRowData = new ArrayList<String>();
				for(String cellData : rowData) {
					cellData = cellData.replaceAll("\"","");
					newRowData.add(cellData);
				}
				fileData.add(newRowData);
			}
			bufferedReader.close();
		} catch (Exception exception) {
			System.err.println("Failed to read downloaded file : " + latestFile.getAbsolutePath() + " with reason "
					+ exception.getMessage());
		}
		return fileData;
	}

	private File getLatestFilefromDownlaods() {
		File dir = new File(reportDownloadDirectory);
		File[] files = dir.listFiles();
		if (files == null || files.length == 0) {
			return null;
		}

		File lastModifiedFile = files[0];
		for (int i = 1; i < files.length; i++) {
			if (lastModifiedFile.lastModified() < files[i].lastModified()) {
				lastModifiedFile = files[i];
			}
		}
		return lastModifiedFile;
	}

}
