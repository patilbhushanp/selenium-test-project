package com.stock.net.data;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.Select;

public class GetTodayNSEData {

	public static void main(String[] args) throws Exception {
		System.out.println("Started Program");
		String excelFilePath = args[0]; //"D:/output.xls";
		List<String> copiedDataList = KeywordReader.getAllKeywords();
		GetTodayNSEDataExecutor getTodayNSEDataExecutor = new GetTodayNSEDataExecutor(excelFilePath, copiedDataList);
		getTodayNSEDataExecutor.execute();
		System.out.println("End Program");
	}

}

class GetTodayNSEDataExecutor {
	private List<String> dataList;
	private String excelFilePath;

	public GetTodayNSEDataExecutor(final String excelFilePath, final List<String> dataList) {
		this.excelFilePath = excelFilePath;
		this.dataList = dataList;
	}

	public void execute() {
		if (!this.dataList.isEmpty()) {
			System.setProperty("webdriver.chrome.driver", "browser/chromedriver.exe");
			WebDriver driver = new ChromeDriver();
			driver.get("https://www.nseindia.com/products/content/equities/equities/eq_security.htm");
			for (String keyword : this.dataList) {
				System.out.println("Starting processing for Search Text : " + keyword);
				List<String> cellStringList = new ArrayList<String>();
				try {
					driver.findElement(By.id("symbol")).clear();
					driver.findElement(By.id("symbol")).sendKeys(keyword);
					Select seriesDropdown = new Select(driver.findElement(By.id("series")));
					seriesDropdown.selectByVisibleText("EQ");
					driver.findElement(By.id("get")).click();
					Thread.sleep(5000);
					WebElement element = driver.findElement(By.id("historicalData"));
					if (element != null) {
						element = element.findElement(By.tagName("table"));
						List<WebElement> rowList = element.findElements(By.tagName("tr"));
						if(rowList.size() > 1) {
							List<WebElement> cellList = rowList.get(1).findElements(By.tagName("td"));
							for (WebElement cell : cellList) {
								cellStringList.add(cell.getText().replaceAll(",", ""));
							}
						}
					}
				} catch (Exception exception) {
					System.err.println("Failed to get keyword data - " + keyword + " with reason " + exception.getMessage());
				}
				
				final ExcelWriter excelWriter = new ExcelWriter(excelFilePath, keyword);
				excelWriter.writeRecord(cellStringList);
				excelWriter.closeExcelFile();
				
			}
			driver.quit();
		}

	}

}
