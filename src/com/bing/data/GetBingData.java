package com.bing.data;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import com.google.gson.Gson;

public class GetBingData {
	public static void main(String[] args) throws Exception, IOException {
		List<String> copiedDataList = getDataFromCopy();
		if (!copiedDataList.isEmpty()) {
			int maxThread = 1;
			int totalRecord = copiedDataList.size();
			int eachThreadDataSize = totalRecord / maxThread;
			for (int i = 0; i < maxThread; i++) {
				List<String> subDataList = new ArrayList<String>();
				if (maxThread == (i + 1)) {
					subDataList.addAll(copiedDataList.subList(i * eachThreadDataSize, copiedDataList.size()));
				} else {
					subDataList.addAll(copiedDataList.subList(i * eachThreadDataSize, (i + 1) * eachThreadDataSize));
				}
				GetBingDataExecutor getBingDataExecutor = new GetBingDataExecutor(subDataList, "D:\\");
				getBingDataExecutor.start();
			}
		}
	}

	public static List<String> getDataFromCopy() throws Exception {
		List<String> copiedDataList = new ArrayList<String>();
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Clipboard clipboard = toolkit.getSystemClipboard();
		String result = (String) clipboard.getData(DataFlavor.stringFlavor);
		String lines[] = result.split("\\r?\\n");
		copiedDataList = Arrays.asList(lines);
		return copiedDataList;
	}
}

class GetBingDataExecutor extends Thread {
	private List<String> dataList;
	private String outputFileDirectory;
	private List<String> headerList = Arrays
			.asList(new String[] { "Title", "Address", "Phone Number", "Website Address", "Opening Timming" ,"Map Center Latitude", "Map Longitude"});

	public GetBingDataExecutor(List<String> dataList, String outputFileDirectory) {
		this.dataList = dataList;
		this.outputFileDirectory = outputFileDirectory;
	}

	@Override
	public void run() {
		if (!this.dataList.isEmpty()) {
			this.outputFileDirectory = this.outputFileDirectory + "/" + Thread.currentThread().getName() + ".xls";
			ExcelWriter excelWriter = new ExcelWriter(this.outputFileDirectory, Thread.currentThread().getName());
			excelWriter.writeRecord(headerList);
			excelWriter.closeExcelFile();
			
			System.setProperty("webdriver.chrome.driver", "browser/chromedriver.exe");
			WebDriver driver = new ChromeDriver();
			driver.get("https://www.bing.com/");
			for (String keyword : this.dataList) {
				try {
					List<String> recordData = new ArrayList<String>();
					driver.findElement(By.id("sb_form_q")).sendKeys(keyword);
					driver.findElement(By.id("sb_form_q")).sendKeys(Keys.ENTER);
					waitForNextOperation(3000);
					WebElement bingResultElement = driver.findElement(By.id("lgb_info"));
					WebElement resultTitle = bingResultElement.findElement(By.className("b_entityTitle"));
					recordData.add(formatData(resultTitle.getText().trim()));
					List<WebElement> resultDetailList = bingResultElement.findElements(By.className("b_factrow"));
					if (resultDetailList.size() >= 4) {
						WebElement resultWebsiteAddressElement = resultDetailList.get(0);
						WebElement resultAddressElement = resultDetailList.get(1);
						WebElement resultPhoneNumberElement = resultDetailList.get(2);
						WebElement resultOpeningTimingElement = resultDetailList.get(3);

						recordData.add(formatData(resultAddressElement.getText().trim()));
						recordData.add(formatData(resultPhoneNumberElement.getText().trim()));
						recordData.add(formatData(resultWebsiteAddressElement.getText().trim()));
						recordData.add(formatData(resultOpeningTimingElement.getText().trim()));
					}
					
					try {
						WebElement mapDetailElement = driver.findElement(By.id("dynMap"));
						WebElement mapLocationDetail = mapDetailElement.findElement(By.className("bm_details_overlay"));
						String attribute = mapLocationDetail.getAttribute("data-detailsoverlay");
						Gson gson = new Gson();
						MapDetail mapDetail = gson.fromJson(attribute, MapDetail.class);
						recordData.add(mapDetail.getCenterLatitude());
						recordData.add(mapDetail.getCenterLongitude());
					}catch(Exception exception) {
						System.err.println("Failed to get map data for - " + keyword);
					}
					
					driver.findElement(By.id("sb_form_q")).clear();
					if (recordData != null && recordData.size() > 0) {
						excelWriter = new ExcelWriter(this.outputFileDirectory, Thread.currentThread().getName());
						excelWriter.writeRecord(recordData);
						excelWriter.closeExcelFile();
					}
				} catch (Exception exception) {
					System.err.println("Failed to get data for - " + keyword);
					driver.findElement(By.id("sb_form_q")).clear();
				}
			}
			driver.quit();
		}

	}

	private void waitForNextOperation(int milliseconds) {
		try {
			Thread.sleep(milliseconds);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	public String formatData(String inputString) throws Exception {
		return inputString.replaceAll("\\r?\\n", "");
	}

}
