package com.stock.net.data;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

public class GetTodayOptionChainData {

	public static void main(String[] args) throws Exception {
		System.out.println("Started Program");
		String excelFilePath = args[0]; //"D:/output.xls"
		List<String> copiedDataList = KeywordReader.getAllKeywords();
		GetTodayOptionChainDataExecutor getTodayOptionChainDataExecutor = new GetTodayOptionChainDataExecutor(excelFilePath, copiedDataList);
		getTodayOptionChainDataExecutor.execute();
		System.out.println("End Program");
	}

}

class GetTodayOptionChainDataExecutor {
	private List<String> dataList;
	private String excelFilePath;
	private List<String> headerList = Arrays.asList(new String[] {"", "OI","Chng in OI","Volume","IV","LTP","Net Chng","Bid Qty", "Bid Price", "Ask	Price", "Ask Qty", "Strike Price", "Bid Qty", "Bid Price", "Ask Price", "Ask Qty", "Net Chng", "LTP", "IV", "Volume", "Chng In OI", "OI" });
	public GetTodayOptionChainDataExecutor(final String excelFilePath, final List<String> dataList) {
		this.excelFilePath = excelFilePath;
		this.dataList = dataList;
	}

	public void execute() {
		if (!this.dataList.isEmpty()) {
			System.setProperty("webdriver.chrome.driver", "browser/chromedriver.exe");
			WebDriver driver = new ChromeDriver();
			driver.get("https://www.nseindia.com/live_market/dynaContent/live_watch/option_chain/optionKeys.jsp?symbolCode=798&symbol=HDFC&symbol=hdfc&instrument=OPTSTK&date=");
			for (String keyword : this.dataList) {
				System.out.println("Starting processing for Search Text : " + keyword);
				List<List<String>> rowDataList = new ArrayList<List<String>>();
				try {
					driver.findElement(By.id("underlyStock")).clear();
					driver.findElement(By.id("underlyStock")).sendKeys(keyword);
					if (driver instanceof JavascriptExecutor) {
					    ((JavascriptExecutor)driver).executeScript("goBtnClick('stock');");
					}
					Thread.sleep(5000);
					
					WebElement element = driver.findElement(By.id("octable"));
					List<WebElement> rowList = element.findElements(By.tagName("tr"));
					WebElement previousRow = null;
					WebElement currentRow = null;
					for(int i = 0; i < rowList.size() && rowList.size() > 2; i++) {
						WebElement row = rowList.get(i);
						List<WebElement> cellList = row.findElements(By.tagName("td"));
						if(!cellList.isEmpty()) {
							String className = cellList.get(1).getAttribute("class");
							if(className.equalsIgnoreCase("nobg")) {
								currentRow = row;
								previousRow = rowList.get(i - 1);
								break;
							}
						}
					}
					
					List<WebElement> cellList = previousRow.findElements(By.tagName("td"));
					List<String> cellStringList = new ArrayList<String>();
					for (WebElement cell : cellList) {
						String columnValue = cell.getText().replaceAll(",", "");
						cellStringList.add(columnValue);
					}
					rowDataList.add(cellStringList);
					
					cellList = currentRow.findElements(By.tagName("td"));
					cellStringList = new ArrayList<String>();
					for (WebElement cell : cellList) {
						String columnValue = cell.getText().replaceAll(",", "");
						cellStringList.add(columnValue);
					}
					rowDataList.add(cellStringList);
					
				} catch (Exception exception) {
					exception.printStackTrace();
					System.err.println("Failed to get keyword data - " + keyword);
				}
				
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY-MM-dd");
				String systemDate = "Record For - " + simpleDateFormat.format(new Date());
				
				final ExcelWriter excelWriter = new ExcelWriter(excelFilePath, keyword);
				excelWriter.writeRecord(Arrays.asList(new String[] {systemDate}));
				excelWriter.writeRecord(headerList);
				for(List<String> record : rowDataList) {
					excelWriter.writeRecord(record);
				}
				excelWriter.writeRecord(Arrays.asList(new String[] {}));
				excelWriter.closeExcelFile();
			}
			driver.quit();
		}

	}

}