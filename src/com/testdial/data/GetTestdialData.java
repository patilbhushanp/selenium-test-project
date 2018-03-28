package com.justdail.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.bharat.matrimony.ExcelWriter;
import com.stock.net.data.KeywordReader;

public class GetTestdailData {
	public static void main(String[] args) throws Exception, IOException {
		GetTestdailData getTestdailData = new GetTestdailData();
		List<String> copiedDataList = getTestdailData.getDataFromCopy();
		if (!copiedDataList.isEmpty()) {
			int maxThread = 2;
			int totalRecord = copiedDataList.size();
			int eachThreadDataSize = totalRecord / maxThread;
			if (eachThreadDataSize > 0) {
				for (int i = 0; i < maxThread; i++) {
					List<String> subDataList = new ArrayList<String>();
					if (maxThread == (i + 1)) {
						subDataList.addAll(copiedDataList.subList(i * eachThreadDataSize, copiedDataList.size()));
					} else {
						subDataList
								.addAll(copiedDataList.subList(i * eachThreadDataSize, (i + 1) * eachThreadDataSize));
					}
					GetTestdailDataExecutor getTestdailDataExecutor = new GetTestdailDataExecutor(subDataList, "D:\\");
					getTestdailDataExecutor.start();
				}
			} else {
				GetTestdailDataExecutor getTestdailDataExecutor = new GetTestdailDataExecutor(copiedDataList, "D:\\");
				getTestdailDataExecutor.run();
			}
		}
	}

	private List<String> getDataFromCopy() throws Exception {
		return KeywordReader.getAllKeywords("D:\\getJustdataInput.txt");
	}
}

class GetTestdailDataExecutor extends Thread {
	private List<String> dataList;
	private String outputFileDirectory;
	private List<String> headerList = Arrays.asList(
			new String[] { "Search Address", "Full Address", "Telephone Number", "Website", "Operational Hours" });

	public GetTestdailDataExecutor(List<String> dataList, String outputFileDirectory) {
		this.dataList = dataList;
		this.outputFileDirectory = outputFileDirectory;
	}

	@Override
	public void run() {
		final ExcelWriter excelWriter = new ExcelWriter(
				this.outputFileDirectory + "/" + Thread.currentThread().getName() + ".xls",
				Thread.currentThread().getName());
		excelWriter.writeRecord(headerList);
		excelWriter.closeExcelFile();

		if (!this.dataList.isEmpty()) {
			
			 /*System.setProperty("webdriver.chrome.driver", "browser/chromedriver.exe");
			 WebDriver driver = new ChromeDriver();*/
			 

			final DesiredCapabilities capabilities = DesiredCapabilities.htmlUnit();
			capabilities.setCapability(CapabilityType.ForSeleniumServer.ENSURING_CLEAN_SESSION, true);
			capabilities.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.ACCEPT);
			capabilities.setJavascriptEnabled(true);
			WebDriver driver = new HtmlUnitDriver(capabilities);

			final Map<String, String> resultDataMap = new ConcurrentHashMap<String, String>();
			for (String keyword : this.dataList) {
				try {
					driver.get("https://www.testdial.com/");
					try {
						Thread.sleep(5000);
					} catch (Exception exception) {}

					driver.findElement(By.id("srchbx")).clear();
					driver.findElement(By.id("srchbx")).sendKeys(keyword);
					driver.findElement(By.id("srchbx")).sendKeys(Keys.ENTER);
					Thread.sleep(5000);
					List<WebElement> webElements = driver.findElements(By.cssSelector("li.cntanr"));
					if (webElements.size() > 1) {
						Thread.sleep(5000);
						webElements.get(0).click();
						getAddressInformation(keyword, driver, resultDataMap);
					} else {
						getAddressInformation(keyword, driver, resultDataMap);
					}
				} catch (Exception exception) {
					System.err.println("No address found - " + keyword);
					exception.printStackTrace();
				}
			}
			driver.quit();
		}
	}

	private void getAddressInformation(String searchKeyword, WebDriver driver, Map<String, String> resultDataMap) {
		resultDataMap.put("Search Address", searchKeyword);
		try {
			WebElement fullAddressElement = driver.findElement(By.id("fulladdress"));
			resultDataMap.put("Full Address", fullAddressElement.getText().trim());
		} catch (Exception exception) {
			System.err.println("Failed to get Address " + exception.getLocalizedMessage());
		}

		try {
			String pageSource = driver.getPageSource();
			int startIndex = pageSource.indexOf("-moz-osx-font-smoothing:grayscale}.icon-")
					+ "-moz-osx-font-smoothing:grayscale}.".length();
			int lastIndex = pageSource.indexOf(".mobilesv{font-size:11px;color:#000");
			pageSource = pageSource.substring(startIndex, lastIndex);

			ConcurrentHashMap<String, Integer> digitMap = createDigitMap(pageSource);
			List<WebElement> webElements = driver.findElements(By.cssSelector("span.telnowpr"));
			if (webElements.size() > 2) {
				StringBuilder record = new StringBuilder();
				WebElement telephoneListElement = webElements.get(1);
				List<WebElement> telephoneList = telephoneListElement.findElements(By.tagName("a"));
				for (WebElement telephoneElement : telephoneList) {
					List<WebElement> telephoneNumberDigitList = telephoneElement.findElements(By.className("mobilesv"));
					for (WebElement digitElement : telephoneNumberDigitList) {
						String classNameStr = digitElement.getAttribute("class");
						String[] classNames = classNameStr.split("\\s+");
						if (classNames.length == 2) {
							String className = classNames[1];
							if (digitMap.get(className) == 10) {
								record.append("+");
								System.out.print("+");
							} else {
								record.append(digitMap.get(className));
							}
						}
					}
					record.append(", ");
				}
				resultDataMap.put("Telephone Number", record.toString());
			}
		} catch (Exception exception) {
			System.err.println("Failed to get telephone number " + exception.getLocalizedMessage());
		}

		try {
			WebElement webElement = driver.findElement(By.cssSelector("span.mreinfp.comp-text"));
			webElement = webElement.findElement(By.tagName("a"));
			if (webElement.getAttribute("href") != null) {
				resultDataMap.put("Website", webElement.getAttribute("href").trim());
			} else {
				resultDataMap.put("Website", "Not Available");
			}
		} catch (Exception exception) {
			System.err.println("Failed to get Website " + exception.getLocalizedMessage());
		}

		try {
			WebElement webElement = driver.findElement(By.id("vhall"));
			webElement.click();
			webElement = driver.findElement(By.id("statHr"));
			List<WebElement> operationHourTimeList = webElement.findElements(By.className("mreinfli"));
			StringBuilder hourListBuilder = new StringBuilder();
			for (WebElement hourElement : operationHourTimeList) {
				hourListBuilder.append(hourElement.getText() + "|");
			}
			resultDataMap.put("Operational Hours", formatData(hourListBuilder.toString()));
		} catch (Exception exception) {
			System.err.println("Hours of Operation failed " + exception.getLocalizedMessage());
		}

		List<String> recordData = new ArrayList<String>();
		for(String header : headerList) {
			recordData.add(resultDataMap.get(header) == null ? "" : resultDataMap.get(header));
		}
		if(recordData != null && !recordData.isEmpty()) {
			final ExcelWriter excelWriter = new ExcelWriter(
					this.outputFileDirectory + "/" + Thread.currentThread().getName() + ".xls",
					Thread.currentThread().getName());
			excelWriter.writeRecord(recordData);
			excelWriter.closeExcelFile();			
		}
		
	}

	private ConcurrentHashMap<String, Integer> createDigitMap(String digitEncodedString) {
		ConcurrentHashMap<String, Integer> digitMap = new ConcurrentHashMap<String, Integer>();
		String[] digitEncoderArray = digitEncodedString.split("\\.");
		int count = 0;
		if (digitEncoderArray.length == 14) {
			for (String digitcharacter : digitEncoderArray) {
				digitMap.put(digitcharacter.split(":")[0], count++);
			}
		}
		return digitMap;
	}

	private String formatData(String inputString) throws Exception {
		return inputString.replaceAll("\\r?\\n", "\t");
	}
}
