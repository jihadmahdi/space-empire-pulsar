package org.axan.sep.server;

import java.io.File;
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
import org.axan.eplib.orm.DataBaseORMGenerator;
import org.axan.eplib.orm.ISQLDataBaseStatement;
import org.axan.eplib.orm.ISQLDataBaseStatementJob;
import org.axan.eplib.orm.SQLDataBaseException;
import org.axan.eplib.orm.sqlite.SQLiteDB;
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

class ORMGenerator
{
	private static interface RecursiveDoer<T>
	{
		T doIt(Class c);
	}
	
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
			
			/*
			for(Map.Entry<String, ArrayList<String>> e : inheritances.entrySet())
			{
				ArrayList<String> o = e.getValue();
				System.out.format("%s: %s\n", e.getKey(), o);
				
			}
			*/
			
			/*
			inheritances = new HashMap<String, String[]>();
			
			inheritances.put("VersionedUnit", new String[] {"Unit"});
			inheritances.put("PulsarMissile", new String[] {"Unit"});
			inheritances.put("Probe", new String[] {"Unit"});
			inheritances.put("AntiProbeMissile", new String[] {"Unit"});
			inheritances.put("CarbonCarrier", new String[] {"Unit"});
			inheritances.put("SpaceRoadDeliverer", new String[] {"Unit"});
			inheritances.put("Fleet", new String[] {"Unit"});
			inheritances.put("VersionedPulsarMissile", new String[] {"PulsarMissile", "VersionedUnit"});
			inheritances.put("VersionedProbe", new String[] {"Probe", "VersionedUnit"});
			inheritances.put("VersionedAntiProbeMissile", new String[] {"AntiProbeMissile", "VersionedUnit"});
			inheritances.put("VersionedCarbonCarrier", new String[] {"CarbonCarrier", "VersionedUnit"});
			inheritances.put("VersionedFleet", new String[] {"Fleet", "VersionedUnit"});
			
			cfg.set("inheritances", inheritances);
			
			cfg.save(configFile);
			
			*/
			
			gen.generate(new ClassGeneratorListener()
			{
				
				@Override
				public boolean skipClass(Class c)
				{
					return (c.getUpperName().matches("GameConfig"));
				}
				
				@Override
				public void genImports(Class c, PrintStream psui, PrintStream psuc)
				{
					Set<String> stringImports = new TreeSet<String>();
					
					Set<java.lang.Class<?>> importedClass = new HashSet<java.lang.Class<?>>();					
					Collections.addAll(importedClass, ISQLDataBaseStatement.class, org.axan.sep.common.db.IGameConfig.class);
										
					if (!skipStaticMethods(c))
					{
						Collections.addAll(importedClass, Set.class, HashSet.class, DataBaseORMGenerator.class, SEPCommonDB.class, SQLDataBaseException.class, SQLiteDB.class, Locale.class, ISQLDataBaseStatementJob.class);
						
						Class versionedClass = getVersionedClass(c);
						
						if (versionedClass != null && versionedClass != c)
						{
							stringImports.add(String.format("%s.I%s", IGameConfig.class.getPackage().getName(), versionedClass.getUpperName()));
						}
						
						java.lang.Class<? extends Enum> enumClass = getEnumType(c);
						if (enumClass != null) for(Enum name : enumClass.getEnumConstants())
						{
							stringImports.add(String.format("%s.I%s", IGameConfig.class.getPackage().getName(), name));
							
							if (versionedClass != null && versionedClass != c)
							{
								stringImports.add(String.format("%s.IVersioned%s", IGameConfig.class.getPackage().getName(), name));
							}
						}												
					}
					
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
				
				private String staticFactoryAdditionalParams(Class c)
				{
					if (c.isInstanceOf(c.getPackage(), "Unit") != null)
					{
						return "db.getConfig()";
					}
					
					return "";
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
				
				private boolean skipStaticMethods(Class c)
				{
					if (c.getUpperName().matches("CelestialBody")) return true;
					if (c.getUpperName().matches("ProductiveCelestialBody")) return false;
					if (c.getUpperName().matches("Vortex")) return false;
					
					for(String k : getEnumTypes().keySet())
					{
						if (c.isInstanceOf(ormPackageName, k) != null)
						{
							return !c.getUpperName().matches(k);
						}												
					}
					
					return c.getSupers().size() > 0;					
				}
				
				@Override
				public void genStaticMethods(Class c, PrintStream psuc)
				{
					boolean first = false;
					
					Class versionedClass = getVersionedClass(c);
					
					java.lang.Class<? extends Enum> enumType = getEnumType(c);
					
					if (skipStaticMethods(c)) return;
					
					///// select
					
					if (versionedClass != null && versionedClass.hasField("turn"))
					{
						psuc.println("\t/** Set maxVersion to null to select last version. */");
						psuc.format("\tpublic static <T extends I%s> Set<T> selectMaxVersion(%s db, Class<T> expectedType, Integer maxVersion, String from, String where, Object ... params) throws %s\n", c.getUpperName(), SEPCommonDB.class.getSimpleName(), SQLDataBaseException.class.getSimpleName());
						psuc.println("\t{\n\t\treturn select(db, expectedType, true, maxVersion, from, where, params);\n\t}\n");
						
						psuc.format("\tpublic static <T extends I%s> Set<T> selectVersion(%s db, Class<T> expectedType, int version, String from, String where, Object ... params) throws %s\n", c.getUpperName(), SEPCommonDB.class.getSimpleName(), SQLDataBaseException.class.getSimpleName());
						psuc.println("\t{\n\t\treturn select(db, expectedType, false, version, from, where, params);\n\t}\n");
						
						psuc.format("\tpublic static <T extends I%s> Set<T> selectUnversioned(%s db, Class<T> expectedType, String from, String where, Object ... params) throws %s\n", c.getUpperName(), SEPCommonDB.class.getSimpleName(), SQLDataBaseException.class.getSimpleName());
						psuc.println("\t{\n\t\treturn select(db, expectedType, false, null, from, where, params);\n\t}\n");
						
						psuc.format("\tprivate static <T extends I%s> Set<T> select(%s db, Class<T> expectedType, boolean maxVersion, Integer version, String from, String where, Object ... params) throws %s\n", c.getUpperName(), SEPCommonDB.class.getSimpleName(), SQLDataBaseException.class.getSimpleName());
					}
					else
					{
						psuc.format("\tpublic static <T extends I%s> T selectOne(%s db, Class<T> expectedType, String from, String where, Object ... params) throws %s\n", c.getUpperName(), SEPCommonDB.class.getSimpleName(), SQLDataBaseException.class.getSimpleName());
						psuc.println("\t{");
						psuc.println("\t\tSet<T> results = select(db, expectedType, from, (where==null?\"(1) \":\"(\"+where+\") \")+\"LIMIT 1\", params);");
						psuc.println("\t\tif (results.isEmpty()) return null;");
						psuc.println("\t\treturn results.iterator().next();");
						psuc.println("\t}");
						psuc.println();						
						psuc.format("\tpublic static <T extends I%s> Set<T> select(%s db, Class<T> expectedType, String from, String where, Object ... params) throws %s\n", c.getUpperName(), SEPCommonDB.class.getSimpleName(), SQLDataBaseException.class.getSimpleName());
					}
					psuc.println("\t{");
					psuc.format("\t\t%s stmnt = null;\n", ISQLDataBaseStatement.class.getSimpleName());
					psuc.println("\t\ttry");
					psuc.println("\t\t{");
					psuc.println("\t\t\tSet<T> results = new HashSet<T>();");	
					psuc.print("\t\t\tstmnt = db.getDB().prepare(");
					if (versionedClass != null && versionedClass.hasField("turn"))
					{
						psuc.print("selectQuery(expectedType, maxVersion, version, from, where, params)");
					}
					else
					{
						psuc.print("selectQuery(expectedType, from, where, params)");
					}
					psuc.format("+\";\", new %s<%s>()\n", ISQLDataBaseStatementJob.class.getSimpleName(), ISQLDataBaseStatement.class.getSimpleName());
					psuc.println("\t\t\t{");
					psuc.println("\t\t\t\t@Override");
					psuc.format("\t\t\t\tpublic %s job(%s stmnt) throws %s\n", ISQLDataBaseStatement.class.getSimpleName(), ISQLDataBaseStatement.class.getSimpleName(), SQLDataBaseException.class.getSimpleName());
					psuc.println("\t\t\t\t{");
					psuc.println("\t\t\t\t\treturn stmnt;");
					psuc.println("\t\t\t\t}");
					psuc.println("\t\t\t}, params);");
							
					String addParams = staticFactoryAdditionalParams(c);
					addParams = addParams == null || addParams.isEmpty() ? "" : ", "+addParams;
					
					psuc.println("\t\t\twhile(stmnt.step())");
					psuc.println("\t\t\t{");
					if (versionedClass != null && versionedClass.hasField("type"))
					{
						psuc.format("\t\t\t\t%s type = %s.valueOf(stmnt.columnString(0));\n", enumType.getSimpleName(), enumType.getSimpleName());
						if (versionedClass != null)
						{
							psuc.println("\t\t\t\tString v = stmnt.columnString(1);");
							psuc.format("\t\t\t\tif (v == null) throw new Error(\"%s with no %s !\");\n", c.getUpperName(), versionedClass.getUpperName());
							psuc.println("\t\t\t\tboolean isVersioned = (!v.isEmpty());");
						}						
						psuc.format("\t\t\t\tClass<? extends I%s> clazz = (Class<? extends I%s>)  Class.forName(String.format(\"%%s.%%s%%s\", %s.class.getPackage().getName(), ", c.getUpperName(), c.getUpperName(), c.getUpperName());
						psuc.format("%s, type.toString()));\n", getUnprefixedVersionedTypes().containsKey(c.getUpperName()) ? "\"\"" : "isVersioned ? \"Versioned\" : \"\"");						
						
						psuc.format("\t\t\t\tI%s o = %s.mapTo(clazz, stmnt%s);\n", c.getUpperName(), DataBaseORMGenerator.class.getSimpleName(), addParams);
						psuc.println("\t\t\t\tif (expectedType.isInstance(o))");
						psuc.println("\t\t\t\t{");
						psuc.println("\t\t\t\t\tresults.add(expectedType.cast(o));");
						psuc.println("\t\t\t\t}");
					}
					else
					{
						psuc.format("\t\t\t\tresults.add(%s.mapTo(expectedType.isInterface() ? (Class<T>) %s.class : expectedType, stmnt%s));\n", DataBaseORMGenerator.class.getSimpleName(), c.getUpperName(), addParams);
					}
					psuc.println("\t\t\t}");
					psuc.println("\t\t\treturn results;");
					psuc.println("\t\t}");
					psuc.println("\t\tcatch(Exception e)");
					psuc.println("\t\t{");
					psuc.format("\t\t\tthrow new %s(e);\n", SQLDataBaseException.class.getSimpleName());
					psuc.println("\t\t}");
					psuc.println("\t\tfinally");
					psuc.println("\t\t{");
					psuc.println("\t\t\tif (stmnt != null) stmnt.dispose();");
					psuc.println("\t\t}");
					psuc.println("\t}\n");
					
					/// Exist
					
					if (versionedClass != null && versionedClass.hasField("turn"))
					{
						psuc.println("\t/** Set maxVersion to null to select last version. */");
						psuc.format("\tpublic static <T extends I%s> boolean existMaxVersion(%s db, Class<T> expectedType, Integer maxVersion, String from, String where, Object ... params) throws %s\n", c.getUpperName(), SEPCommonDB.class.getSimpleName(), SQLDataBaseException.class.getSimpleName());
						psuc.println("\t{\n\t\treturn exist(db, expectedType, true, maxVersion, from, where, params);\n\t}\n");
						
						psuc.format("\tpublic static <T extends I%s> boolean existVersion(%s db,Class<T> expectedType, int version, String from, String where, Object ... params) throws %s\n", c.getUpperName(), SEPCommonDB.class.getSimpleName(), SQLDataBaseException.class.getSimpleName());
						psuc.println("\t{\n\t\treturn exist(db, expectedType, false, version, from, where, params);\n\t}\n");
						
						psuc.format("\tpublic static <T extends I%s> boolean existUnversioned(%s db, Class<T> expectedType, String from, String where, Object ... params) throws %s\n", c.getUpperName(), SEPCommonDB.class.getSimpleName(), SQLDataBaseException.class.getSimpleName());
						psuc.println("\t{\n\t\treturn exist(db, expectedType, false, null, from, where, params);\n\t}\n");
						
						psuc.format("\tprivate static <T extends I%s> boolean exist(%s db, Class<T> expectedType, boolean maxVersion, Integer version, String from, String where, Object ... params) throws %s\n", c.getUpperName(), SEPCommonDB.class.getSimpleName(), SQLDataBaseException.class.getSimpleName());
					}
					else
					{
						psuc.format("\tpublic static <T extends I%s> boolean exist(%s db, Class<T> expectedType, String from, String where, Object ... params) throws %s\n", c.getUpperName(), SEPCommonDB.class.getSimpleName(), SQLDataBaseException.class.getSimpleName());
					}
					psuc.println("\t{");
					//psuc.format("\t\t%s stmnt = null;\n", ISQLDataBaseStatement.class.getSimpleName());
					psuc.println("\t\ttry");
					psuc.println("\t\t{");
					/*
					boolean exist = db.prepare("SELECT key FROM GameConfig WHERE key = '%s'", new ISQLDataBaseStatementJob<Boolean>()
					{
						@Override
						public Boolean job(ISQLDataBaseStatement stmnt) throws SQLDataBaseException
						{
							return stmnt.step() && stmnt.columnString(0) != null;
						}
					}, key);
					 */
					psuc.print("\t\t\treturn db.getDB().prepare(");
					//psuc.print("\t\t\tstmnt = db.getDB().prepare(\"SELECT EXISTS ( \"+");
					if (versionedClass != null && versionedClass.hasField("turn"))
					{
						psuc.print("selectQuery(expectedType, maxVersion, version, from, where, params)");
					}
					else
					{
						psuc.print("selectQuery(expectedType, from, where, params)");
					}
					psuc.format("+\" ;\", new %s<%s>()\n", ISQLDataBaseStatementJob.class.getSimpleName(), Boolean.class.getSimpleName());
					psuc.println("\t\t\t{");
					psuc.println("\t\t\t\t@Override");
					psuc.format("\t\t\t\tpublic %s job(%s stmnt) throws %s\n", Boolean.class.getSimpleName(), ISQLDataBaseStatement.class.getSimpleName(), SQLDataBaseException.class.getSimpleName());
					psuc.println("\t\t\t\t{");
					psuc.println("\t\t\t\t\ttry");
					psuc.println("\t\t\t\t\t{");
					psuc.println("\t\t\t\t\t\treturn stmnt.step() && stmnt.columnValue(0) != null;");
					psuc.println("\t\t\t\t\t}");
					psuc.println("\t\t\t\t\tfinally");
					psuc.println("\t\t\t\t\t{");
					psuc.println("\t\t\t\t\t\tif (stmnt != null) stmnt.dispose();");
					psuc.println("\t\t\t\t\t}");
					//psuc.println("\t\t\t\t\treturn stmnt;");
					psuc.println("\t\t\t\t}");
					psuc.println("\t\t\t}, params);");
					
					//psuc.println("\t\t\treturn stmnt.step() && stmnt.columnInt(0) != 0;");
					psuc.println("\t\t}");
					psuc.println("\t\tcatch(Exception e)");
					psuc.println("\t\t{");
					psuc.format("\t\t\tthrow new %s(e);\n", SQLDataBaseException.class.getSimpleName());
					psuc.println("\t\t}");
					psuc.println("\t}\n");
					
					psuc.println();
					/// selectQuery
					
					if (versionedClass != null && versionedClass.hasField("turn"))
					{
						psuc.format("\tprivate static <T extends I%s> String selectQuery(Class<T> expectedType, boolean maxVersion, Integer version, String from, String where, Object ... params)\n", c.getUpperName());
					}
					else
					{
						psuc.format("\tprivate static <T extends I%s> String selectQuery(Class<T> expectedType, String from, String where, Object ... params)\n", c.getUpperName());
					}
					psuc.println("\t{");
					psuc.println("\t\twhere = (where == null) ? null : (params == null) ? where : String.format(Locale.UK, where, params);");
					psuc.println("\t\tif (where != null && !where.isEmpty() && where.charAt(0) != '(') where = \"(\"+where+\")\";");
					//psuc.println("\t\tif (where != null) where = String.format(\"(%s)\",where);");
					
					if (c.hasField("type"))
					{
						psuc.println("\t\tString typeFilter = null;");
						psuc.println("\t\tif (expectedType != null)");
						psuc.println("\t\t{");
						psuc.println("\t\t\tString type = expectedType.isInterface() ? expectedType.getSimpleName().substring(1) : expectedType.getSimpleName();");
						psuc.println("\t\t\ttypeFilter = String.format(\"%s.type IS NOT NULL\", type);");
						psuc.println("\t\t}");
						psuc.println("\t\tif (typeFilter != null && !typeFilter.isEmpty()) where = (where == null) ? typeFilter : String.format(\"%s AND %s\", typeFilter, where);");
					}
					
					if (versionedClass != null && versionedClass.hasField("turn"))
					{
						psuc.println("\t\tString versionFilter;");
						psuc.println("\t\tif (maxVersion)");
						psuc.println("\t\t{");
													
						psuc.format("\t\t\tversionFilter = String.format(\"(%s.turn = ( SELECT MAX(LV%s.turn) FROM %s LV%s WHERE ", versionedClass.getUpperName(), versionedClass.getUpperName(), versionedClass.getUpperName(), versionedClass.getUpperName());
						boolean comma = false;
						for(Field f : c.getCommonFields(versionedClass))
						{
							if (comma) psuc.print(" AND ");
							psuc.format("LV%s.%s = %s.%s", versionedClass.getUpperName(), f.getLowerName(), c.getUpperName(), f.getLowerName());
							comma = true;
						}
						psuc.format("%%s ))\", (version != null && version >= 0) ? \" AND LV%s.turn <= \"+version : \"\");\n", versionedClass.getUpperName());
						psuc.println("\t\t}");
						psuc.println("\t\telse");
						psuc.println("\t\t{");
						psuc.format("\t\t\tversionFilter = (version == null) ? \"\" : String.format(\"(%s.turn = %%d)\", version);\n", versionedClass.getUpperName());
						psuc.println("\t\t}");
						
						psuc.println("\t\tif (versionFilter != null && !versionFilter.isEmpty()) where = (where == null) ? versionFilter : String.format(\"%s AND %s\", where, versionFilter);");
					}
					
					psuc.print("\t\treturn String.format(\"SELECT ");
					if (versionedClass != null && versionedClass.hasField("type"))
					{
						psuc.format("%s.type, %s.type, ", c.getUpperName(), versionedClass.getUpperName());
					}
					//psuc.format("%s.*", c.getUpperName());
					
					final Set<Class> seen = new HashSet<Class>();
					final Class[] jointure = {c};
					final Class from = c;
					final StringBuffer select = new StringBuffer();
					final StringBuffer leftJoins = new StringBuffer();
					
					RecursiveDoer<Void> doJointures = new RecursiveDoer<Void>()
					{
						@Override
						public Void doIt(Class c)
						{
							if (seen.contains(c)) return null;
							seen.add(c);
							
							for(Class sup : c.getSupers())
							{
								doIt(sup);
							}
							
							Class versionedClass = getVersionedClass(c);
							if (versionedClass != null && versionedClass != c) doIt(versionedClass);
							
							if (select.length() > 0) select.append(", ");
							select.append(String.format("%s.*", c.getUpperName()));
							
							if (from != c && jointure[0] != c)
							{
								leftJoins.append(String.format(" LEFT JOIN %s USING (", c.getUpperName()));
								boolean comma = false;
								for(Field f : jointure[0].getCommonFields(c))
								{
									if (comma) leftJoins.append(", ");
									comma = true;
									leftJoins.append(String.format("%s", f.getLowerName()));							
								}
								leftJoins.append(")");
								
								if (c == versionedClass) jointure[0] = versionedClass;							
							}
							
							for(Class sub : c.getSubers())
							{
								doIt(sub);
							}
								
							return null;
						}
					};
					
					doJointures.doIt(c);
					psuc.append(select);
					
					psuc.format(" FROM %s%%s%s", c.getUpperName(), leftJoins);
					psuc.println("%s\", (from != null && !from.isEmpty()) ? \", \"+from : \"\", (where != null && !where.isEmpty()) ? \" WHERE \"+where : \"\");");
					psuc.println("\t}");
					
					//// InsertOrUpdate
					psuc.println();
					
					psuc.format("\tpublic static <T extends I%s> void insertOrUpdate(%s db, T %s) throws %s\n", c.getUpperName(), SEPCommonDB.class.getSimpleName(), c.getLowerName(), SQLDataBaseException.class.getSimpleName());
					psuc.println("\t{");
					psuc.println("\t\ttry");
					psuc.println("\t\t{");
					
					psuc.format("\t\t\tboolean exist = %s(db, %s.getClass(), null, \"", (versionedClass != null && versionedClass.hasField("turn")) ? "existUnversioned" : "exist", c.getLowerName());
					
					boolean comma = false;
					StringBuffer values = new StringBuffer();
					for(Field f : c.getFields())
					{
						if (!f.isPrimaryKey()) continue;
						
						if (comma)
						{
							psuc.print(" AND");
							values.append(", ");
						}
						
						psuc.print(getFieldQuery(c.getUpperName(), f, " AND"));
						values.append(getFieldValue(c.getLowerName(), f));
						
						comma = true;
					}
					psuc.format("\", %s);\n", values);				
					
					if (versionedClass != null && versionedClass != c)
					{
						psuc.format("\t\t\tI%s v%s = (I%s.class.isInstance(%s) ? I%s.class.cast(%s) : null);\n", versionedClass.getUpperName(), c.getLowerName(), versionedClass.getUpperName(), c.getLowerName(), versionedClass.getUpperName(), c.getLowerName());
						
						psuc.format("\t\t\tboolean vexist = existVersion(db, v%s.getClass(), v%s.getTurn(), null, \"", c.getLowerName(), c.getLowerName());
						
						comma = false;
						values.setLength(0);
						for(Field f : versionedClass.getFields())
						{
							if (!f.isPrimaryKey()) continue;
							
							if (comma)
							{
								psuc.print(" AND");
								values.append(", ");
							}
							
							psuc.print(getFieldQuery(versionedClass.getUpperName(), f, " AND"));
							values.append(getFieldValue("v"+c.getLowerName(), f));
							
							comma = true;
						}
						psuc.format("\", %s);\n", values);
						
						psuc.format("\t\t\tif (vexist && !exist) throw new Error(\"Versioned %s cannot exist without unversioned entry.\");\n", c.getUpperName());
					}
										
					values.setLength(0);
					psuc.println("\t\t\tif (exist)");
					psuc.println("\t\t\t{");
					
					seen.removeAll(seen);
					final StringBuffer inserts = new StringBuffer();
					final StringBuffer updates = new StringBuffer();
					final String varName = c.getLowerName();
					RecursiveDoer<Void> doInsertUpdateSupers = new RecursiveDoer<Void>()
					{
						@Override
						public Void doIt(Class c)
						{
							if (seen.contains(c)) return null;
							
							for(Class sup : c.getSupers())
							{
								doIt(sup);
							}
							
							inserts.append(String.format("\t\t\t\tif (!exist) db.getDB().exec(%s);\n", generateInsertQuery(c, varName)));
							String update = generateUpdateQuery(c, varName);
							if (update != null) updates.append(String.format("\t\t\t\tdb.getDB().exec(%s);\n", update));

							seen.add(c);
							return null;
						}
					};
					
					doInsertUpdateSupers.doIt(c);
					
					psuc.append(updates);
					values.append(inserts);					
					if (versionedClass != null && versionedClass != c)
					{
						final StringBuffer vinserts = new StringBuffer();
						final StringBuffer vupdates = new StringBuffer();
						RecursiveDoer<Void> doVInsertUpdateSupers = new RecursiveDoer<Void>()
						{
							@Override
							public Void doIt(Class c)
							{
								if (seen.contains(c)) return null;
								
								for(Class sup : c.getSupers())
								{
									doIt(sup);
								}
								
								vinserts.append(String.format("\t\t\t\tif (v%s != null)\n\t\t\t\t{\n\t\t\t\t\tdb.getDB().exec(%s);\n\t\t\t\t}\n", varName, generateInsertQuery(c, "v"+varName)));
								String update = generateUpdateQuery(c, "v"+varName);
								if (update != null) vupdates.append(String.format("\t\t\t\tif (vexist && v%s != null)\n\t\t\t\t{\n\t\t\t\t\tdb.getDB().exec(%s);\n\t\t\t\t}\n", varName, update));
								
								seen.add(c);
								return null;
							}
						};
						
						doVInsertUpdateSupers.doIt(versionedClass);
						
						psuc.append(vupdates);
						values.append(vinserts);
					}
					
					if (c.hasField("type"))
					{
						psuc.format("\t\t\t\tswitch(%s.getType())\n", c.getLowerName());
						values.append(String.format("\t\t\t\tswitch(%s.getType())\n", c.getLowerName()));
						psuc.println("\t\t\t\t{");
						values.append("\t\t\t\t{\n");
						
						for(Enum<?> e : enumType.getEnumConstants())
						{							
							for(Class sub : c.getSubers())
							{
								if (sub.getUpperName().matches(e.name()))
								{
									psuc.format("\t\t\t\t\tcase %s:\n", e.name());
									values.append(String.format("\t\t\t\t\tcase %s:\n", e.name()));
									psuc.println("\t\t\t\t\t{");
									values.append("\t\t\t\t\t{\n");
									
									psuc.format("\t\t\t\t\t\tI%s %s = I%s.class.cast(%s);\n", sub.getUpperName(), sub.getLowerName(), sub.getUpperName(), c.getLowerName());
									values.append(String.format("\t\t\t\t\t\tI%s %s = I%s.class.cast(%s);\n", sub.getUpperName(), sub.getLowerName(), sub.getUpperName(), c.getLowerName()));
									String update = generateUpdateQuery(sub, sub.getLowerName());
									if (update != null) psuc.format("\t\t\t\t\t\tdb.getDB().exec(%s);\n", update);
									values.append(String.format("\t\t\t\t\t\tif (!exist) db.getDB().exec(%s);\n", generateInsertQuery(sub, sub.getLowerName())));
									if (versionedClass != c)
									{
										Class vsub = getVersionedClass(sub);
										if (vsub != null && vsub != sub)
										{
											psuc.format("\t\t\t\t\t\tif (vexist && v%s != null)\n", c.getLowerName());
											values.append(String.format("\t\t\t\t\t\tif (v%s != null)\n", c.getLowerName()));
											psuc.println("\t\t\t\t\t\t{");
											values.append("\t\t\t\t\t\t{\n");
											psuc.format("\t\t\t\t\t\t\tI%s %s = I%s.class.cast(%s);\n", vsub.getUpperName(), vsub.getLowerName(), vsub.getUpperName(), c.getLowerName());
											values.append(String.format("\t\t\t\t\t\t\tI%s %s = I%s.class.cast(%s);\n", vsub.getUpperName(), vsub.getLowerName(), vsub.getUpperName(), c.getLowerName()));
											update = generateUpdateQuery(vsub, vsub.getLowerName());
											if (update != null) psuc.format("\t\t\t\t\t\t\tdb.getDB().exec(%s);\n", update);
											values.append(String.format("\t\t\t\t\t\t\tdb.getDB().exec(%s);\n", generateInsertQuery(vsub, vsub.getLowerName())));
											psuc.println("\t\t\t\t\t\t}");
											values.append("\t\t\t\t\t\t}\n");
										}																					
									}
									
									psuc.println("\t\t\t\t\t\tbreak;");
									values.append("\t\t\t\t\t\tbreak;\n");
									psuc.println("\t\t\t\t\t}");
									values.append("\t\t\t\t\t}\n");
								}
							}
						}
						psuc.println("\t\t\t\t}");
						values.append("\t\t\t\t}\n");
					}
					
					/*
					 * switch (unit.getType())
						{
							case AntiProbeMissile:
							{
					 */
					
					psuc.println("\t\t\t}");
					psuc.println((versionedClass == null || versionedClass == c) ? "\t\t\telse" : "\t\t\tif (!exist || !vexist)");
					psuc.println("\t\t\t{");
					psuc.print(values);					
					psuc.println("\t\t\t}");
					psuc.println("\t\t}");
					psuc.println("\t\tcatch(Exception e)");
					psuc.println("\t\t{");
					psuc.format("\t\t\tthrow new %s(e);\n", SQLDataBaseException.class.getSimpleName());
					psuc.println("\t\t}");
					psuc.println("\t}");
				}
				
				private String generateUpdateQuery(Class c, String var)
				{
					StringBuffer set = new StringBuffer();
					StringBuffer where = new StringBuffer();
					StringBuffer setValues = new StringBuffer();
					StringBuffer whereValues = new StringBuffer();
					
					boolean pkComma = false;
					boolean npkComma = false;
					
					for(Field f : c.getFields())
					{
						if (!f.isPrimaryKey())
						{
							if (npkComma)
							{
								set.append(", ");
								setValues.append(", ");
							}
							
							set.append(getFieldQuery(null, f, ","));
							setValues.append(getFieldValue(var, f));
							
							npkComma = true;
						}
						else
						{
							if (pkComma)
							{
								where.append(" AND");
								whereValues.append(", ");
							}
							
							where.append(getFieldQuery(c.getUpperName(), f, " AND"));
							whereValues.append(getFieldValue(var, f));
							
							pkComma = true;
						}						
					}
					
					if (set.length() == 0) return null; // "\";\"";
					return String.format("String.format(\"UPDATE %s SET%s WHERE %s ;\", %s, %s).replaceAll(\"'null'\", \"NULL\")", c.getUpperName(), set, where, setValues, whereValues);
				}
				
				private String generateInsertQuery(Class c, String var)
				{
					StringBuffer result = new StringBuffer();
					StringBuffer flags = new StringBuffer();
					StringBuffer values = new StringBuffer();
					
					result.append(String.format("String.format(\"INSERT INTO %s (", c.getUpperName()));
					
					boolean comma = false;
					for(Field f : c.getFields())
					{
						if (comma)
						{
							result.append(", ");
							flags.append(", ");
							values.append(", ");
						}
						
						result.append(f.getLowerName());
						flags.append(String.format("%s", getFieldQueryFlag(f)));
						values.append(getFieldValue(var, f));
						
						comma = true;
					}
					
					result.append(String.format(") VALUES (%s);\", %s).replaceAll(\"'null'\", \"NULL\")", flags, values));
					return result.toString();
				}
				
				private String getFieldQueryFlag(Field f)
				{
					if (f.getType().equals(Location.class))
					{
						return "%d";
					}
					else if (f.getType().equals(RealLocation.class))
					{
						return "%f";
					}
					else
					{
						return "%s";
					}
				}
				
				private String getFieldQuery(String table, Field f, String comma)
				{
					String flag = getFieldQueryFlag(f);					
					return String.format(" %s%s = %s", table == null ? "" : table+".", f.getLowerName(), flag);
				}
				
				private String getFieldValue(String var, Field f)
				{
					String flag = getFieldQueryFlag(f);
					
					if (f.getLowerName().endsWith("_x") || f.getLowerName().endsWith("_y") || f.getLowerName().endsWith("_z"))
					{
						String[] ff = Basic.split(f.getUpperName(), "_");
						return String.format("%s.get%s() == null ? \"NULL\" : \"'\"+%s.get%s().%s+\"'\"", var, ff[0], var, ff[0], ff[1]);
					}
					else
					{
						return String.format("%s%s.get%s()%s", flag.matches("%s") ? "\"'\"+" : "", var, f.getUpperName(), flag.matches("%s") ? "+\"'\"" : "");
					}
				}
				
				
			}, ormPackageName, interfacePackageName, Exception.class, inheritances);
			
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
