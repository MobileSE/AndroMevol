package edu.monash.andromevol;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import soot.G;
import soot.PackManager;
import soot.Transform;
import soot.options.Options;

public class MethodFieldExtractor {
	public Set<String> apimethods = new HashSet<String>();
	public Set<String> apifields = new HashSet<String>();
	
	public void transform(String apkOrDexPath, String androidJars, int apiLevel)
	{
		G.reset();
		
		String[] args =
        {
			"-process-dir", apkOrDexPath,
            "-ire",
			"-pp",
			"-keep-line-number",
			"-allow-phantom-refs",
			"-w",
			"-p", "cg", "enabled:false",
			"-src-prec", "apk"
        };
			
		Options.v().set_output_format(Options.output_format_none);
//		Options.v().set_verbose(false);  // get rid of verbose output.
		if (-1 != apiLevel)
			Options.v().set_force_android_jar(androidJars + File.separator + "android-" + apiLevel + File.separator + "android.jar");
		else
			Options.v().set_android_jars(androidJars);
		
		MiningMethodFieldTransformer transformer = new MiningMethodFieldTransformer();
		
		PackManager.v().getPack("wjtp").add(new Transform("wjtp.MiningMethodFieldTransformer", transformer));
		soot.Main.main(args);
		
		apimethods.addAll(transformer.apimethods);
		apifields.addAll(transformer.apifields);
		
		G.reset();
	}
}
