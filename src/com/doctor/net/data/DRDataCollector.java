package com.doctor.net.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

public class DRDataCollector {
	public static void main(String[] args) throws Exception {
		System.out.println("Started Program");
		String doctorPageString = "DoctorData_Page 1600-1639";
		String excelFilePath = "D:/" + doctorPageString + ".xls";
		DRDataCollectorExecutor drDataCollectorExecutor = new DRDataCollectorExecutor(excelFilePath, doctorPageString);
		for(int i = 1600; i < 1639; i++) {
			String url = "https://www.drdata.in/list-doctors.php?search=Doctor&page=" + (i+1);
			drDataCollectorExecutor.execute(url);
		}
		System.out.println("End Program");
	}
}

class DRDataCollectorExecutor {
	private String excelFilePath;
	private String excelSpreadSheetName;
	private List<String> headerList = Arrays.asList(new String[] {"Clinic/ Hospital Name", "Name", "Specialization", "Degree", "Area of Practice", "Practicing Since", "Practice as", "Date of Birth", "Address", "State", "District", "Phone Number", "Mobile Number and Email"});
	public DRDataCollectorExecutor(final String excelFilePath, final String excelSpreadSheetName) {
		this.excelFilePath = excelFilePath;
		this.excelSpreadSheetName = excelSpreadSheetName;
		final ExcelWriter excelWriter = new ExcelWriter(excelFilePath, excelSpreadSheetName);
		excelWriter.writeRecord(headerList);
		excelWriter.closeExcelFile();
	}
	
	public void execute(String url) {
		System.setProperty("webdriver.chrome.driver", "browser/chromedriver.exe");
		WebDriver driver = new ChromeDriver();
		driver.get(url);
		try {
			Thread.sleep(2000);
		}catch(Exception exception) {
			exception.printStackTrace();
		}
		
		int rowCounter = 0;
		while(true) {
			WebElement tableParentElement = driver.findElement(By.id("no-more-tables"));
			WebElement tbodyElement = tableParentElement.findElement(By.tagName("tbody"));
			List<WebElement> rows = tbodyElement.findElements(By.tagName("tr"));
			WebElement row = null;
			try {
				row = rows.get(rowCounter++);
			}catch(Exception exception) {}
			if(row != null) {
				List<WebElement> cells = row.findElements(By.tagName("td"));
				try {
					if(cells != null && cells.size() > 0) {
						WebElement detailsCell = cells.get(cells.size() - 1);
						detailsCell.findElement(By.tagName("a")).click();
					}
					Thread.sleep(2000);
					
					WebElement profileElement = driver.findElement(By.id("profile"));
					List<WebElement> profileDataList = profileElement.findElements(By.tagName("tr"));
					Map<String, String> doctorDetailMap = new ConcurrentHashMap<String, String>();
					for(String header : headerList) {
						doctorDetailMap.put(header, "");
					}
					for(WebElement profileRow : profileDataList) {
						List<WebElement> profileCells = profileRow.findElements(By.tagName("td"));
						if(profileCells != null && profileCells.size() == 2) {
							String detailName = profileCells.get(0).getText();
							String detailValue = profileCells.get(1).getText();
							Iterator<String> keyIterator = doctorDetailMap.keySet().iterator();
							while(keyIterator.hasNext()) {
								String key = keyIterator.next();
								if(key != null && detailName != null && detailName.trim().equalsIgnoreCase(key.trim())) {
									doctorDetailMap.put(key, detailValue);
								}
							}
						}
					}
					
					final ExcelWriter excelWriter = new ExcelWriter(excelFilePath, excelSpreadSheetName);
					final List<String> finalResultList = new ArrayList<String>();
					for(String header : headerList) {
						finalResultList.add(doctorDetailMap.get(header));
					}
					excelWriter.writeRecord(finalResultList);
					excelWriter.closeExcelFile();
					driver.get(url);
					Thread.sleep(2000);
				}catch(Exception exception) {
					if(cells != null && cells.size() > 0) {
						try {
						System.err.println("Failed to get details for doctor - " + cells.get(0).getText());
						}catch(Exception exceptionFailure) {}
					}
				}
			}

			if(rowCounter == 15)
				break;
		}
		driver.quit();
	}
	
	public String formatData(String inputString){
		if(inputString == null)
			return "";
		else
			return inputString.replaceAll("\\r?\\n", " ");
	}
}
