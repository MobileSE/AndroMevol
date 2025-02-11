package edu.monash.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import com.opencsv.CSVWriter;

public class FileInOutUtils {
    public static void writeToFile(String content, String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(content);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeToFile(Set<String> sets, String filePath) {
    	StringBuilder sb = new StringBuilder();
    	for (String content : sets) {
    		sb.append(content + "\n");
    	}
    	writeToFile(sb.toString(), filePath);
    }

    public static void writeToFile(List<String> sets, String filePath) {
    	StringBuilder sb = new StringBuilder();
    	for (String content : sets) {
    		sb.append(content + "\n");
    	}
    	writeToFile(sb.toString(), filePath);
    }

    public static void directoryCreate(String dirPath) {
    	File file = new File(dirPath);
    	if (!file.exists()) {
    		file.mkdir();
    	}
    }

    public static void writeToCSV(List<String[]> csvData, String csvPath) {
    	File file = new File(csvPath);
    	try {
    		// create FileWriter object with file as parameter
    		FileWriter outputfile = new FileWriter(file);

    		// create CSVWriter object filewriter object as parameter
    		CSVWriter writer = new CSVWriter(outputfile);
    		writer.writeAll(csvData);

    		// closing writer connection
    		writer.close();
    	} catch (IOException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}
    }
}
