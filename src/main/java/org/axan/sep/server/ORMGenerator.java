package org.axan.sep.server;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.axan.eplib.orm.Class;
import org.axan.eplib.orm.Class.Field;
import org.axan.eplib.orm.ClassGeneratorListener;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;
import org.axan.eplib.utils.Basic;
import org.axan.eplib.utils.Reflect;
import org.axan.eplib.yaml.YamlConfigFile;
import org.axan.sep.common.Protocol.eBuildingType;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.Protocol.eSpecialUnitType;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.SEPUtils.RealLocation;
import org.axan.sep.common.db.IGameConfig;
import org.axan.sep.common.db.SEPCommonDB;
import org.neo4j.graphdb.Node;

class ORMGenerator
{
	
	public static void main(String[] args)
	{
		SQLiteORMGenerator gen;
		try
		{		
			File ormTemp = File.createTempFile("SEP_ORM", ".db");
			ormTemp.deleteOnExit();
			
			String baseDir = "/media/data/code/Java_Workspace/Space-Empire-Pulsar/src/main/java/";
			String interfacePackageName = IGameConfig.class.getPackage().getName();
			String sqlitePackageName = SEPCommonDB.class.getPackage().getName();
			final String ormPackageName = sqlitePackageName+".orm";
			File interfaceOutputFile = new File(baseDir+interfacePackageName.replace('.', '/'));
			File outputFile = new File(baseDir+ormPackageName.replace('.', '/'));
			URL dbFileURL = Reflect.getResource(sqlitePackageName, "SEPSQLiteDB.sql");
			File configFile = new File(Reflect.getResource(sqlitePackageName, "SEPSQLDB.ORM.yaml").getFile());
			
			YamlConfigFile cfg = YamlConfigFile.open(configFile);
			
			gen = new SQLiteORMGenerator(ormTemp, interfaceOutputFile, outputFile);
			
			gen.importFile(new File(dbFileURL.getFile()));
			
			Map<String, Collection<String>> inheritances;
			
			inheritances = cfg.get("inheritances", Map.class);
			
			gen.generate(new ClassGeneratorListener()
			{
				
				@Override
				public boolean skipClass(Class c)
				{
					return (c.getUpperName().matches("GameConfig"));
				}
				
				@Override
				public void genBasicImports(Class c, PrintStream psbi, PrintStream psbc)
				{					
					psbi.format("import %s;\n", Map.class.getName());
					psbc.format("import %s;\n", Node.class.getName());
					psbc.format("import %s;\n", Map.class.getName());
				}
				
				@Override
				public void genImports(Class c, PrintStream psui, PrintStream psuc)
				{
					Set<String> stringImports = new TreeSet<String>();
					
					Set<java.lang.Class<?>> importedClass = new HashSet<java.lang.Class<?>>();
										
					Collections.addAll(importedClass, Node.class, org.axan.sep.common.db.IGameConfig.class, Map.class, HashMap.class);
					
					List<Field> fields = c.getAllFields();
					
					for(Field field : fields)
					{
						if (field.getLowerName().endsWith("_x"))
						{
							importedClass.add(getLocationRelationField(field).getType());
						}
						else if (field.getLowerName().matches("type"))
						{
							importedClass.add(getEnumType(c));
						}
						else if (! (field.getType().isArray() ? field.getType().getComponentType() : field.getType()).getPackage().equals(Package.getPackage("java.lang")))
						{
							importedClass.add(field.getType().isArray() ? field.getType().getComponentType() : field.getType());
						}
					}
					
					for(java.lang.Class<?> clazz : importedClass)
					{
						stringImports.add(clazz.getCanonicalName());
					}
					
					for(String clazz : stringImports)
					{
						psuc.format("import %s;\n", clazz);
						psui.format("import %s;\n", clazz);
					}					
				}
				
				@Override
				public java.lang.Class<?> getBasicStatementClass()
				{
					return Node.class;
				}
				
				@Override
				public void genBasicCtorImplementation(StringBuffer ctorFilter, StringBuffer ctorFields, StringBuffer booleanFields, java.lang.Class<? extends Throwable> exceptionClass, PrintStream psbc)
				{
					if (ctorFilter.length() + ctorFields.length() > 0)
					{
						psbc.format("%s", ctorFields.toString().replaceAll("\t\t\t", "\t\t"));
					}
				}
				
				public String getBasicFieldFromStatement(Field field)
				{
					return String.format("stmnt.hasProperty(\"%s\") ? %s.class.cast(stmnt.getProperty(\"%s\")) : null;", field.getLowerName(), field.getType().getSimpleName(), field.getLowerName());
				}
				
				@Override
				public String extendsClause(Class c)
				{
					if (!c.getUpperName().matches("VersionedUnit") && c.isInstanceOf(c.getPackage(), "VersionedUnit") != null)
					{
						return "VersionedUnit";
					}
					
					if (!c.getUpperName().matches("VersionedProductiveCelestialBody") && c.isInstanceOf(c.getPackage(), "VersionedProductiveCelestialBody") != null)
					{
						return "VersionedProductiveCelestialBody";
					}
					
					return "";
				}
				
				@Override
				public String superCall(Class c)
				{
					if (!c.getUpperName().matches("VersionedUnit") && c.isInstanceOf(c.getPackage(), "VersionedUnit") != null)
					{
						return String.format("base%sProxy", c.getUpperName());
					}
					
					if (!c.getUpperName().matches("VersionedProductiveCelestialBody") && c.isInstanceOf(c.getPackage(), "VersionedProductiveCelestialBody") != null)
					{
						return String.format("base%sProxy", c.getUpperName());
					}
					
					return "";
				}
				
				@Override
				public String fieldsDeclarations(Class c)
				{
					StringBuffer sb = new StringBuffer();
					
					for(Field f : c.getSpecificFields())
					{
						if (f.getLowerName().endsWith("_x"))
						{
							Field nf = getLocationRelationField(f);
							sb.append(String.format("\tprivate final %s %s;\n", nf.getType().getSimpleName(), nf.getLowerName()));
						}
						
						if (f.getLowerName().matches("type"))
						{
							Field nf = wrapField(c, f);
							sb.append(String.format("\tprivate final %s %s;\n", nf.getType().getSimpleName(), nf.getLowerName()));
						}
					}
					
					return sb.toString();
				}
				
				private Field wrapField(Class c, Field f)
				{
					if (f.getLowerName().endsWith("_x"))
					{						
						return getLocationRelationField(f);
					}
					else if (f.getLowerName().endsWith("_y") || f.getLowerName().endsWith("_z"))
					{
						// Ignore
						return null;
					}
					else if (f.getLowerName().matches("type"))
					{
						java.lang.Class<? extends Enum> enumType = getEnumType(c);
						return new Field(enumType, f.getLowerName(), f.isPrimaryKey());
					}
					
					return f;
				}
				
				@Override
				public String fullConstructorParameter(Class c, Field f)
				{
					Field wf = wrapField(c, f);
					if (wf == null) return "";
					return super.fullConstructorParameter(c, wf);
				}
				
				/*
				@Override
				public String fullConstructorField(Class c, Field f)
				{
					Field wf = wrapField(c, f);
					if (wf == null || f.equals(wf)) return "";
					return String.format("this.%s = %s;", wf.getLowerName(), wf.getLowerName());
				}
				*/
				
				/*
				@Override
				public String fullConstructorProxyParameter(Class c, Field f)
				{
					Field wf = wrapField(c, f);
					if (wf == null) return "";
					
					return super.fullConstructorProxyParameter(c, wf);
				}
				*/
				
				@Override
				public String fullConstructorBaseParameter(Class c, Field f)
				{
					Field wf = wrapField(c, f);
					
					if (wf == null) return "";
					
					if (Location.class.equals(wf.getType()) || RealLocation.class.equals(wf.getType()))
					{
						return String.format("%s == null ? null : %s.x, %s == null ? null : %s.y, %s == null ? null : %s.z", wf.getLowerName(), wf.getLowerName(), wf.getLowerName(), wf.getLowerName(), wf.getLowerName(), wf.getLowerName());
					}
					else if (wf.getLowerName().matches("type"))
					{
						return String.format("%s.toString()", wf.getLowerName());
					}
					
					return super.fullConstructorBaseParameter(c, wf);
				}
				
				@Override
				public String constructorAdditionalDeclaration(Class c)
				{
					if (c.isInstanceOf(c.getPackage(), "Unit") != null)
					{
						return String.format("%s config", org.axan.sep.common.db.IGameConfig.class.getSimpleName());
					}
					
					return "";
				}
				
				@Override
				public String constructorAdditionalParams(Class c)
				{
					if (c.isInstanceOf(c.getPackage(), "Unit") != null)
					{
						return "config";
					}
					
					return "";
				}
				
				@Override
				public void genOtherMethods(Class c, PrintStream psui, PrintStream psuc, PrintStream psbi, PrintStream psbc)
				{
					super.genOtherMethods(c, psui, psuc, psbi, psbc);
					psbi.println("\tpublic Map<String, Object> getNode();");
					psui.println("\tpublic Map<String, Object> getNode();");
					
					psuc.println("\t@Override");
					psuc.println("\tpublic Map<String, Object> getNode()");
					psuc.println("\t{");
					psuc.format("\t\treturn base%sProxy.getNode();\n", c.getUpperName());
					psuc.println("\t}");
					psuc.println();
					
					psbc.println("\t@Override");
					psbc.println("\tpublic Map<String, Object> getNode()");
					psbc.println("\t{");
					psbc.println("\t\tMap<String, Object> result = new HashMap<String, Object>();");
					for(Field field : c.getAllFields())
					{
						psbc.format("\t\tresult.put(\"%s\", get%s());\n", field.getLowerName(), field.getUpperName());
					}
					psbc.println("\t\treturn result;");
					psbc.println("\t}");
					psbc.println();					
				}
				
				/*
				@Override
				public String constructorCreateProxy(Class c, Class sup)
				{
					return String.format("this.%sProxy = new %s(stmnt, config);", sup.getLowerName(), sup.getUpperName());
				}
				*/	
				
				/*
				@Override
				public String constructorSuperCall(Class c)
				{
					return "super(stmnt, config);";
				}
				*/
				
				@Override
				public String constructorFields(Class c)
				{
					StringBuffer sb = new StringBuffer();
					
					for(Field f : c.getSpecificFields())
					{
						Field nf = wrapField(c, f);
						if (nf == null) continue;
						
						if (f.getLowerName().endsWith("_x"))
						{
							sb.append(String.format("\t\tthis.%s = (base%sProxy.get%s_x() == null ? null : new %s(base%sProxy.get%s_x(), base%sProxy.get%s_y(), base%sProxy.get%s_z()));\n", nf.getLowerName(), c.getUpperName(), nf.getUpperName(), nf.getType().getSimpleName(), c.getUpperName(), nf.getUpperName(), c.getUpperName(), nf.getUpperName(), c.getUpperName(), nf.getUpperName()));
						}
						
						if (f.getLowerName().matches("type"))
						{
							sb.append(String.format("\t\tthis.%s = %s.valueOf(base%sProxy.get%s());\n", nf.getLowerName(), nf.getType().getSimpleName(), c.getUpperName(), nf.getUpperName()));
						}
					}
					
					return sb.toString();
				}
				
				private Field getLocationRelationField(Field field)
				{
					String[] s = Basic.split(field.getLowerName(), "_");
					if (field.getType().equals(int.class) || field.getType().equals(Integer.class))
					{
						return new Field(Location.class, s[0], field.isPrimaryKey());
					}
					else
					{
						return new Field(RealLocation.class, s[0], field.isPrimaryKey());
					}
				}
				
				private Map<String, Boolean> unprefixedVersionedTypes = new HashMap<String, Boolean>();
				
				private Map<String, Boolean> getUnprefixedVersionedTypes()
				{
					if (unprefixedVersionedTypes.isEmpty())
					{
						/*
						unprefixedVersionedTypes.put("Building", true);
						unprefixedVersionedTypes.put("Government", true);
						unprefixedVersionedTypes.put("Diplomacy", true);
						unprefixedVersionedTypes.put("UnitArrivalLog", true);
						unprefixedVersionedTypes.put("UnitEncounterLog", true);
						*/
					}
					
					return unprefixedVersionedTypes;
				}
				
				private Class getVersionedClass(Class c)
				{
					// IF (isVersionedTypeUnprefixed(c)) return c
					// IF (c.startsWith("Versioned")) return c
					// IF (exist("Versioned"+c)) return "Versioned"+c
					// return NULL
					
					if (c.getUpperName().startsWith("Versioned")) return c;
					if (getUnprefixedVersionedTypes().get(c.getUpperName()) != null && getUnprefixedVersionedTypes().get(c.getUpperName())) return c;
					// Versioned class must be a sub class.
					for(Class sub: c.getSubers())
					{
						if (sub.getUpperName().matches("Versioned"+c.getUpperName())) return sub;
					}
					
					return null;
				}
				
				private Map<String, java.lang.Class<? extends Enum>> enumTypes = new HashMap<String, java.lang.Class<? extends Enum>>();
				
				private Map<String, java.lang.Class<? extends Enum>> getEnumTypes()
				{
					if (enumTypes.isEmpty())
					{
						enumTypes.put("Building", eBuildingType.class);
						enumTypes.put("CelestialBody", eCelestialBodyType.class);
						enumTypes.put("SpecialUnit", eSpecialUnitType.class);
						enumTypes.put("Unit", eUnitType.class);
					}
					
					return enumTypes;
				}
				
				private java.lang.Class<? extends Enum> getEnumType(Class c)
				{
					for(Map.Entry<String, java.lang.Class<? extends Enum>> e : getEnumTypes().entrySet())
					{
						if (c.isInstanceOf(ormPackageName, e.getKey()) != null)
						{
							return e.getValue();
						}												
					}
					
					return null;
				}
				
				@Override
				public String getterDeclaration(Class c, Field f)
				{
					Field wf = wrapField(c, f);
					if (wf == null) return "";

					return super.getterDeclaration(c, wf);
				}
		
				/*
				@Override
				public String getterProxy(Class c, Field f, Class proxy)
				{
					Field wf = wrapField(c, f);
					if (wf == null) return "";
					// Type ?
					return super.getterProxy(c, wf, proxy);
				}
				*/
				
				@Override
				public String getter(Class c, Field f)
				{
					Field wf = wrapField(c, f);
					if (wf == null) return "";
					
					if (Location.class.equals(wf.getType()) || RealLocation.class.equals(wf.getType()) || wf.getLowerName().matches("type"))
					{
						return String.format("return %s;", wf.getLowerName());
					}					
					
					return super.getter(c, wf);
				}
				
				@Override
				public boolean skipGetter(Class c, Field field)
				{
					if (field.getLowerName().endsWith("_y") || field.getLowerName().endsWith("_z"))
					{
						return true;
					}
					
					Class vs = c.isInstanceOf(c.getPackage(), "VersionedUnit");
					if (vs == null) vs = c.isInstanceOf(c.getPackage(), "VersionedProductiveCelestialBody");
					
					if (vs != null)
					{
						return vs.hasField(field.getLowerName()) && !c.getSpecificFields().contains(field);
					}
					
					return false;
				}		
				
			}, ormPackageName, interfacePackageName, null, inheritances);
			
			// TODO: assurer l'import des types paramètres et retour.
			// TODO: méthodes cast pour les membres nested (ex: récupérer l'interface VersionedProductiveCelestialBody d'une VersionedPlanet.
			
			gen.stop();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return;
		}
	}
}
