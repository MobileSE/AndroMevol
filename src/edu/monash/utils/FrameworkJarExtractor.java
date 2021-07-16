package edu.monash.utils;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class FrameworkJarExtractor 
{
	public static Set<String> extractJarFiles(String repoPath)
	{
		return getAllJarFiles(repoPath, repoPath, "");
	}
	
	public static Set<String> extractJarFiles(String repoPath, String prefix)
	{
		return getAllJarFiles(repoPath, repoPath, prefix);
	}
	
	private static Set<String> getAllJarFiles(String repoPath, String dir, String prefix)
	{
		Set<String> javaFiles = new HashSet<String>();
		
		File root = new File(dir);
		
		for (File file : root.listFiles())
		{
			if (file.isDirectory())
			{
				javaFiles.addAll(getAllJarFiles(repoPath, file.getAbsolutePath(), prefix));
			}
			else
			{
				String path = file.getAbsolutePath();
				if (path.endsWith("framework.jar"))
				{
					path = path.substring(path.indexOf(repoPath));
					String javaFilePath = path.replace(repoPath, "");
					
					if (prefix == null || prefix.isEmpty() || javaFilePath.startsWith(prefix))
					{
						javaFiles.add(javaFilePath);
					}
				}
			}
		}
		
		return javaFiles;
	}
}
