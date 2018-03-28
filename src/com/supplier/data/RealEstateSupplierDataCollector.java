package com.supplier.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

public class RealEstateSupplierDataCollector {
	public static void main(String[] args) throws Exception {
		System.out.println("Started Program");
		String acidSupplierDataString = "RealEstateData Page_601-800";
		String excelFilePath = "D:/" + acidSupplierDataString + ".xls";
		RealEstateSupplierDataCollectorExecutor realEstateSupplierDataCollectorExecutor = new RealEstateSupplierDataCollectorExecutor(excelFilePath, acidSupplierDataString);
		for(int i = 600; i < 800; i++) {
			String url = "http://www.esuppliersindia.com/products/construction-real-estate/bathroom-toilet-accessories-fittings/?num_data=2906&set=1&page_no=" + (i+1);
			realEstateSupplierDataCollectorExecutor.execute(url);
		}
		System.out.println("End Program");
	}
}

class RealEstateSupplierDataCollectorExecutor {
	private String excelFilePath;
	private String excelSpreadSheetName;
	private List<String> headerList = Arrays.asList(new String[] {"Material Available Type", "Supplier Name", "Phone Number", "Other Information"});
	public RealEstateSupplierDataCollectorExecutor(final String excelFilePath, final String excelSpreadSheetName) {
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
		System.out.println("supplierRowList : " + supplierRowList.size());
		for(int i = 1; i < supplierRowList.size(); i++) {
			List<String> rowDataList = new ArrayList<String>();
			WebElement rowDataElement = supplierRowList.get(i);
			List<WebElement> dataElementList = rowDataElement.findElements(By.cssSelector("td.orangebg"));
			for(WebElement dataElement : dataElementList) {
				rowDataList.add(formatData(dataElement.getText()));
			}
			dataElementList = rowDataElement.findElements(By.cssSelector("td.text-f11"));
			for(WebElement dataElement : dataElementList) {
				rowDataList.add(formatData(dataElement.getText()));
			}
			
			if(rowDataList.isEmpty() == false) {
				final ExcelWriter excelWriter = new ExcelWriter(excelFilePath, excelSpreadSheetName);
				excelWriter.writeRecord(rowDataList);
				excelWriter.closeExcelFile();
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
