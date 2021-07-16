package edu.monash.utils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CommonUtils {
	public static void put(Map<String, Set<String>> dest, Map<String, Set<String>> src)
	{
		for (Map.Entry<String, Set<String>> entry : src.entrySet())
		{
			String cls = entry.getKey();
			Set<String> set2 = entry.getValue();
			
			if (dest.containsKey(cls))
			{
				Set<String> set1 = dest.get(cls);
				set1.addAll(set2);
				dest.put(cls, set1);
			}
			else
			{
				Set<String> set1 = new HashSet<String>();
				set1.addAll(set2);
				dest.put(cls, set1);
			}
		}
	}

	public static <T> void put(Map<String, Set<T>> map1, String key, T value)
	{
		if (map1.containsKey(key))
		{
			Set<T> values = map1.get(key);
			values.add(value);
			map1.put(key, values);
		}
		else
		{
			Set<T> values = new HashSet<T>();
			values.add(value);
			map1.put(key, values);
		}
	}

	public static void methodListInsert(List<String[]> csvList, String deviceName, 
			String apiLevel, String methodField, 
			List<String> devices, Set<String> deviceProvided) {
		if (csvList.isEmpty()) {
			newMethodInsert(csvList, deviceName, apiLevel, methodField, devices, deviceProvided);
		} else {
			boolean methodExists = false;
			for (String[] row : csvList) {
				if (row[0].equals(apiLevel) && row[1].equals(methodField)) {
					methodExists = true;
					int idx = 2;
					for (String device : devices) {
						if (device.equals(deviceName)) {
							row[idx++] = "1";
						}
						idx++;
					}
				}
			}

			if (!methodExists) {
				newMethodInsert(csvList, deviceName, apiLevel, methodField, devices, deviceProvided);
			}
		}
	}

	public static void methodListInsert(String deviceName, String methodFieldKey,
			Map<String, String[]> methodFieldData, List<String> devices, Set<String> deviceProvided) {
		if (methodFieldData.isEmpty() || !methodFieldData.containsKey(methodFieldKey)) {
			int dashPos = methodFieldKey.indexOf("-");
			String apiLevel = methodFieldKey.substring(0, dashPos);
			String methodField = methodFieldKey.substring(dashPos + 1);
			int rowLength = devices.size() + 2;
			String[] currRow = new String[rowLength];
			currRow[0] = apiLevel;
			currRow[1] = methodField;
			int idx = 2;
			for (String device : devices) {
				if (device.equals(deviceName)) {
					currRow[idx++] = "1";
				} else if (deviceProvided.contains(device)) {
					currRow[idx++] = "0";
				} else {
					currRow[idx++] = "-1";
				}
			}
			methodFieldData.put(methodFieldKey, currRow);
		} else {
			String[] currRow = methodFieldData.get(methodFieldKey);
			int idx = 2;
			for (String device : devices) {
				if (device.equals(deviceName)) {
					currRow[idx] = "1";
				} else {
					idx += 1;
				}
			}
			methodFieldData.put(methodFieldKey, currRow);
		}
	}

	private static void newMethodInsert(List<String[]> csvList, String deviceName,
			String apiLevel, String methodField,
			List<String> devices, Set<String> deviceProvided) {
		int rowLength = devices.size() + 2;
		String[] currRow = new String[rowLength];
		currRow[0] = apiLevel;
		currRow[1] = methodField;
		int idx = 2;
		for (String device : devices) {
			if (device.equals(deviceName)) {
				currRow[idx++] = "1";
			} else if (deviceProvided.contains(device)) {
				currRow[idx++] = "0";
			} else {
				currRow[idx++] = "-1";
			}
		}
		csvList.add(currRow);
	}
}
