package edu.monash.utils;

import java.util.HashSet;
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
}
