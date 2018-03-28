package com.supplier.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

public class AcidSupplierDataCollector {
	public static void main(String[] args) throws Exception {
		System.out.println("Started Program");
		String acidSupplierDataString = "AcidSupplierData_Page 300-350";
		String excelFilePath = "D:/" + acidSupplierDataString + ".xls";
		AcidSupplierDataCollectorExecutor acidSupplierDataCollectorExecutor = new AcidSupplierDataCollectorExecutor(excelFilePath, acidSupplierDataString);
		for(int i = 300; i < 350; i++) {
			String url = "http://www.esuppliersindia.com/products/chemicals/acid/?num_data=1813&set=1&page_no=" + (i+1);
			acidSupplierDataCollectorExecutor.execute(url);
		}
		System.out.println("End Program");
	}
}

class AcidSupplierDataCollectorExecutor {
	private String excelFilePath;
	private String excelSpreadSheetName;
	private List<String> headerList = Arrays.asList(new String[] {"Acid Type", "Company Name", "Address", "Contact Person", "Phone", "Mobile", "Fax", "Business Type", "Year Established", "Website", "Standard Certification", "Products Exporter, Manufacturer and Supplier"});
	public AcidSupplierDataCollectorExecutor(final String excelFilePath, final String excelSpreadSheetName) {
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
		List<WebElement> supplierRowList = driver.findElements(By.xpath("/html/body/table/tbody/tr[4]/td/table[2]/tbody/tr[1]/td[1]/table/tbody/tr[3]/td/table/tbody/tr[2]/td/table/tbody/tr[3]/td/table/tbody/tr/td/table/tbody/tr"));
		WebElement rowDataElement = null;
		for(int i = 1; i < supplierRowList.size(); i++) {
			try {
				List<String> rowDataList = new ArrayList<String>();
				supplierRowList = driver.findElements(By.xpath("/html/body/table/tbody/tr[4]/td/table[2]/tbody/tr[1]/td[1]/table/tbody/tr[3]/td/table/tbody/tr[2]/td/table/tbody/tr[3]/td/table/tbody/tr/td/table/tbody/tr"));
				rowDataElement = supplierRowList.get(i);
							
				List<WebElement> dataElementList = rowDataElement.findElements(By.cssSelector("td.orangebg"));
				for(WebElement dataElement : dataElementList) {
					rowDataList.add(formatData(dataElement.getText()));
				}
				List<WebElement> anchorLinkElementList = rowDataElement.findElements(By.cssSelector("span.blue-f11-b"));
				for(WebElement anchorLinkElement : anchorLinkElementList) {

					anchorLinkElement.findElement(By.tagName("a")).click();
				
					List<WebElement> supplierDetailList = driver.findElements(By.xpath("/html/body/table/tbody/tr[4]/td/table[2]/tbody/tr[1]/td[1]/table/tbody/tr/td[1]/table/tbody/tr[3]/td/table/tbody/tr[5]/td/table/tbody/tr"));
					for(WebElement supplierDetailElement : supplierDetailList) {
						List<WebElement> tempList = supplierDetailElement.findElements(By.tagName("td"));
						if(tempList != null && tempList.size() > 1 && tempList.get(1) != null) {
							rowDataList.add(tempList.get(1).getText().trim());
						}
					}
					
					supplierDetailList = driver.findElements(By.xpath("/html/body/table/tbody/tr[4]/td/table[2]/tbody/tr[1]/td[1]/table/tbody/tr/td[1]/table/tbody/tr[3]/td/table/tbody/tr[1]/td/table/tbody/tr/td/table/tbody/tr[3]/td/table/tbody/tr"));
					for(WebElement supplierDetailElement : supplierDetailList) {
						List<WebElement> tempList = supplierDetailElement.findElements(By.tagName("td"));
						if(tempList != null && tempList.size() > 1 && tempList.get(1) != null) {
							rowDataList.add(tempList.get(1).getText().trim());
						}
					}
				}

				if(rowDataList.isEmpty() == false) {
					final ExcelWriter excelWriter = new ExcelWriter(excelFilePath, excelSpreadSheetName);
					excelWriter.writeRecord(rowDataList);
					excelWriter.closeExcelFile();
				}
				
				try {
					Thread.sleep(2000);
				}catch(Exception exception) {
					exception.printStackTrace();
				}
				
				driver.navigate().back();
				driver.switchTo().activeElement();
				
			}catch(Exception exception) {
				System.err.println("Failed to get information");
				driver.get(url);
				driver.switchTo().activeElement();
			}
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
