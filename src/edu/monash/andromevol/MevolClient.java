package edu.monash.andromevol;

import edu.monash.utils.FileInOutUtils;

public class MevolClient {

	public static void main(String[] args) {
		mining(args);
	}
	
	public static void mining(String[] args) {
		MethodFieldExtractor extractor = new MethodFieldExtractor();
		extractor.transform(args[0], args[1], Integer.parseInt(args[2]));
//		FileInOutUtils.writeToFile(extractor.apimethods, args[3]);
//		FileInOutUtils.writeToFile(extractor.apifields, args[4]);
	}
}
