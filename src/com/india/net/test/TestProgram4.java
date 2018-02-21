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
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

public class TestProgram4 {
	public static void main(String[] args) throws Exception, IOException {
		TestProgram3 testProgram = new TestProgram3();
			List<String> copiedDataList = testProgram.getDataFromCopy();
			if(!copiedDataList.isEmpty()) {
				int maxThread = 4;
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
						RunProgram4 runProgram = new RunProgram4(subDataList, "D:\\TEST");
						runProgram.start();
					}
				} else {
					RunProgram4 runProgram = new RunProgram4(copiedDataList, "D:\\TEST");
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

class RunProgram4 extends Thread {
	private List<String> dataList;
	private String outputFileDirectory;

	public RunProgram4(List<String> dataList, String outputFileDirectory) {
		this.dataList = dataList;
		this.outputFileDirectory = outputFileDirectory;
	}

	@Override
	public void run() {
		if(!this.dataList.isEmpty()) {
			File outputFile = new File(this.outputFileDirectory + "/" + Thread.currentThread().getName() + ".csv");
			FileWriter fileWriter = null;
			BufferedWriter bufferedWriter = null;
			try {
				fileWriter = new FileWriter(outputFile, true);
				bufferedWriter = new BufferedWriter(fileWriter);

				StringBuilder record = new StringBuilder();
				System.setProperty("webdriver.chrome.driver", "browser/chromedriver.exe");
				WebDriver driver = new ChromeDriver();
				driver.get("http://www.gpsvisualizer.com/geocode/");

				for(String keyword : this.dataList) {
					try {
						driver.findElement(By.id("address_box"));
						driver.findElement(By.id("address_box")).sendKeys(keyword);
						if (driver instanceof JavascriptExecutor) {
						    ((JavascriptExecutor)driver).executeScript("StartGeocoding();");
						} else {
						    throw new IllegalStateException("This driver does not support JavaScript!");
						}
						
						Thread.sleep(5000);
						
						List<WebElement> elementList = driver.findElements(By.className("geocode_data"));
						if(!elementList.isEmpty() && elementList.size() == 6) {
							WebElement resultElement = elementList.get(5);
							record.append(keyword.replaceAll(",", "#") + ":"
										 + resultElement.getText() + "\n");
						}else {
							record.append(keyword.replaceAll(",", "#") + ":"
									 + " No Location Found\n");
						}
					} catch(Exception exception) {
						exception.printStackTrace();
						System.out.println("No address found - " + keyword);
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
