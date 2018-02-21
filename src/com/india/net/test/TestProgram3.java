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

public class TestProgram3 {
	public static void main(String[] args) throws Exception, IOException {
		TestProgram3 testProgram = new TestProgram3();
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
						RunProgram2 runProgram = new RunProgram2(subDataList, "D:\\TEST");
						runProgram.start();
					}
				} else {
					RunProgram2 runProgram = new RunProgram2(copiedDataList, "D:\\TEST");
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

class RunProgram2 extends Thread {
	private List<String> dataList;
	private String outputFileDirectory;

	public RunProgram2(List<String> dataList, String outputFileDirectory) {
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
				driver.get("https://www.findlatitudeandlongitude.com/find-latitude-and-longitude-from-address/");

				for(String keyword : this.dataList) {
					try {
						driver.findElement(By.name("loc"));
						if (driver instanceof JavascriptExecutor) {
							((JavascriptExecutor)driver).executeScript("document.getElementsByName('loc')[0].maxlength=1000;");
							((JavascriptExecutor)driver).executeScript("document.getElementsByName('loc')[0].value='';");
						}
						
						driver.findElement(By.name("loc")).clear();
						driver.findElement(By.name("loc")).sendKeys(keyword);
						driver.findElement(By.id("load_address_button")).click();
						
						List<WebElement> elementList = driver.findElements(By.className("info_block"));
						if(!elementList.isEmpty() && elementList.size() >= 2) {
							Thread.sleep(5000);
							
							WebElement resultElement1 = elementList.get(1);
							WebElement loadRep = resultElement1.findElement(By.id("load_rep"));
							WebElement resultElement2 = elementList.get(2);
							WebElement latHMSElement = resultElement2.findElement(By.id("lat_HMS")).findElement(By.className("value"));
							WebElement lonHMSElement = resultElement2.findElement(By.id("lon_HMS")).findElement(By.className("value"));
							WebElement latDMDElement = resultElement2.findElement(By.id("lat_DMD")).findElement(By.className("value"));
							WebElement lonDMDElement = resultElement2.findElement(By.id("lon_DMD")).findElement(By.className("value"));
							WebElement latDECElement = resultElement2.findElement(By.id("lat_dec")).findElement(By.className("value"));
							WebElement lonDECElement = resultElement2.findElement(By.id("lon_dec")).findElement(By.className("value"));
							if(!loadRep.getText().contains("Failed")) {
							record.append(keyword.replaceAll(",","#") + ":"
										 + latHMSElement.getText() + ":"
									 	 + lonHMSElement.getText() + ":"
										 + latDMDElement.getText() + ":"
										 + lonDMDElement.getText() + ":"
										 + latDECElement.getText() + ":"
										 + lonDECElement.getText() + ":"
										 + loadRep.getText() + "\n");
							} else {
								record.append(keyword.replaceAll(",","#") + ":"
										 + " :"
									 	 + " :"
										 + " :"
										 + " :"
										 + " :"
										 + " :"
										 + loadRep.getText() + "\n");
							}
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
