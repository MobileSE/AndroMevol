package edu.monash.andromevol;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.monash.utils.CommonUtils;
import edu.monash.utils.FileInOutUtils;
import edu.monash.utils.FrameworkJarExtractor;

public class MevolClient {

	public static void main(String[] args) {
		mining(args);
	}
	
	public static void mining(String[] args) {
		String platforms = args[0];
		Map<String, Set<String>> methodFields = new HashMap<String, Set<String>>();
		String outBase = "./res";
		String frameworkBase = "./frameworks";
		Set<String> devices = new HashSet<String>();
		Set<String> frameworkPaths = FrameworkJarExtractor.extractJarFiles(frameworkBase);
		Map<String, Set<String>> levelDevices = new HashMap<String, Set<String>>();
		List<String[]> csvMethodData = new ArrayList<String[]>();
		List<String[]> csvFieldData = new ArrayList<String[]>();
		Map<String, String[]> apiMethodData = new HashMap<String, String[]>();
		Map<String, String[]> apiFieldData = new HashMap<String, String[]>();
		for (String fpath : frameworkPaths) {
			String methodOutPath = outBase + File.separator;
			String fieldOutPath = outBase + File.separator;
			int apiLevel = -1;
			String[] splits = fpath.split("/");
			String mapKey = "";
			String deviceName = "";
			for (String split : splits) {
				if (split.isEmpty() || split.contains(".jar")) {
					continue;
				} else {
					if (deviceName.isEmpty()) {
						mapKey += split;
						deviceName = split;
						methodOutPath += split;
						fieldOutPath += split;
						devices.add(split);
						FileInOutUtils.directoryCreate(methodOutPath);
						FileInOutUtils.directoryCreate(fieldOutPath);
					} else {
						mapKey += ("_" + split);
						methodOutPath += File.separator + split;
						fieldOutPath += File.separator + split;
						FileInOutUtils.directoryCreate(methodOutPath);
						FileInOutUtils.directoryCreate(fieldOutPath);
					}

					if (split.startsWith("framework-")) {
						String level = split.replace("framework-", "");
						apiLevel = Integer.parseInt(level);
						CommonUtils.put(levelDevices, level, deviceName);
					}
				}
			}
			methodOutPath += File.separator + "methods.txt";
			fieldOutPath += File.separator + "fields.txt";
			String frameworkPath = frameworkBase + fpath;
			MethodFieldExtractor extractor = new MethodFieldExtractor();
			extractor.transform(frameworkPath, platforms, apiLevel);
			FileInOutUtils.writeToFile(extractor.apimethods, methodOutPath);
			FileInOutUtils.writeToFile(extractor.apifields, fieldOutPath);
			methodFields.put(mapKey + "_method", extractor.apimethods);
			methodFields.put(mapKey + "_field", extractor.apifields);
		}
		List<String> sortedDeviceNames = new ArrayList<>(devices);
		for (String mapKey : methodFields.keySet()) {
			if (mapKey.contains("_method")) {
				String[] splits = mapKey.split("_");
				String deviceName = splits[0];
				String apiLevel = splits[1].replace("framework-", "");
				Set<String> methods = methodFields.get(mapKey);
				for (String method : methods) {
					String[] msplits = method.split(">:<");
					String currMethod = msplits[0] + ">";
					String methodKey = apiLevel + "-" + currMethod;
					CommonUtils.methodListInsert(deviceName, methodKey, apiMethodData, sortedDeviceNames, levelDevices.get(apiLevel));
				}
			}
		}
		String[] methodHeaders = new String[sortedDeviceNames.size() + 2];
		String[] fieldHeaders = new String[sortedDeviceNames.size() + 2];
		methodHeaders[0] = "api-level";
		methodHeaders[1] = "method";
		fieldHeaders[0] = "api-level";
		fieldHeaders[1] = "field";
		int idx = 2;
		for (String device : sortedDeviceNames) {
			methodHeaders[idx] = device;
			fieldHeaders[idx] = device;
			idx++;
		}
		for (String key : apiMethodData.keySet()) {
			csvMethodData.add(apiMethodData.get(key));
		}

		Comparator<String[]> comp = (String[] a, String[] b) -> {
		    return a[0].compareTo(b[0]);
		};

		Collections.sort(csvMethodData, comp);
		csvMethodData.add(0, methodHeaders);

		String csvOutPath = outBase + File.separator + "framework-apis.csv";
		FileInOutUtils.writeToCSV(csvMethodData, csvOutPath);

		for (String mapKey : methodFields.keySet()) {
			if (mapKey.contains("_field")) {
				String[] splits = mapKey.split("_");
				String deviceName = splits[0];
				String apiLevel = splits[1].replace("framework-", "");
				Set<String> fields = methodFields.get(mapKey);
				for (String field : fields) {
					String[] msplits = field.split(">:<");
					String currField = msplits[0] + ">";
					String methodKey = apiLevel + "-" + currField;
					CommonUtils.methodListInsert(deviceName, methodKey, apiFieldData, sortedDeviceNames, levelDevices.get(apiLevel));
				}
			}
		}

		for (String key : apiFieldData.keySet()) {
			csvFieldData.add(apiFieldData.get(key));
		}

		Comparator<String[]> compField = (String[] a, String[] b) -> {
		    return a[0].compareTo(b[0]);
		};

		Collections.sort(csvFieldData, compField);
		csvFieldData.add(0, fieldHeaders);

		String csvFieldPath = outBase + File.separator + "framework-fields.csv";
		FileInOutUtils.writeToCSV(csvFieldData, csvFieldPath);
	}
}
