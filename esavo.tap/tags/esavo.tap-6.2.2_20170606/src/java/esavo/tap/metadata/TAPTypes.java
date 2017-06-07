/*******************************************************************************
 * Copyright (C) 2017 European Space Agency
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package esavo.tap.metadata;

/*
 * This file is part of TAPLibrary.
 * 
 * TAPLibrary is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * TAPLibrary is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with TAPLibrary.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright 2012 - UDS/Centre de Données astronomiques de Strasbourg (CDS)
 */

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * <p>
 * 	Gathers all types used by a TAP service and described in the IVOA document for TAP.
 * 	This class lets "translating" a DB type into a VOTable field type and vice-versa.
 * 	You can also add some DB type aliases, that's to say other other names for the existing DB types:
 * 	smallint, integer, bigint, real, double, binary, varbinary, char, varchar, blob, clob, timestamp, point, region.
 * 	For instance: TEXT &lt;-&gt; VARCHAR.
 * </p>
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 11/2011
 * 
 * @see VotType
 */
public final class TAPTypes {

	private static final Map<String, VotType> dbTypes;
	private static final Map<Class, VotType> classTypes;
	private static final Map<String, String> tapTypes;
	private static final Map<String, String> dbTypeAliases;
	private static final Map<VotType, String> votTypes;
	private static final Map<VotType, Integer> sqlTypes;

	public static final String BOOLEAN = "BOOLEAN";
	public static final String SMALLINT = "SMALLINT";
	public static final String INTEGER = "INTEGER";
	public static final String BIGINT = "BIGINT";
	public static final String REAL = "REAL";
	public static final String DOUBLE_PRECISION = "DOUBLE PRECISION";
	public static final String DOUBLE = "DOUBLE";
	public static final String BINARY = "BINARY";
	public static final String VARBINARY = "VARBINARY";
	public static final String CHAR = "CHAR";
	public static final String VARCHAR = "VARCHAR";
	public static final String BLOB = "BLOB";
	public static final String CLOB = "CLOB";
	public static final String TIMESTAMP = "TIMESTAMP";
	public static final String POINT = "POINT";
	public static final String REGION = "REGION";

	/** No array size. */
	public static final int NO_SIZE = -1;

	/** Means '*' (i.e. char(*)). */
	public static final int STAR_SIZE = -12345;

	static {
		dbTypes = new HashMap<String, VotType>(15);
		classTypes = new HashMap<Class, VotType>(15);
		tapTypes = new HashMap<String, String>(15);
		votTypes = new HashMap<VotType, String>(8);
		sqlTypes = new HashMap<VotType, Integer>();

		VotType type = new VotType("boolean", 1, null);
		tapTypes.put(BOOLEAN, BOOLEAN);
		dbTypes.put(BOOLEAN, type);
		classTypes.put(Boolean.class, type);
		votTypes.put(type, BOOLEAN);
		sqlTypes.put(type, java.sql.Types.BOOLEAN);

		type = new VotType("short", 1, null);
		tapTypes.put(SMALLINT, SMALLINT);
		dbTypes.put(SMALLINT, type);
		classTypes.put(Short.class, type);
		votTypes.put(type, SMALLINT);
		sqlTypes.put(type, java.sql.Types.SMALLINT);

		type = new VotType("unsignedByte", 1, null);
		dbTypes.put(SMALLINT, type);
		classTypes.put(Short.class, type);
		votTypes.put(type, SMALLINT);
		sqlTypes.put(type, java.sql.Types.SMALLINT);

		type = new VotType("int", 1, null);
		dbTypes.put(INTEGER, type);
		classTypes.put(Integer.class, type);
		tapTypes.put(INTEGER, INTEGER);
		votTypes.put(type, INTEGER);
		sqlTypes.put(type, java.sql.Types.INTEGER);

		type = new VotType("long", 1, null);
		dbTypes.put(BIGINT, type);
		classTypes.put(Long.class, type);
		tapTypes.put(BIGINT, BIGINT);
		votTypes.put(type, BIGINT);
		sqlTypes.put(type, java.sql.Types.BIGINT);

		type = new VotType("float", 1, null);
		dbTypes.put(REAL, type);
		classTypes.put(Float.class, type);
		tapTypes.put(REAL, REAL);
		votTypes.put(type, REAL);
		sqlTypes.put(type, java.sql.Types.REAL);

		type = new VotType("double", 1, null);
		dbTypes.put(DOUBLE_PRECISION, type);
		classTypes.put(Double.class, type);
		tapTypes.put(DOUBLE_PRECISION, "DOUBLE");
		votTypes.put(type, DOUBLE_PRECISION);
		sqlTypes.put(type, java.sql.Types.DOUBLE);

		//dbTypes.put(BINARY, new VotType("unsignedByte", 1, null));
		//tapTypes.put(BINARY, BINARY);

		type = new VotType("unsignedByte", STAR_SIZE, null);
		dbTypes.put(VARBINARY, type);
		tapTypes.put(VARBINARY, VARBINARY);
		votTypes.put(type, VARBINARY);
		sqlTypes.put(type, java.sql.Types.VARBINARY);

		type = new VotType("char", 1, null);
		dbTypes.put(VARCHAR, type);
		classTypes.put(String.class, type);
		tapTypes.put(VARCHAR, VARCHAR);
		votTypes.put(type, VARCHAR);
		sqlTypes.put(type, java.sql.Types.VARCHAR);

		type = new VotType("char", STAR_SIZE, null);
		dbTypes.put(VARCHAR, type);
		classTypes.put(String.class, type);
		tapTypes.put(VARCHAR, VARCHAR);
		votTypes.put(type, VARCHAR);
		sqlTypes.put(type, java.sql.Types.VARCHAR);

		type = new VotType("unsignedByte", STAR_SIZE, "adql:BLOB");
		dbTypes.put(BLOB, type);
		tapTypes.put(BLOB, BLOB);
		votTypes.put(type, BLOB);
		sqlTypes.put(type, java.sql.Types.BLOB);

		type = new VotType("char", STAR_SIZE, "adql:CLOB");
		dbTypes.put(CLOB, type);
		tapTypes.put(CLOB, CLOB);
		votTypes.put(type, CLOB);
		sqlTypes.put(type, java.sql.Types.CLOB);

		type = new VotType("char", STAR_SIZE, "adql:TIMESTAMP");
		dbTypes.put(TIMESTAMP, type);
		classTypes.put(Timestamp.class, type);
		tapTypes.put(TIMESTAMP, TIMESTAMP);
		votTypes.put(type, TIMESTAMP);
		sqlTypes.put(type, java.sql.Types.TIMESTAMP);

		type = new VotType("char", STAR_SIZE, "adql:POINT");
		dbTypes.put(POINT, type);
		tapTypes.put(POINT, POINT);
		votTypes.put(type, POINT);

		type = new VotType("char", STAR_SIZE, "adql:REGION");
		dbTypes.put(REGION, type);
		tapTypes.put(REGION, REGION);
		votTypes.put(type, REGION);

		dbTypeAliases = new HashMap<String, String>(8);
		// PostgreSQL data types:
		dbTypeAliases.put("INT2", SMALLINT);
		dbTypeAliases.put("INT", INTEGER);
		dbTypeAliases.put("INT4", INTEGER);
		dbTypeAliases.put("INT8", BIGINT);
		dbTypeAliases.put("FLOAT4", REAL);
		dbTypeAliases.put("FLOAT8", DOUBLE_PRECISION);
		dbTypeAliases.put("TEXT", VARCHAR);
		dbTypeAliases.put("SPOINT", POINT);
		dbTypeAliases.put("DOUBLE", DOUBLE_PRECISION);
	}

	/**
	 * Gets all DB types.
	 * @return	An iterator on DB type name.
	 */
	public static final Iterator<String> getDBTypes(){
		return dbTypes.keySet().iterator();
	}

	/**
	 * Gets all DB type aliases.
	 * @return	An iterator on Entry&lt;String,String&gt; whose the key is the alias and the value is its corresponding DB type.
	 */
	public static final Iterator<Entry<String,String>> getDBTypeAliases(){
		return dbTypeAliases.entrySet().iterator();
	}

	/**
	 * Gets all VOTable types.
	 * @return	An iterator on {@link VotType}.
	 */
	public static final Iterator<VotType> getVotTypes(){
		return votTypes.keySet().iterator();
	}

	/**
	 * <p>Gets the TAP type corresponding to the given DB type (or a DB type alias).</p>
	 * <b>Important:</b>
	 * <ul>
	 * 	<li>Spaces before and after the DB type are automatically removed,</li>
	 * 	<li>The DB type is automatically formatted in UPPER-CASE,</li>
	 * 	<li>Nothing is done if the given DB type is <code>null</code> or empty.</li>
	 * </ul>
	 * 
	 * @param dbType	A DB type (ex: SMALLINT, INTEGER, VARCHAR, POINT, ...)
	 * 
	 * @return	The corresponding TAP type or <code>null</code> if not found.
	 */
	public static final String getTapType(String dbType){
		if (dbType == null)
			return null;

		// Normalize the type name (upper case and with no leading and trailing spaces):
		dbType = dbType.trim().toUpperCase();
		if (dbType.length() == 0)
			return null;

		// Search the corresponding VOTable type:
		String tapType = tapTypes.get(dbType);

		return tapType;
	}

	

	/**
	 * <p>Gets the VOTable type corresponding to the given DB type (or a DB type alias).</p>
	 * <b>Important:</b>
	 * <ul>
	 * 	<li>Spaces before and after the DB type are automatically removed,</li>
	 * 	<li>The DB type is automatically formatted in UPPER-CASE,</li>
	 * 	<li>Nothing is done if the given DB type is <code>null</code> or empty.</li>
	 * </ul>
	 * 
	 * @param dbType	A DB type (ex: SMALLINT, INTEGER, VARCHAR, POINT, ...)
	 * 
	 * @return	The corresponding VOTable type or <code>null</code> if not found.
	 */
	public static final VotType getVotType(String dbType){
		if (dbType == null)
			return null;

		// Normalize the type name (upper case and with no leading and trailing spaces):
		dbType = dbType.trim().toUpperCase();
		if (dbType.length() == 0)
			return null;

		// Search the corresponding VOTable type:
		VotType votType = dbTypes.get(dbType);
		// If no match, try again considering the given type as an alias:
		if (votType == null)
			votType = dbTypes.get(dbTypeAliases.get(dbType));

		return votType;
	}

	/**
	 * <p>Gets the VOTable type corresponding to the given Class type.</p>
	 * 
	 * @param dbType	A Class type (ex: Integer.class, ...)
	 * 
	 * @return	The corresponding VOTable type or <code>null</code> if not found.
	 */
	public static final VotType getVotType(Class classType){
		if (classType == null)
			return null;

		// Search the corresponding VOTable type:
		VotType votType = classTypes.get(classType);

		return votType;
	}

	
	/**
	 * <p>Gets the VOTable type (with the given arraysize) corresponding to the given DB type (or a DB type alias).</p>
	 * <b>Important:</b>
	 * <ul>
	 * 	<li>Spaces before and after the DB type are automatically removed,</li>
	 * 	<li>The DB type is automatically formatted in UPPER-CASE,</li>
	 * 	<li>Nothing is done if the given DB type is <code>null</code> or empty,</li>
	 * 	<li>The given arraysize is used only if the found VOTable type is not special (that's to say: <code>xtype</code> is <code>null</code>).</li>
	 * </ul>
	 * 
	 * @param dbType	A DB type (ex: SMALLINT, INTEGER, VARCHAR, POINT, ...)
	 * @param arraysize	Arraysize to set in the found VOTable type.
	 * 
	 * @return	The corresponding VOTable type or <code>null</code> if not found.
	 */
	public static final VotType getVotType(String dbType, int arraysize){
		VotType votType = getVotType(dbType);

		// If there is a match, set the arraysize:
		if (votType != null && votType.xtype == null && arraysize > 0)
			votType = new VotType(votType.datatype, arraysize, null);

		return votType;
	}

	/**
	 * 
	 * <p>Gets the DB type corresponding to the given DB type alias.</p>
	 * <b>Important:</b>
	 * <ul>
	 * 	<li>Spaces before and after the DB type are automatically removed,</li>
	 * 	<li>The DB type is automatically formatted in UPPER-CASE,</li>
	 * 	<li>If the given DB type is not alias but directly a DB type, it is immediately return.</li>
	 * </ul>
	 * 
	 * @param dbTypeAlias	A DB type alias.
	 * 
	 * @return		The corresponding DB type or <code>null</code> if not found.
	 */
	public static final String getDBType(String dbTypeAlias){
		if (dbTypeAlias == null)
			return null;

		// Normalize the type name:
		dbTypeAlias = dbTypeAlias.trim().toUpperCase();
		if (dbTypeAlias.length() == 0)
			return null;

		// Get the corresponding DB type:
		if (dbTypes.containsKey(dbTypeAlias))
			return dbTypeAlias;
		else
			return dbTypeAliases.get(dbTypeAlias);
	}

	/**
	 * 
	 * <p>Gets the DB type corresponding to the given VOTable field type.</p>
	 * <b>Important:</b>
	 * <ul>
	 * 	<li>The research is made only on the following fields: <code>datatype</code> and <code>xtype</code>,</li>
	 * 	<li>Case <b>insensitive</b> research.</li>
	 * </ul>
	 * 
	 * @param type	A VOTable type.
	 * 
	 * @return		The corresponding DB type or <code>null</code> if not found.
	 */
	public static final String getDBType(final VotType type){
		if (type == null)
			return null;

		String dbType = votTypes.get(type); 
		
		return dbType;
	}

	/**
	 * 
	 * <p>Gets the SLQ type corresponding to the given VOTable field type.</p>
	 * <b>Important:</b>
	 * <ul>
	 * 	<li>The research is made only on the following fields: <code>datatype</code> and <code>xtype</code>,</li>
	 * 	<li>Case <b>insensitive</b> research.</li>
	 * </ul>
	 * 
	 * @param type	A VOTable type.
	 * 
	 * @return		The corresponding SQL type or <code>null</code> if not found.
	 */
	public static final Integer getSQLType(final VotType type){
		if (type == null)
			return null;
		return sqlTypes.get(type);
	}

	/**
	 * <p>Adds, replaces or removes a DB type alias.</p>
	 * <b>Important:</b>
	 * <ul>
	 * 	<li>Spaces before and after the DB type are automatically removed,</li>
	 * 	<li>The DB type is automatically formatted in UPPER-CASE,</li>
	 * 	<li>The same "normalizations" are done on the given alias (so the case sensitivity is ignored),</li>
	 * 	<li>Nothing is done if the given alias is <code>null</code> or empty,</li>
	 * 	<li>If the given DB type is <code>null</code>, the given alias is removed,</li>
	 * 	<li>Nothing is done if the given DB type (!= null) does not match with a known DB type.</li>
	 * </ul>
	 * 
	 * @param alias		A DB type alias (ex: spoint)
	 * @param dbType	A DB type (ex: POINT).
	 * 
	 * @return	<code>true</code> if the association has been updated, <code>false</code> otherwise.
	 */
	public static final boolean putDBTypeAlias(String alias, String dbType){
		if (alias == null)
			return false;

		// Normalize the given alias:
		alias = alias.trim().toUpperCase();
		if (alias.length() == 0)
			return false;

		// Check the existence of the given DB type:
		if (dbType != null){
			dbType = dbType.trim().toUpperCase();
			if (dbType.length() == 0)
				return false;
			else if (!dbTypes.containsKey(dbType))
				return false;
		}

		// Update the map of aliases:
		if (dbType == null)
			dbTypeAliases.remove(alias);
		else
			dbTypeAliases.put(alias, dbType);

		return true;
	}


	/** SELF TEST */
	public final static void main(final String[] args) throws Exception {
		System.out.println("***** DB TYPES *****");
		Iterator<String> itDB = TAPTypes.getDBTypes();
		while(itDB.hasNext())
			System.out.println("\t- "+itDB.next());

		System.out.println("\n***** DB TYPE ALIASES *****");
		Iterator<Entry<String,String>> itAliases = TAPTypes.getDBTypeAliases();
		while(itAliases.hasNext()){
			Entry<String, String> e = itAliases.next();
			System.out.println("\t- "+e.getKey()+" = "+e.getValue());
		}

		System.out.println("\n***** VOTABLE TYPES *****");
		Iterator<VotType> itVot = TAPTypes.getVotTypes();
		while(itVot.hasNext())
			System.out.println("\t- "+itVot.next());


		byte[] buffer = new byte[1024];
		int nbRead = 0;
		String type = null;

		System.out.print("\nDB Type ? ");
		nbRead=System.in.read(buffer); type = new String(buffer, 0, nbRead);
		System.out.println(TAPTypes.getVotType(type));

		int arraysize = 1;
		String xtype = null;
		VotType votType = null;
		System.out.print("\nVOTable datatype ? ");
		nbRead=System.in.read(buffer); type = (new String(buffer, 0, nbRead)).trim();
		System.out.print("VOTable arraysize ? ");
		nbRead=System.in.read(buffer);
		try{
			arraysize = Integer.parseInt((new String(buffer, 0, nbRead)).trim());
		}catch(NumberFormatException nfe){
			arraysize = STAR_SIZE;
		}
		System.out.print("VOTable xtype ? ");
		nbRead=System.in.read(buffer); xtype = (new String(buffer, 0, nbRead)).trim(); if (xtype != null && xtype.length() == 0) xtype = null;
		votType = new VotType(type, arraysize, xtype);
		System.out.println(TAPTypes.getDBType(votType));
	}

	public static Integer getColumnArraySize(String dataType, int arraySize){
		if(TAPTypes.VARBINARY.equals(dataType)){
			return null;
		}else{
			return new Integer(arraySize);
		}
	}

	/**
	 * This is required because currently, STIL creates a short array for byte arrays
	 * @param dataType
	 * @param arraySize
	 * @return
	 */
	public static boolean checkVarBinaryRequired(String dataType, int arraySize){
		if(dataType.equals(TAPTypes.VARBINARY)){
			return true;
		}
		if(arraySize == TAPTypes.STAR_SIZE){
			if(dataType.equals(TAPTypes.BIGINT) ||
					dataType.equals(TAPTypes.BOOLEAN) ||
					dataType.equals(TAPTypes.DOUBLE_PRECISION) ||
					dataType.equals(TAPTypes.DOUBLE) ||
					dataType.equals(TAPTypes.INTEGER) ||
					dataType.equals(TAPTypes.REAL) ||
					dataType.equals(TAPTypes.SMALLINT)){
				return true;
			}
		}
		return false;
	}
	
	public static int getEffectiveSQLType(TAPColumn c){
		if(checkVarBinaryRequired(c.getDatatype(), c.getArraySize())){
			return java.sql.Types.VARBINARY;
		}else{
			VotType type = c.getVotType();
			return getSQLType(type);
		}
	}
}
