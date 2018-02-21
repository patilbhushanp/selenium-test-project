package com.stock.net.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class KeywordReader {

	public static String KEYWORD_FILE_LOCATION = "../conf/keywords.txt";

	public static List<String> getAllKeywords() {
		List<String> list = new ArrayList<String>();
		try {
			File keywordFile = new File(KEYWORD_FILE_LOCATION);
			if (keywordFile.isFile()) {
				FileReader fileReader = new FileReader(keywordFile);
				BufferedReader bufferedReader = new BufferedReader(fileReader);
				while (true) {
					String lineData = bufferedReader.readLine();
					if (lineData == null)
						break;
					list.add(lineData);
				}
				bufferedReader.close();
			}
		} catch (Exception exception) {
			System.err.println("Failed to get keyword with reason " + exception.getMessage());
		}
		return list;
	}
}
