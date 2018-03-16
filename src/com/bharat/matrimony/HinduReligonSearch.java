package com.bharat.matrimony;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;

public class HinduReligonSearch {
	public static void main(String[] args) throws Exception {
		System.out.println("Started Program");
		String bharatMatrimonyString = "BharatMatrimony";
		String excelFilePath = "F:/" + bharatMatrimonyString + ".xls";
		HinduReligonSearchExecutor hinduReligonSearchExecutor = new HinduReligonSearchExecutor(excelFilePath,
				bharatMatrimonyString);
		String url = "https://www.hindimatrimony.com/";
		hinduReligonSearchExecutor.execute(url);
		System.out.println("End Program");
	}
}

class HinduReligonSearchExecutor {
	private String excelFilePath;
	private String excelSpreadSheetName;
	private List<String> headerList = Arrays.asList(
			new String[] { "Name", "Religion", "Caste", "Sub Caste", "Location", "Anual Income" });

	public HinduReligonSearchExecutor(final String excelFilePath, final String excelSpreadSheetName) {
		this.excelFilePath = excelFilePath;
		this.excelSpreadSheetName = excelSpreadSheetName;
		final ExcelWriter excelWriter = new ExcelWriter(this.excelFilePath, this.excelSpreadSheetName);
		excelWriter.writeRecord(headerList);
		excelWriter.closeExcelFile();
	}

	public void execute(String url) {
		System.setProperty("webdriver.chrome.driver", "browser/chromedriver.exe");
		WebDriver driver = new ChromeDriver();
		driver.get(url);
		waitForNextOperation(2000);
		performedAuthentication(driver);
		skipAdvertise(driver);
		performedSearchOperation(driver);
		for(int i = 0; i < 10; i++) {
			List<WebElement> searchResultList = driver.findElements(By.className("srhlist-bg"));
			for(int j = 0; j < searchResultList.size(); j++) {
				List<String> recordData = new ArrayList<String>();
				WebElement searchResultElement = searchResultList.get(j);
				List<WebElement> linkElementList = searchResultElement.findElements(By.className("link"));
				if(linkElementList.size() > 0) {
					String text = linkElementList.get(0).getText();
					if (text != null && text.trim().length() > 2)
					recordData.add(text.trim());
				}
				
				List<WebElement> searchUserDetailList = searchResultElement.findElements(By.className("paddt5"));
				for(WebElement searchUserDetailElement : searchUserDetailList) {
					String text = searchUserDetailElement.getText();
					if(text != null && text.trim().length() > 2 && text.indexOf(":") > 0) {
						String[] textArray = text.split(":");
						if(textArray != null && textArray.length == 2) {
							recordData.add(textArray[1]);
						}
					}
				}
				
				if(recordData != null && recordData.size() > 0) {
					ExcelWriter excelWriter = new ExcelWriter(this.excelFilePath, this.excelSpreadSheetName);
					excelWriter.writeRecord(recordData);
					excelWriter.closeExcelFile();
				}
			}
			if (driver instanceof JavascriptExecutor) {
			    ((JavascriptExecutor)driver).executeScript("shownxt_pg(Jsg_curpage,'frmpaging');");
			    waitForNextOperation(2000);
			}else {
				break;
			}
		}		
		driver.quit();
	}

	private void performedAuthentication(final WebDriver driver) {
		driver.findElement(By.id("ID")).sendKeys("H6940194");
		driver.findElement(By.id("ID")).sendKeys(Keys.TAB);
		driver.findElement(By.id("PASSWORD")).sendKeys("hellocommunity");
		driver.findElement(By.id("PASSWORD")).sendKeys(Keys.ENTER);
		waitForNextOperation(15000);
	}

	private void skipAdvertise(final WebDriver driver) {
		try {
			driver.findElement(By.className("skiptxtbtn")).click();
			waitForNextOperation(5000);
		} catch(Exception exception) {
			System.err.println("Advertise banner is not available " + exception.getLocalizedMessage() );
		}
	}

	private void performedSearchOperation(final WebDriver driver) {
		WebElement searchElement = driver.findElement(By.linkText("SEARCH"));
		Actions action = new Actions(driver);
        action.moveToElement(searchElement).build().perform();
        driver.findElement(By.linkText("Search")).click();
		waitForNextOperation(10000);
	}
	
	private void waitForNextOperation(int milliseconds) {
		try {
			Thread.sleep(milliseconds);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}
}
