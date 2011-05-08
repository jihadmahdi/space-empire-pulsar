package org.axan.sep.server.model;

import java.io.File;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.axan.eplib.orm.Class;
import org.axan.eplib.orm.ClassGeneratorListener;
import org.axan.eplib.orm.Class.Field;
import org.axan.eplib.orm.sqlite.SQLiteDB;
import org.axan.eplib.orm.sqlite.SQLiteORMGenerator;
import org.axan.eplib.orm.sqlite.SQLiteDB.SQLiteDBException;
import org.axan.eplib.utils.Basic;
import org.axan.eplib.utils.Reflect;
import org.axan.eplib.yaml.YamlConfigFile;
import org.axan.sep.common.IGame;
import org.axan.sep.common.IGameConfig;
import org.axan.sep.common.Protocol.eBuildingType;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.Protocol.eSpecialUnitType;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.SEPUtils.RealLocation;
import org.axan.sep.common.db.sqlite.SEPCommonSQLiteDB;
import org.axan.sep.common.db.sqlite.orm.IUnit;
import org.axan.sep.common.db.sqlite.orm.IVersionedUnit;

import com.almworks.sqlite4java.SQLite;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;

public class ORMGenerator
{
	
	public static void main(String[] args)
	{
		SQLiteORMGenerator gen;
		try
		{
			SQLiteDB.checkSQLiteLib("target/izpack/lib/");
			
			File ormTemp = File.createTempFile("SEP_ORM", ".db");
			ormTemp.deleteOnExit();
			
			String sqlitePackageName = SEPCommonSQLiteDB.class.getPackage().getName();
			final String ormPackageName = sqlitePackageName+".orm";
			File outputFile = new File("/media/data/code/Java_Workspace/Space-Empire-Pulsar/src/main/java/"+ormPackageName.replace('.', '/'));
			URL dbFileURL = Reflect.getResource(sqlitePackageName, "SEPSQLiteDB.sql");
			File configFile = new File(Reflect.getResource(sqlitePackageName, "SEPSQLiteDB.ORM.yaml").getFile());
			
			YamlConfigFile cfg = YamlConfigFile.open(configFile);
			
			gen = new SQLiteORMGenerator(ormTemp, outputFile);
			
			gen.importFile(new File(dbFileURL.getFile()));
			
			Map<String, Collection<String>> inheritances;
			
			inheritances = (Map<String, Collection<String>>) cfg.get("inheritances", Map.class);
			
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
					Set<java.lang.Class<?>> importedClass = new TreeSet<java.lang.Class<?>>(new Comparator<java.lang.Class<?>>()
					{
						@Override
						public int compare(java.lang.Class<?> o1, java.lang.Class<?> o2)
						{
							return o1.getCanonicalName().compareTo(o2.getCanonicalName());
						}
					});					
					Collections.addAll(importedClass, SQLiteStatement.class, org.axan.sep.common.IGameConfig.class);
					
					if (!skipStaticMethods(c))
					{
						Collections.addAll(importedClass, Set.class, HashSet.class, SQLiteORMGenerator.class, SQLiteConnection.class, SQLiteDBException.class);
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
						psuc.format("import %s;\n", clazz.getCanonicalName());
						psui.format("import %s;\n", clazz.getCanonicalName());
					}
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
							sb.append(String.format("private %s %s;", nf.getType().getSimpleName(), nf.getLowerName()));
						}
						
						if (f.getLowerName().matches("type"))
						{
							Field nf = wrapField(c, f);
							sb.append(String.format("private %s %s;", nf.getType().getSimpleName(), nf.getLowerName()));
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
				
				@Override
				public String fullConstructorField(Class c, Field f)
				{
					Field wf = wrapField(c, f);
					if (wf == null || f.equals(wf)) return "";
					return String.format("this.%s = %s;", wf.getLowerName(), wf.getLowerName());
				}
				
				@Override
				public String fullConstructorProxyParameter(Class c, Field f)
				{
					Field wf = wrapField(c, f);
					if (wf == null) return "";
					
					return super.fullConstructorProxyParameter(c, wf);
				}
				
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
				public String constructorDeclaration(Class c)
				{
					return String.format("public %s(%s stmnt, %s config) throws %s", c.getUpperName(), SQLiteStatement.class.getSimpleName(), org.axan.sep.common.IGameConfig.class.getSimpleName(), Exception.class.getSimpleName());
				}
				
				@Override
				public String constructorCreateProxy(Class c, Class sup)
				{
					return String.format("this.%sProxy = new %s(stmnt, config);", sup.getLowerName(), sup.getUpperName());
				}	
				
				@Override
				public String constructorSuperCall(Class c)
				{
					return "super(stmnt, config);";
				}
				
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
						unprefixedVersionedTypes.put("Building", true);
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
						if (c.isInstanceOf(ormPackageName, e.getKey()))
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
		
				@Override
				public String getterProxy(Class c, Field f, Class proxy)
				{
					Field wf = wrapField(c, f);
					if (wf == null) return "";
					// Type ?
					return super.getterProxy(c, wf, proxy);
				}
				
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
					
					return false;
				}
				
				private boolean skipStaticMethods(Class c)
				{
					for(String k : getEnumTypes().keySet())
					{
						if (c.isInstanceOf(ormPackageName, k))
						{
							return !c.getUpperName().matches(k);
						}												
					}
					
					return c.getSupers().size() > 0;					
				}
				
				@Override
				public void genStaticMethods(Class c, PrintStream psuc)
				{
					genStaticMethods(c, psuc, new HashSet<Class>());
				}
				
				private void genStaticMethods(Class c, PrintStream psuc, Set<Class> seen)
				{
					boolean first = false;
					
					Class versionedClass = getVersionedClass(c);
					
					java.lang.Class<? extends Enum> enumType = getEnumType(c);
					
					if (seen.isEmpty())
					{
						if (skipStaticMethods(c)) return;
						
						psuc.format("\tpublic static <T extends I%s> Set<T> select(SQLiteConnection conn, IGameConfig config, Class<T> expectedType, %sString where, Object ... params) throws SQLiteDBException\n", c.getUpperName(), (versionedClass != null && versionedClass.hasField("turn")) ? "boolean lastVersion, " : "" );
						psuc.println("\t{");
						psuc.println("\t\ttry");
						psuc.println("\t\t{");
						psuc.println("\t\t\tSet<T> results = new HashSet<T>();\n");
						psuc.println("\t\t\tif (where != null && params != null) where = String.format(where, params);");
						if (versionedClass != null && versionedClass.hasField("turn"))
						{
							psuc.println("\t\t\tif (lastVersion)");
							psuc.println("\t\t\t{");
							psuc.format("\t\t\t\twhere = String.format(\"%%s(%s.turn = ( SELECT MAX(LV%s.turn) FROM %s LV%s WHERE ", versionedClass.getUpperName(), versionedClass.getUpperName(), versionedClass.getUpperName(), versionedClass.getUpperName());
							
							boolean comma = false;
							for(Field f : c.getCommonFields(versionedClass))
							{
								if (comma) psuc.print(" AND ");
								psuc.format("LV%s.%s = %s.%s", versionedClass.getUpperName(), f.getLowerName(), c.getUpperName(), f.getLowerName());
								comma = true;
							}
							
							psuc.println(" ))\", (where != null && !where.isEmpty()) ? \"(\"+where+\") AND \" : \"\");");						
							psuc.println("\t\t\t}");
						}
						psuc.print("\t\t\tSQLiteStatement stmnt = conn.prepare(String.format(\"");												
						psuc.print("SELECT ");
						if (versionedClass != null && versionedClass.hasField("type"))
						{
							psuc.format("%s.type, %s.type, ", c.getUpperName(), versionedClass.getUpperName());
						}
						psuc.format("* FROM %s", c.getUpperName());			
						first = true;
					}
					
					Set<Class> subers = c.getSubers();
					Vector<Class> jointures = new Vector<Class>(subers.size()+1);
					if (versionedClass != null && versionedClass != c) jointures.add(versionedClass);
					jointures.addAll(subers);
					
					Class jointure = c;
					
					for(Class sub : jointures)
					{
						if (seen.contains(sub)) continue;
						seen.add(sub);
						
						psuc.format(" LEFT JOIN %s USING (", sub.getUpperName());
						boolean comma = false;
						for(Field f : jointure.getCommonFields(sub))
						{
							if (comma) psuc.print(", ");
							comma = true;
							psuc.format("%s", f.getLowerName());							
						}
						psuc.print(")");
						
						if (sub == versionedClass) jointure = versionedClass;
						genStaticMethods(sub, psuc, seen);			
					}
					
					if (first)
					{
						psuc.println("%s ;\", (where != null && !where.isEmpty()) ? \" WHERE \"+where : \"\"));");
						psuc.println("\t\t\twhile(stmnt.step())");
						psuc.println("\t\t\t{");
						if (versionedClass != null && versionedClass.hasField("type"))
						{
							psuc.format("\t\t\t\t%s type = %s.valueOf(stmnt.columnString(0));\n", enumType.getSimpleName(), enumType.getSimpleName());
							if (versionedClass != null)
							{
								psuc.println("\t\t\t\tboolean isVersioned = (!stmnt.columnString(1).isEmpty());");
							}						
							psuc.format("\t\t\t\tClass<? extends I%s> clazz = (Class<? extends I%s>)  Class.forName(String.format(\"%%s.%%s%%s\", %s.class.getPackage().getName(), ", c.getUpperName(), c.getUpperName(), c.getUpperName());
							psuc.format("%s, type.toString()));\n", getUnprefixedVersionedTypes().containsKey(c.getUpperName()) ? "\"\"" : "isVersioned ? \"Versioned\" : \"\"");						
							
							psuc.format("\t\t\t\tI%s o = SQLiteORMGenerator.mapTo(clazz, stmnt, config);\n", c.getUpperName());
							psuc.println("\t\t\t\tif (expectedType.isInstance(o))");
							psuc.println("\t\t\t\t{");
							psuc.println("\t\t\t\t\tresults.add(expectedType.cast(o));");
							psuc.println("\t\t\t\t}");
						}
						else
						{
							psuc.format("\t\t\t\tresults.add(SQLiteORMGenerator.mapTo(expectedType, stmnt, config));\n", c.getUpperName());
						}
						psuc.println("\t\t\t}");
						psuc.println("\t\t\treturn results;");
						psuc.println("\t\t}");
						psuc.println("\t\tcatch(Exception e)");
						psuc.println("\t\t{");
						psuc.format("\t\t\tthrow new %s(e);\n", SQLiteDBException.class.getSimpleName());
						psuc.println("\t\t}");
						psuc.println("\t}\n");
					}
					
					//// InsertOrUpdate
					if (first)
					{
						psuc.println();
						
						psuc.format("\tpublic static <T extends I%s> void insertOrUpdate(SQLiteConnection conn, T %s) throws SQLiteDBException\n", c.getUpperName(), c.getLowerName());
						psuc.println("\t{");
						psuc.println("\t\ttry");
						psuc.println("\t\t{");
						if (versionedClass != null)
						{
							psuc.format("\t\t\tI%s v%s = (I%s.class.isInstance(%s) ? I%s.class.cast(%s) : null);\n", versionedClass.getUpperName(), c.getLowerName(), versionedClass.getUpperName(), c.getLowerName(), versionedClass.getUpperName(), c.getLowerName());
						}
						psuc.format("\t\t\tSQLiteStatement stmnt = conn.prepare(String.format(\"SELECT EXISTS ( SELECT %s FROM %s WHERE", c.getAllFields().iterator().next().getLowerName(), c.getUpperName());

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
							
							psuc.print(getFieldQuery(f, " AND"));
							values.append(getFieldValue(c.getLowerName(), f));
							
							comma = true;
						}
						psuc.format(") AS exist ;\", ");
						psuc.format("%s));\n", values);
						
						values.setLength(0);
						psuc.println("\t\t\tstmnt.step();");
						psuc.println("\t\t\tif (stmnt.columnInt(0) == 0)");
						psuc.println("\t\t\t{");
						
						psuc.format("\t\t\t\tconn.exec(%s);\n", generateInsertQuery(c, c.getLowerName()));
						values.append(String.format("\t\t\t\tconn.exec(%s);\n", generateUpdateQuery(c, c.getLowerName())));
						if (versionedClass != null && versionedClass != c)
						{
							psuc.format("\t\t\t\tif (v%s != null)\n\t\t\t\t{\n\t\t\t\t\tconn.exec(%s);\n\t\t\t\t}\n", c.getLowerName(), generateInsertQuery(versionedClass, "v"+c.getLowerName()));
							values.append(String.format("\t\t\t\tif (v%s != null)\n\t\t\t\t{\n\t\t\t\t\tconn.exec(%s);\n\t\t\t\t}\n", c.getLowerName(), generateUpdateQuery(versionedClass, "v"+c.getLowerName())));
						}
						
						if (c.hasField("type"))
						{
							psuc.format("\t\t\t\tswitch(%s.getType())\n", c.getLowerName());
							values.append(String.format("\t\t\t\tswitch(%s.getType())\n", c.getLowerName()));
							psuc.println("\t\t\t\t{");
							values.append("\t\t\t\t{\n");
							
							for(Enum<?> e : enumType.getEnumConstants())
							{
								psuc.format("\t\t\t\t\tcase %s:\n", e.name());
								values.append(String.format("\t\t\t\t\tcase %s:\n", e.name()));
								psuc.println("\t\t\t\t\t{");
								values.append("\t\t\t\t\t{\n");
								
								for(Class sub : c.getSubers())
								{
									if (sub.getUpperName().matches(e.name()))
									{
										psuc.format("\t\t\t\t\t\tI%s %s = I%s.class.cast(%s);\n", sub.getUpperName(), sub.getLowerName(), sub.getUpperName(), c.getLowerName());
										values.append(String.format("\t\t\t\t\t\tI%s %s = I%s.class.cast(%s);\n", sub.getUpperName(), sub.getLowerName(), sub.getUpperName(), c.getLowerName()));
										psuc.format("\t\t\t\t\t\tconn.exec(%s);\n", generateInsertQuery(sub, sub.getLowerName()));
										values.append(String.format("\t\t\t\t\t\tconn.exec(%s);\n", generateUpdateQuery(sub, sub.getLowerName())));
										if (versionedClass != c)
										{
											Class vsub = getVersionedClass(sub);
											if (vsub != null && vsub != sub)
											{
												psuc.format("\t\t\t\t\t\tif (v%s != null)\n", c.getLowerName());
												values.append(String.format("\t\t\t\t\t\tif (v%s != null)\n", c.getLowerName()));
												psuc.println("\t\t\t\t\t\t{");
												values.append("\t\t\t\t\t\t{\n");
												psuc.format("\t\t\t\t\t\t\tI%s %s = I%s.class.cast(%s);\n", vsub.getUpperName(), vsub.getLowerName(), vsub.getUpperName(), c.getLowerName());
												values.append(String.format("\t\t\t\t\t\t\tI%s %s = I%s.class.cast(%s);\n", vsub.getUpperName(), vsub.getLowerName(), vsub.getUpperName(), c.getLowerName()));
												psuc.format("\t\t\t\t\t\t\tconn.exec(%s);\n", generateInsertQuery(vsub, vsub.getLowerName()));
												values.append(String.format("\t\t\t\t\t\t\tconn.exec(%s);\n", generateUpdateQuery(vsub, vsub.getLowerName())));
												psuc.println("\t\t\t\t\t\t}");
												values.append("\t\t\t\t\t\t}\n");
											}																					
										}
									}
								}
								
								psuc.println("\t\t\t\t\t\tbreak;");
								values.append("\t\t\t\t\t\tbreak;\n");
								psuc.println("\t\t\t\t\t}");
								values.append("\t\t\t\t\t}\n");
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
						psuc.println("\t\t\telse");
						psuc.println("\t\t\t{");
						psuc.print(values);					
						psuc.println("\t\t\t}");
						psuc.println("\t\t}");
						psuc.println("\t\tcatch(Exception e)");
						psuc.println("\t\t{");
						psuc.println("\t\t\tthrow new SQLiteDBException(e);");
						psuc.println("\t\t}");
						psuc.println("\t}");
					}
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
							
							set.append(getFieldQuery(f, ","));
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
							
							where.append(getFieldQuery(f, " AND"));
							whereValues.append(getFieldValue(var, f));
							
							pkComma = true;
						}						
					}
					
					if (set.length() == 0) return "\";\"";
					return String.format("String.format(\"UPDATE %s SET %s WHERE %s ;\", %s, %s)", c.getUpperName(), set, where, setValues, whereValues);
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
					
					result.append(String.format(") VALUES (%s);\", %s)", flags, values));
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
				
				private String getFieldQuery(Field f, String comma)
				{
					String flag = getFieldQueryFlag(f);					
					return String.format(" %s = %s", f.getLowerName(), flag);
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
				
				
			}, ormPackageName, Exception.class, inheritances);
			
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
