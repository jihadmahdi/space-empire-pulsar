package org.axan.sep.server;

import java.io.File;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.axan.eplib.orm.Class;
import org.axan.eplib.orm.DataBaseORMGenerator;
import org.axan.eplib.orm.Class.Field;
import org.axan.eplib.orm.ClassGeneratorListener;
import org.axan.eplib.orm.nosql.GraphClassGeneratorListener;
import org.axan.eplib.orm.nosql.YamlORMGenerator;
import org.axan.eplib.orm.nosql.GraphClassGeneratorListener.NodeGeneratorListener;
import org.axan.eplib.orm.sql.sqlite.SQLiteORMGenerator;
import org.axan.eplib.utils.Basic;
import org.axan.eplib.utils.Reflect;
import org.axan.eplib.yaml.YamlConfigFile;
import org.axan.sep.common.Protocol.eBuildingType;
import org.axan.sep.common.Protocol.eCelestialBodyType;
import org.axan.sep.common.Protocol.eSpecialUnitType;
import org.axan.sep.common.Protocol.eUnitType;
import org.axan.sep.common.SEPUtils.Location;
import org.axan.sep.common.SEPUtils.RealLocation;
import org.axan.sep.common.db.IDBGraph;
import org.axan.sep.common.db.IGameConfig;
import org.axan.sep.common.db.orm.SEPCommonDB;
import org.neo4j.graphdb.Node;

class ORMGenerator
{

	public static void main(String[] args)
	{
		try
		{					
			String baseDir = "/media/data/code/Java_Workspace/Space-Empire-Pulsar/src/main/java/";
			String interfacePackageName = IGameConfig.class.getPackage().getName();
			String dbPackageName = IDBGraph.class.getPackage().getName();
			final String ormPackageName = dbPackageName+".orm";
			File interfaceOutputDirectory = new File(baseDir+interfacePackageName.replace('.', '/'));
			File outputDirectory = new File(baseDir+ormPackageName.replace('.', '/'));
			
			/* SQLite generator
			File ormTemp = File.createTempFile("SEP_ORM", ".db");
			ormTemp.deleteOnExit();			
			URL dbFileURL = Reflect.getResource(dbPackageName, "SEPSQLiteDB.sql");
			File dbFile = new File(dbFileURL.getFile());
			File inheritencesConfigFile = new File(Reflect.getResource(dbPackageName, "SEPSQLDB.ORM.yaml").getFile());			
			SQLiteORMGenerator gen = new SQLiteORMGenerator(dbFile, interfaceOutputDirectory, outputDirectory);			
			gen.generate(getClassGeneratorListener(ormPackageName), ormPackageName, interfacePackageName, null, inheritencesConfigFile);
			*/
			
			// YAML generator
			URL yamlFileURL = Reflect.getResource(dbPackageName, "SEPDBGraph.yaml");
			File yamlFile = new File(yamlFileURL.getFile());			
			YamlORMGenerator gen = new YamlORMGenerator(yamlFile);
			gen.generate(getGraphClassGeneratorListener(ormPackageName), ormPackageName, interfacePackageName, "DBGraph", interfaceOutputDirectory, outputDirectory);
			
			// TODO: assurer l'import des types paramètres et retour.
			// TODO: méthodes cast pour les membres nested (ex: récupérer l'interface VersionedProductiveCelestialBody d'une VersionedPlanet.
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return;
		}
	}

	private static GraphClassGeneratorListener getGraphClassGeneratorListener(final String ormPackageName)
	{
		return new GraphClassGeneratorListener()
		{			
			@Override
			public ClassGeneratorListener getRelationsGeneratorListener()
			{
				return getRelationClassGeneratorListener(ormPackageName);
			}
			
			@Override
			public NodeGeneratorListener getNodesGeneratorListener()
			{
				return getClassGeneratorListener(ormPackageName);
			}
			
			@Override
			public Set<String> getAPIClassImports()
			{
				return new LinkedHashSet<String>(Arrays.asList(Location.class.getName()));
			}
		};
	}
	
	private static ClassGeneratorListener getRelationClassGeneratorListener(final String ormPackageName)
	{
		return getClassGeneratorListener(ormPackageName);
	}
	
	private static NodeGeneratorListener getClassGeneratorListener(final String ormPackageName)
	{
		return new NodeGeneratorListener()
		{

			@Override
			public boolean skipClass(Class c)
			{
				return(c.getUpperName().matches("GameConfig"));
			}

			@Override
			public void genBasicImports(Class c, Set<String> biImports, Set<String> bcImports)
			{
				Collections.addAll(biImports, Map.class.getName());
				Collections.addAll(bcImports, Node.class.getName(), Map.class.getName());
			}
			
			@Override
			public void genImports(Class c, Set<String> uiImports, Set<String> ucImports)
			{
				Collections.addAll(uiImports, Node.class.getName(), IGameConfig.class.getName(), Map.class.getName(), HashMap.class.getName());
				Collections.addAll(ucImports, Node.class.getName(), IGameConfig.class.getName(), Map.class.getName(), HashMap.class.getName());
								
				for(Field field: c.getAllFields())
				{
					if (field.getLowerName().endsWith("_x"))
					{
						uiImports.add(getLocationRelationField(field).getType().getName());
						ucImports.add(getLocationRelationField(field).getType().getName());
					}
					else if (field.getLowerName().matches("type"))
					{
						uiImports.add(getEnumType(c).getName());
						ucImports.add(getEnumType(c).getName());
					}
					else
					{
						java.lang.Class<?> type = field.getType().isArray() ? field.getType().getComponentType() : field.getType();
						if (!type.isPrimitive() && !Package.getPackage("java.lang").equals(type.getPackage()))
						{
							uiImports.add(field.getType().isArray() ? field.getType().getComponentType().getName() : field.getType().getName());
							ucImports.add(field.getType().isArray() ? field.getType().getComponentType().getName() : field.getType().getName());
						}
					}
				}
			}

			@Override
			public java.lang.Class<?> getStatementClass()
			{
				return Node.class;
			}

			@Override
			protected StringBuilder genBasicCtorLegacyImplementation(Class c, java.lang.Class<? extends Throwable> exceptionClass, StringBuilder ctorFilter, StringBuilder booleanFields, StringBuilder ctorFields)
			{				
				StringBuilder sb = new StringBuilder();
				
				if (!c.getSpecificFields().isEmpty())
				{
					sb.append(ctorFields.toString().replaceAll("\t\t\t", "\t\t"));
				}
				
				return sb;
			}

			public String getBasicFieldFromStatement(Field field)
			{
				return String.format("stmnt.hasProperty(\"%s\") ? %s.class.cast(stmnt.getProperty(\"%s\")) : null", field.getLowerName(), field.getType().getSimpleName(), field.getLowerName());
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
			public Map<String, String> fieldsDeclarations(Class c)
			{
				Map<String, String> fields = new LinkedHashMap<String, String>();

				for(Field f: c.getSpecificFields())
				{
					if (f.getLowerName().endsWith("_x"))
					{
						Field nf = getLocationRelationField(f);
						fields.put(nf.getLowerName(), nf.getType().getSimpleName());
					}

					if (f.getLowerName().matches("type"))
					{
						Field nf = wrapField(c, f);
						fields.put(nf.getLowerName(), nf.getType().getSimpleName());
					}
				}
				
				return fields;
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
			public Map.Entry<String, String> fullConstructorParameter(Class c, Field f)
			{
				Field wf = wrapField(c, f);
				if (wf == null)
					return null; 
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
			public String fullConstructorBaseParameterValue(Class c, Field f)
			{
				Field wf = wrapField(c, f);

				if (wf == null)
					return "";

				if (Location.class.equals(wf.getType()) || RealLocation.class.equals(wf.getType()))
				{
					return String.format("%s == null ? null : %s.x, %s == null ? null : %s.y, %s == null ? null : %s.z", wf.getLowerName(), wf.getLowerName(), wf.getLowerName(), wf.getLowerName(), wf.getLowerName(), wf.getLowerName());
				}
				else if (wf.getLowerName().matches("type"))
				{
					return String.format("%s.toString()", wf.getLowerName());
				}

				return super.fullConstructorBaseParameterValue(c, wf);
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
			public Map<String, String> constructorAdditionalParams(Class c)
			{
				if (c.isInstanceOf(c.getPackage(), "Unit") != null)
				{
					Map<String, String> result = new LinkedHashMap<String, String>();
					result.put("config", IGameConfig.class.getName());
					return result;
				}

				return Collections.EMPTY_MAP;
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
				StringBuilder sb = new StringBuilder();

				for(Field f: c.getSpecificFields())
				{
					Field nf = wrapField(c, f);
					if (nf == null)
						continue;

					if (f.getLowerName().endsWith("_x"))
					{
						//sb.append(String.format("\t\tthis.%s = (base%sProxy.get%s_x() == null ? null : new %s(base%sProxy.get%s_x(), base%sProxy.get%s_y(), base%sProxy.get%s_z()));\n", nf.getLowerName(), c.getUpperName(), nf.getUpperName(), nf.getType().getSimpleName(), c.getUpperName(), nf.getUpperName(), c.getUpperName(), nf.getUpperName(), c.getUpperName(), nf.getUpperName()));
						sb.append(String.format("\t\tthis.%s = new %s(base%sProxy.get%s_x(), base%sProxy.get%s_y(), base%sProxy.get%s_z());\n", nf.getLowerName(), nf.getType().getSimpleName(), c.getUpperName(), nf.getUpperName(), c.getUpperName(), nf.getUpperName(), c.getUpperName(), nf.getUpperName()));
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

				if (c.getUpperName().startsWith("Versioned"))
					return c;
				if (getUnprefixedVersionedTypes().get(c.getUpperName()) != null && getUnprefixedVersionedTypes().get(c.getUpperName()))
					return c;
				// Versioned class must be a sub class.
				for(Class sub: c.getSubers())
				{
					if (sub.getUpperName().matches("Versioned" + c.getUpperName()))
						return sub;
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
				for(Map.Entry<String, java.lang.Class<? extends Enum>> e: getEnumTypes().entrySet())
				{
					if (c.isInstanceOf(ormPackageName, e.getKey()) != null)
					{
						return e.getValue();
					}
				}

				return null;
			}

			@Override
			public Field getterDeclaration(Class c, Field f)
			{
				Field wf = wrapField(c, f);
				if (wf == null)
					return null;

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
				if (wf == null)
					return "";

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
				if (vs == null)
					vs = c.isInstanceOf(c.getPackage(), "VersionedProductiveCelestialBody");

				if (vs != null)
				{
					return vs.hasField(field.getLowerName()) && !c.getSpecificFields().contains(field);
				}

				return false;
			}

		};
	}
}
