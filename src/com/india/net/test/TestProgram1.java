package com.india.net.test;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

public class TestProgram1 implements ClipboardOwner  {

	public static void main(String[] args) throws Exception, IOException {
		StringBuilder record = new StringBuilder();
		TestProgram1 testProgram1 = new TestProgram1();
		List<String> copiedDataList = testProgram1.getDataFromCopy();
		
		System.setProperty("webdriver.chrome.driver", "browser/chromedriver.exe");
		WebDriver driver = new ChromeDriver();
		driver.get("https://www.zaubacorp.com/company");
		
		for(String keyword : copiedDataList) {
			driver.findElement(By.id("searchid")).sendKeys(keyword);
			driver.findElement(By.id("edit-submit--3")).click();
			Thread.sleep(10000);
			
			int counter = 1;
			while(true) {
				try {
					WebElement element = driver.findElement(By.id("package" + counter++));
					if (element == null) {
						break;
					} else {
						record.append(keyword + "\t" + testProgram1.formatData(element.getText()) + "\n");
					}
				}catch(Exception exception) {
					break;
				}
			}
			testProgram1.setDataFromCopy(record.toString());
		}
		driver.quit();
	}
	
	public void setDataFromCopy(String data)throws Exception{
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Clipboard clipboard = toolkit.getSystemClipboard();
		StringSelection stringSelection = new StringSelection(data);
		clipboard.setContents(stringSelection, this);
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

	public String formatData(String inputString)throws Exception{
		return inputString.replaceAll("\\r?\\n", "\t");
	}
	
	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
	}

}
