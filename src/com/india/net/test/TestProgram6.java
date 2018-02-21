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

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

public class TestProgram6 {
	public static void main(String[] args) throws Exception, IOException {
		TestProgram6 testProgram = new TestProgram6();
			List<String> copiedDataList = testProgram.getDataFromCopy();
			if(!copiedDataList.isEmpty()) {
				int maxThread = 400000;
				int totalRecord = copiedDataList.size();
				int eachThreadDataSize = totalRecord / maxThread;
				if(eachThreadDataSize > 0) {
					for(int i = 0; i < maxThread; i++) {
						List<String> subDataList = new ArrayList<String>();
						if(maxThread == (i+1)) {
							subDataList.addAll(copiedDataList.subList(i * eachThreadDataSize, copiedDataList.size()));
						}else {
							subDataList.addAll(copiedDataList.subList(i * eachThreadDataSize, (i + 1) * eachThreadDataSize));
						}
						RunProgram6 runProgram = new RunProgram6(subDataList, "D:\\TEST");
						runProgram.start();
					}
				} else {
					RunProgram6 runProgram = new RunProgram6(copiedDataList, "D:\\TEST");
					runProgram.run();
				}
			}
	}

	public List<String> getDataFromCopy()throws Exception{
		List<String> copiedDataList = new ArrayList<String>();
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Clipboard clipboard = toolkit.getSystemClipboard();
		String result = (String) clipboard.getData(DataFlavor.stringFlavor);
		String lines[] = result.split("\\r?\\n");
		copiedDataList = Arrays.asList(lines);
		return copiedDataList;
	}
}

class RunProgram6 extends Thread {
	private List<String> dataList;
	private String outputFileDirectory;

	public RunProgram6(List<String> dataList, String outputFileDirectory) {
		this.dataList = dataList;
		this.outputFileDirectory = outputFileDirectory;
	}

	@Override
	public void run() {
		if(!this.dataList.isEmpty()) {
			File outputFile = new File(this.outputFileDirectory + "/" + Thread.currentThread().getName() + ".csv");
			FileWriter fileWriter = null;
			BufferedWriter bufferedWriter = null;
			String removeTab = "\t";
			try {
				fileWriter = new FileWriter(outputFile, true);
				bufferedWriter = new BufferedWriter(fileWriter);

				StringBuilder record = new StringBuilder();
				System.setProperty("webdriver.chrome.driver", "browser/chromedriver.exe");
				WebDriver driver = new ChromeDriver();
				driver.get("https://www.google.co.in/");
				record.append("Company Name\t");
				record.append("Company Address\t");
				record.append("Company Phone Number\t");
				record.append("Opening Timing\t");
				record.append("Business Owner Link\t");
				record.append("Company wedsite URL\t");
				record.append("Company Google Map URL\n");
				for(String keyword : this.dataList) {
					try {
						Thread.sleep(5000);
						System.out.println("Starting processing for Search Text : " + keyword);
						
						driver.findElement(By.id("lst-ib")).clear();
						driver.findElement(By.id("lst-ib")).sendKeys(keyword);
						driver.findElement(By.id("lst-ib")).sendKeys(Keys.ENTER);
						
						String address = driver.findElement(By.className("_Xbe")).getText();
						String phoneNumber = driver.findElement(By.className("kno-fv")).getText();
						String openTimings = "";
						String siteAddress = "";
						String businessOwnerLink = "";
						
						try {
							List<WebElement> addressList = driver.findElements(By.className("_ldf"));
							for(WebElement addElement : addressList) {
								siteAddress += "\t" + addElement.findElement(By.tagName("a")).getAttribute("href").replaceAll(removeTab, " ") + "\t";	
							}
						}catch(Exception exception) {
							System.err.println("No Sites and Google Map link found for - " + keyword);
						}
						
						try {
							driver.findElement(By.className("_vap")).click();
							WebElement element = driver.findElement(By.className("_Y0c"));
							List<WebElement> trList = element.findElements(By.tagName("tr"));
							for(WebElement tr : trList) {
								List<WebElement> tdList = tr.findElements(By.tagName("td"));
								String day = "";
								String timing = "";
								if(tdList.size() == 2) {
									day = tdList.get(0).getText();
									timing = tdList.get(1).getText();
								}
								openTimings += day + ":" +timing + "#";
							}							
						}catch(Exception exception) {
							System.err.println("Opening Timing is not available for - " + keyword);
						}
						try {
							businessOwnerLink = driver.findElement(By.xpath("//a[contains(.,'Own this business?')]")).getAttribute("href");
						}catch (Exception exception) {
							System.err.println("Business Owner Link is not found for - " + keyword);
						}
												
						keyword = keyword.replaceAll(removeTab, " ");
						address = address.replaceAll(removeTab, " ");
						phoneNumber = phoneNumber.replaceAll(removeTab, " ");					
						record.append(keyword + "\t");
						record.append(address + "\t");
						record.append(phoneNumber + "\t");
						record.append(openTimings);
						record.append(businessOwnerLink);
						record.append(siteAddress + "\n");
					} catch(Exception exception) {
						exception.printStackTrace();
						System.err.println("No address or phone number found for - " + keyword);
					}
				}
				bufferedWriter.write(record.toString());
				driver.quit();
				System.out.println("Output File Path - " + outputFile.getAbsolutePath());
			}catch(IOException exception) {
				System.out.println("Failed to Open File - " + outputFile.getAbsolutePath());
				exception.printStackTrace();
				System.exit(0);
			}finally {
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

	public String formatData(String inputString)throws Exception{
		return inputString.replaceAll("\\r?\\n", "\t");
	}

}
