package edu.monash.andromevol;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Body;
import soot.Modifier;
import soot.PatchingChain;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.Stmt;
import soot.tagkit.DoubleConstantValueTag;
import soot.tagkit.FloatConstantValueTag;
import soot.tagkit.IntegerConstantValueTag;
import soot.tagkit.LongConstantValueTag;
import soot.tagkit.SignatureTag;
import soot.tagkit.StringConstantValueTag;
import soot.tagkit.Tag;
import soot.util.Chain;

public class MiningMethodFieldTransformer extends SceneTransformer {
	public Set<String> apimethods = new HashSet<String>();
	public Set<String> apifields = new HashSet<String>();

	private void extract(Body b)
	{
		String callerMethodSig = b.getMethod().getSignature();
		
		System.out.println(callerMethodSig);
		
		PatchingChain<Unit> units = b.getUnits();
		
		for (Iterator<Unit> unitIter = units.snapshotIterator(); unitIter.hasNext(); )
		{
			Stmt stmt = (Stmt) unitIter.next();
			
			if (stmt.containsInvokeExpr())
			{
				SootMethod sootMethod = stmt.getInvokeExpr().getMethod();
				String methodSig = sootMethod.getSignature();
				System.out.println(methodSig);
			} else if (stmt instanceof AssignStmt) {
				System.out.println(stmt.toString());
			}
		}
	}
	
	protected void internalBodyTransform(Body b) 
	{
		extract(b);
	}

	@Override
	protected void internalTransform(String arg0, Map<String, String> arg1) 
	{
		Chain<SootClass> sootClasses = Scene.v().getApplicationClasses();
		for (Iterator<SootClass> iter = sootClasses.snapshotIterator(); iter.hasNext(); )
		{
			SootClass sc = iter.next();
			
			if (sc.getName().startsWith("android.support."))
			{
				continue;
			}

			SootClass superCls = sc;
			StringBuilder superClsNames = new StringBuilder();
			superClsNames.append(sc.getName().replace("$", "."));
			boolean firstSuperCls = true;
			while (superCls.hasSuperclass()) {
				superCls = superCls.getSuperclass();
				if (!superCls.getName().equals("java.lang.Object")) {
					if (firstSuperCls) {
						firstSuperCls = false;
						superClsNames.append(":" + superCls.getName().replace("$", "."));
					} else {
						superClsNames.append("," + superCls.getName().replace("$", "."));
					}
				}
			}

			Chain<SootClass> interfaces = sc.getInterfaces();
			for (SootClass cls : interfaces) {
				if (firstSuperCls) {
					firstSuperCls = false;
					superClsNames.append(":" + cls.getName().replace("$", "."));
				} else {
					superClsNames.append("," + cls.getName().replace("$", "."));
				}

				SootClass interfaceSuper = cls;
				while (interfaceSuper.hasSuperclass()) {
					interfaceSuper = interfaceSuper.getSuperclass();
					if (!interfaceSuper.getName().equals("java.lang.Object")) {
						if (firstSuperCls) {
							firstSuperCls = false;
							superClsNames.append(":" + interfaceSuper.getName().replace("$", "."));
						} else {
							superClsNames.append("," + interfaceSuper.getName().replace("$", "."));
						}
					}
				}
			}
			List<SootMethod> methods = sc.getMethods();
			for (int i = 0; i < methods.size(); i++)
			{
				String sig = methods.get(i).getSignature().replace("$", ".");
				String mods = Modifier.toString(methods.get(i).getModifiers());
				if (!mods.isEmpty() && mods.contains("private")) {
					continue;
				}
				String apiMethod = sig + ":<" + mods + ">:<" + superClsNames.toString() + ">";
				apimethods.add(apiMethod);
			}
			
			Chain<SootField> chain  = sc.getFields();
			for (SootField field : chain) {
				String fieldDec = field.getSignature().replace("$", ".");
				String mods = Modifier.toString(field.getModifiers());
				if (!mods.isEmpty() && mods.contains("private")) {
					continue;
				}
				StringBuilder tagValues = new StringBuilder();
				List<Tag> tags = field.getTags();
				boolean firstValue = true;
				String commaLiteral = "";
				for (Tag tag: tags) {
					String tagValue = "";
					if (!tag.getName().isEmpty()) {
						if (tag instanceof StringConstantValueTag) {
							StringConstantValueTag strConstant = (StringConstantValueTag) tag;
							tagValue = strConstant.getStringValue();
						} else if (tag instanceof SignatureTag) {
							SignatureTag sigTag = (SignatureTag) tag;
							tagValue = sigTag.getSignature();
						} else if (tag instanceof IntegerConstantValueTag) {
							IntegerConstantValueTag intConstant = (IntegerConstantValueTag) tag;
							tagValue = Integer.toString(intConstant.getIntValue());
						} else if (tag instanceof LongConstantValueTag) {
							LongConstantValueTag longConstant = (LongConstantValueTag) tag;
							tagValue = Long.toString(longConstant.getLongValue());
						} else if (tag instanceof FloatConstantValueTag) {
							FloatConstantValueTag floatConstant = (FloatConstantValueTag) tag;
							tagValue = Float.toString(floatConstant.getFloatValue());
						} else if (tag instanceof DoubleConstantValueTag) {
							DoubleConstantValueTag doubleConstant = (DoubleConstantValueTag) tag;
							tagValue = Double.toString(doubleConstant.getDoubleValue());
						} else {
							continue;
						}
						if (firstValue) {
							tagValues.append(commaLiteral + tag.getName() + ":" + tagValue);
							commaLiteral = ",";
							firstValue = false;
						} else {
							tagValues.append(commaLiteral + tag.getName() + ":" + tagValue);
						}
					}
				}
				if (tagValues.toString().isEmpty()) {
					String apifield = fieldDec + ":<" + mods + ">:<" + "" + ">:<" + superClsNames.toString() + ">";
					apifields.add(apifield);
				} else {
					String apifield = fieldDec + ":<" + mods + ">:<" + tagValues.toString().replace("\n", " ") + ">:<" + superClsNames.toString() + ">";
					apifields.add(apifield);
				}
			}
		}
		
	}
}
