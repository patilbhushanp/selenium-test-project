package com.india.net.test;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

public class TestProgram7 {
	public static void main(String[] args) throws Exception, IOException {
		TestProgram7 testProgram = new TestProgram7();
		List<String> copiedDataList = testProgram.getDataFromCopy();
		if (!copiedDataList.isEmpty()) {
			int maxThread = 400000;
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
					RunProgram7 runProgram = new RunProgram7(subDataList, "D:\\TEST");
					runProgram.start();
				}
			} else {
				RunProgram7 runProgram = new RunProgram7(copiedDataList, "D:\\TEST");
				runProgram.run();
			}
		}
	}

	private List<String> getDataFromCopy() throws Exception {
		List<String> copiedDataList = new ArrayList<String>();
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Clipboard clipboard = toolkit.getSystemClipboard();
		String result = (String) clipboard.getData(DataFlavor.stringFlavor);
		String lines[] = result.split("\\r?\\n");
		copiedDataList = Arrays.asList(lines);
		return copiedDataList;
	}
}

class RunProgram7 extends Thread {
	private List<String> dataList;
	private String outputFileDirectory;

	public RunProgram7(List<String> dataList, String outputFileDirectory) {
		this.dataList = dataList;
		this.outputFileDirectory = outputFileDirectory;
	}

	@Override
	public void run() {
		if (!this.dataList.isEmpty()) {
			File outputFile = new File(this.outputFileDirectory + "/" + Thread.currentThread().getName() + ".csv");
			FileWriter fileWriter = null;
			BufferedWriter bufferedWriter = null;
			try {
				fileWriter = new FileWriter(outputFile, true);
				bufferedWriter = new BufferedWriter(fileWriter);

				StringBuilder record = new StringBuilder();
				System.setProperty("webdriver.chrome.driver", "browser/chromedriver.exe");
				WebDriver driver = new ChromeDriver();
				driver.get("https://www.justdial.com/");

				record.append("SearchKey$");
				record.append("Full Address$");
				record.append("Telephone Number$");
				record.append("Website$");
				record.append("Operational Hours\n");

				for (String keyword : this.dataList) {
					try {
						driver.findElement(By.id("srchbx")).clear();
						driver.findElement(By.id("srchbx")).sendKeys(keyword);
						driver.findElement(By.id("srchbx")).sendKeys(Keys.ENTER);
						List<WebElement> webElements = driver.findElements(By.cssSelector("li.cntanr"));
						if (webElements.size() > 1) {
							Thread.sleep(5000);
							webElements.get(0).click();
							getAddressInformation(keyword, driver, record);
						} else {
							getAddressInformation(keyword, driver, record);
						}
					} catch (Exception exception) {
						System.err.println("No address found - " + keyword);
					}
					record.append("\n");
				}
				bufferedWriter.write(record.toString());
				driver.quit();
				System.out.println("Output File Path - " + outputFile.getAbsolutePath());
			} catch (IOException exception) {
				System.out.println("Failed to Open File - " + outputFile.getAbsolutePath());
				exception.printStackTrace();
				System.exit(0);
			} finally {
				try {
					if (bufferedWriter != null)
						bufferedWriter.close();
					if (fileWriter != null)
						fileWriter.close();
				} catch (IOException exception) {
					exception.printStackTrace();
				}
			}
		}

	}

	private void getAddressInformation(String searchKeyword, WebDriver driver, StringBuilder record) {
		record.append(searchKeyword + "$");

		try {
			WebElement fullAddressElement = driver.findElement(By.id("fulladdress"));
			record.append(fullAddressElement.getText() + "$");
		}catch(Exception exception) {
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
			}
		}catch(Exception exception) {
			System.err.println("Failed to get telephone number " + exception.getLocalizedMessage());
		}
		
		try {
			WebElement webElement = driver.findElement(By.cssSelector("span.mreinfp.comp-text"));
			webElement = webElement.findElement(By.tagName("a"));
			if(webElement.getAttribute("href") != null) {
				record.append(webElement.getAttribute("href") + ",");
			}else {
				record.append("Not Available,");
			}
		}catch(Exception exception) {
			System.err.println("Failed to get Website " + exception.getLocalizedMessage());
		}
		
		try {
			WebElement webElement = driver.findElement(By.id("vhall"));
			webElement.click();
			webElement = driver.findElement(By.id("statHr"));
			List<WebElement> operationHourTimeList = webElement.findElements(By.className("mreinfli"));
			StringBuilder hourListBuilder = new StringBuilder();
			for(WebElement hourElement : operationHourTimeList) {
				hourListBuilder.append(hourElement.getText() + "|");
			}
			record.append(formatData(hourListBuilder.toString()));
			record.append(",");
		}catch(Exception exception) {
			System.err.println("Hours of Operation failed "+ exception.getLocalizedMessage());
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
