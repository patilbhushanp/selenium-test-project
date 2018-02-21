package com.doctor.net.data;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

public class IndMedicaDataCollector {
	public static void main(String[] args) throws Exception {
		System.out.println("Started Program");
		String excelFilePath = args[0]; //"D:/DoctorData.xls";
		IndMedicaDataCollectorExecutor indMedicaDataCollectorExecutor = new IndMedicaDataCollectorExecutor(excelFilePath);
		indMedicaDataCollectorExecutor.execute();
		System.out.println("End Program");
	}
}

class IndMedicaDataCollectorExecutor {
	private String excelFilePath;
	public IndMedicaDataCollectorExecutor(final String excelFilePath) {
		this.excelFilePath = excelFilePath;
	}
	
	public void execute() {
		System.setProperty("webdriver.chrome.driver", "browser/chromedriver.exe");
		WebDriver driver = new ChromeDriver();
		driver.get("http://www.indmedica.com/directory.php?keywords=Dr&directory=doctor&search=Search&action=search");
		
		for(int i = 1 ; i < 200; i++) {
			boolean pageStatusFailed = false;
			List<List<String>> pageData = new ArrayList<List<String>>();
			for(int j = 1 ; j < 11; j++) {
				System.out.println("Record Number on page "+ j);
				WebElement tableElement = driver.findElement(By.className("rslttbl"));
				WebElement trElement = tableElement.findElements(By.tagName("tr")).get(j);
				WebElement tdElement = trElement.findElements(By.tagName("td")).get(0);
				tdElement.findElement(By.tagName("a")).click();
				try {
					Thread.sleep(2000);
				}catch(Exception exception) {
					System.err.println("Failed to Pause Flow " + exception.getLocalizedMessage());
				}
				WebElement doctorDataElement = driver.findElement(By.className("maincolumn"));
				String doctorName = doctorDataElement.findElement(By.tagName("h4")).getText();
				List<WebElement> doctorDetails = doctorDataElement.findElements(By.tagName("p"));
				if(doctorDetails.size() == 4) {
					String qualification = formatData(doctorDetails.get(0).getText()).replaceAll("Qualifications:", "").trim();
					String speciality = formatData(doctorDetails.get(1).getText()).replaceAll("Speciality:", "").trim();
					String address = formatData(doctorDetails.get(2).getText()).trim();
					String phone = formatData(doctorDetails.get(3).getText()).replaceAll("Phone:", "").trim();
					List<String> data = new ArrayList<String>();
					data.add(doctorName);
					data.add(qualification);
					data.add(speciality);
					data.add(address);
					data.add(phone);
					pageData.add(data);
				}else if(doctorDetails.size() == 3) {
					String qualification = formatData(doctorDetails.get(0).getText()).replaceAll("Qualifications:", "").trim();
					String speciality = formatData(doctorDetails.get(1).getText()).replaceAll("Speciality:", "").trim();
					String address = formatData(doctorDetails.get(2).getText()).trim();
					List<String> data = new ArrayList<String>();
					data.add(doctorName);
					data.add(qualification);
					data.add(speciality);
					data.add(address);
					pageData.add(data);
				}
				doctorDataElement.findElement(By.tagName("a")).click();
				try {
					Thread.sleep(2000);
				}catch(Exception exception) {
					System.err.println("Failed to Pause Flow " + exception.getLocalizedMessage());
				}
			}
			
			final ExcelWriter excelWriter = new ExcelWriter(excelFilePath, "doctor_data");
			for(List<String> data : pageData) {
				excelWriter.writeRecord(data);
			}
			excelWriter.closeExcelFile();
			
			try {
				WebElement pageNavigationElement = driver.findElement(By.className("pagenav"));
				pageNavigationElement.findElement(By.linkText("" + (i+1))).click();
				System.out.println("Calling "+ i + " page") ;
				pageStatusFailed = false;
				try {
					Thread.sleep(2000);
				}catch(Exception exception) {
					System.err.println("Failed to Pause Flow " + exception.getLocalizedMessage());
				}
			}catch(Exception exception) {
				System.err.println("Failed to click on next page " + exception.getLocalizedMessage());
				System.err.println("Failed to call "+ i + " page") ;
				pageStatusFailed = true;
			}
			if(pageStatusFailed)
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
