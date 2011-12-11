package org.axan.sep.common.db.orm;

/**
 * This class helps with SEP SQL redundant queries.
 */
public class SQLHelper
{
	private SQLHelper() {}
	
	/**
	 * %1$: point A table.
	 * %2$: point A location field
	 * %3$: point B x value
	 * %4$: point B y value
	 * %5$: point B z value
	 * %6$: comparison sign
	 * %7$: comparison value
	 */
	private static final String DISTANCE_CONDITION_FORMULA = "((%1$s%2$s_x - %3$s) * (%1$s%2$s_x - %3$s) + (%1$s%2$s_y - %4$s) * (%1$s%2$s_y - %4$s) + (%1$s%2$s_z - %5$s) * (%1$s%2$s_z - %5$s)) %6$s %7$s";
	
	/**
	 * @see SQLHelper#getDistanceCondition(String, String, String)
	 */
	public static String getDistanceCondition(int firstParameterIndex, String tableAField, String sign)
	{
		return getDistanceCondition(firstParameterIndex, null, tableAField, sign);
	}
	
	/**
	 * Return distance condition formula. Adds 4 parameters to the query: point B coordinates x, y, z, and comparison value.
	 * @param firstParameterIndex Offset to assign to the first parameter. It is equal to the number of bound parameters '?' you use in the query before this condition.
	 * @param tableA Table alias to test
	 * @param tableAField field to test
	 * @param sign sign of the comparison (between integer)
	 * @return
	 */
	public static String getDistanceCondition(int firstParameterIndex, String tableA, String tableAField, String sign)
	{
		tableA = tableA == null ? "" : tableA+".";
		return String.format(DISTANCE_CONDITION_FORMULA, tableA, tableAField, "?"+firstParameterIndex, "?"+(firstParameterIndex+1), "?"+(firstParameterIndex+2), sign, "?");
	}
}
