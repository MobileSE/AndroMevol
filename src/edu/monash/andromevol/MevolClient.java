package edu.monash.andromevol;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import edu.monash.utils.FileInOutUtils;
import edu.monash.utils.DexHunter;

public class MevolClient {

	public static void main(String[] args) {
		try {
			mining(args);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void mining(String[] args) throws IOException {
		String platforms = args[0];
		String frameworkPath = args[1];
		String outBase = "./res";
		FileInOutUtils.directoryCreate(outBase);
		String methodOutPath = outBase + File.separator;
		String fieldOutPath = outBase + File.separator;
		int apiLevel = -1;
		String[] splits = frameworkPath.split("/");
		int splitsSize = splits.length;
		String deviceName = splits[splitsSize - 3];
		methodOutPath += deviceName;
		fieldOutPath += deviceName;
		FileInOutUtils.directoryCreate(methodOutPath);
		FileInOutUtils.directoryCreate(fieldOutPath);
		
		methodOutPath += File.separator + splits[splitsSize - 2];
		fieldOutPath += File.separator + splits[splitsSize - 2];
		FileInOutUtils.directoryCreate(methodOutPath);
		FileInOutUtils.directoryCreate(fieldOutPath); 

		String level = splits[splitsSize - 2].replace("framework-", "");
		apiLevel = Integer.parseInt(level);
		
		methodOutPath += File.separator + "methods.txt";
		fieldOutPath += File.separator + "fields.txt";
		MethodFieldExtractor extractor = new MethodFieldExtractor();
		Set<String> additionalDexes = new DexHunter(frameworkPath).hunt();
		for (String dex : additionalDexes) {
			extractor.transform(dex, platforms, apiLevel);
		}
		FileInOutUtils.writeToFile(extractor.apimethods, methodOutPath);
		FileInOutUtils.writeToFile(extractor.apifields, fieldOutPath);
	}
}
