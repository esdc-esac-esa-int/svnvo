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
package esavo.adql.translator;

/*
 * This file is part of ADQLLibrary.
 * 
 * ADQLLibrary is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ADQLLibrary is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with ADQLLibrary.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright 2012-2014 - UDS/Centre de Données astronomiques de Strasbourg (CDS),
 *                       Astronomisches Rechen Institute (ARI)
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import esavo.adql.db.DBColumn;
import esavo.adql.db.DBTable;
import esavo.adql.db.exception.UnresolvedJoin;
import esavo.adql.query.ADQLList;
import esavo.adql.query.ADQLObject;
import esavo.adql.query.ADQLOrder;
import esavo.adql.query.ADQLQuery;
import esavo.adql.query.ClauseConstraints;
import esavo.adql.query.ClauseSelect;
import esavo.adql.query.ColumnReference;
import esavo.adql.query.IdentifierField;
import esavo.adql.query.SelectAllColumns;
import esavo.adql.query.SelectItem;
import esavo.adql.query.constraint.ADQLConstraint;
import esavo.adql.query.constraint.Between;
import esavo.adql.query.constraint.Comparison;
import esavo.adql.query.constraint.ConstraintsGroup;
import esavo.adql.query.constraint.Exists;
import esavo.adql.query.constraint.In;
import esavo.adql.query.constraint.IsNull;
import esavo.adql.query.constraint.NotConstraint;
import esavo.adql.query.from.ADQLJoin;
import esavo.adql.query.from.ADQLTable;
import esavo.adql.query.from.FromContent;
import esavo.adql.query.operand.ADQLColumn;
import esavo.adql.query.operand.ADQLOperand;
import esavo.adql.query.operand.Concatenation;
import esavo.adql.query.operand.NegativeOperand;
import esavo.adql.query.operand.NumericConstant;
import esavo.adql.query.operand.Operation;
import esavo.adql.query.operand.StringConstant;
import esavo.adql.query.operand.WrappedOperand;
import esavo.adql.query.operand.function.ADQLFunction;
import esavo.adql.query.operand.function.MathFunction;
import esavo.adql.query.operand.function.SQLFunction;
import esavo.adql.query.operand.function.SQLFunctionType;
import esavo.adql.query.operand.function.UserDefinedFunction;
import esavo.adql.query.operand.function.geometry.AreaFunction;
import esavo.adql.query.operand.function.geometry.BoxFunction;
import esavo.adql.query.operand.function.geometry.CentroidFunction;
import esavo.adql.query.operand.function.geometry.CircleFunction;
import esavo.adql.query.operand.function.geometry.ContainsFunction;
import esavo.adql.query.operand.function.geometry.DistanceFunction;
import esavo.adql.query.operand.function.geometry.ExtractCoord;
import esavo.adql.query.operand.function.geometry.ExtractCoordSys;
import esavo.adql.query.operand.function.geometry.GeometryFunction;
import esavo.adql.query.operand.function.geometry.IntersectsFunction;
import esavo.adql.query.operand.function.geometry.PointFunction;
import esavo.adql.query.operand.function.geometry.PolygonFunction;
import esavo.adql.query.operand.function.geometry.RegionFunction;
import esavo.adql.query.operand.function.geometry.GeometryFunction.GeometryValue;


/**
 * <p>Translates all ADQL objects into the SQL adaptation of Postgres.</p>
 * 
 * <p><b><u>IMPORTANT:</u> The geometrical functions are translated exactly as in ADQL.
 * You will probably need to extend this translator to correctly manage the geometrical functions.
 * An extension is already available for PgSphere: {@link PgSphereTranslator}.</b></p>
 * 
 * @author Gr&eacute;gory Mantelet (CDS;ARI)
 * @version 1.2 (03/2014)
 * 
 * @see PgSphereTranslator
 */
public class PostgreSQLTranslator implements ADQLTranslator {

	protected boolean inSelect = false;
	protected byte caseSensitivity = 0x00;

	/**
	 * Builds a PostgreSQLTranslator which takes into account the case sensitivity on column names.
	 * It means that column names which have been written between double quotes, will be also translated between double quotes.
	 */
	public PostgreSQLTranslator(){
		this(true);
	}

	/**
	 * Builds a PostgreSQLTranslator.
	 * 
	 * @param column	<i>true</i> to take into account the case sensitivity of column names, <i>false</i> otherwise.
	 */
	public PostgreSQLTranslator(final boolean column){
		caseSensitivity = IdentifierField.COLUMN.setCaseSensitive(caseSensitivity, column);
	}

	/**
	 * Builds a PostgreSQLTranslator.
	 * 
	 * @param catalog	<i>true</i> to take into account the case sensitivity of catalog names, <i>false</i> otherwise.
	 * @param schema	<i>true</i> to take into account the case sensitivity of schema names, <i>false</i> otherwise.
	 * @param table		<i>true</i> to take into account the case sensitivity of table names, <i>false</i> otherwise.
	 * @param column	<i>true</i> to take into account the case sensitivity of column names, <i>false</i> otherwise.
	 */
	public PostgreSQLTranslator(final boolean catalog, final boolean schema, final boolean table, final boolean column){
		caseSensitivity = IdentifierField.CATALOG.setCaseSensitive(caseSensitivity, catalog);
		caseSensitivity = IdentifierField.SCHEMA.setCaseSensitive(caseSensitivity, schema);
		caseSensitivity = IdentifierField.TABLE.setCaseSensitive(caseSensitivity, table);
		caseSensitivity = IdentifierField.COLUMN.setCaseSensitive(caseSensitivity, column);
	}

	/**
	 * Appends the full name of the given table to the given StringBuffer.
	 * 
	 * @param str		The string buffer.
	 * @param dbTable	The table whose the full name must be appended.
	 * 
	 * @return			The string buffer + full table name.
	 */
	public final StringBuffer appendFullDBName(final StringBuffer str, final DBTable dbTable){
		if (dbTable != null){
			if (dbTable.getDBCatalogName() != null)
				appendIdentifier(str, dbTable.getDBCatalogName(), IdentifierField.CATALOG).append('.');

			if (dbTable.getDBSchemaName() != null)
				appendIdentifier(str, dbTable.getDBSchemaName(), IdentifierField.SCHEMA).append('.');

			appendIdentifier(str, dbTable.getDBName(), IdentifierField.TABLE);
		}
		return str;
	}

	/**
	 * Appends the given identifier in the given StringBuffer.
	 * 
	 * @param str		The string buffer.
	 * @param id		The identifier to append.
	 * @param field		The type of identifier (column, table, schema, catalog or alias ?).
	 * 
	 * @return			The string buffer + identifier.
	 */
	public final StringBuffer appendIdentifier(final StringBuffer str, final String id, final IdentifierField field){
		return appendIdentifier(str, id, field.isCaseSensitive(caseSensitivity));
	}

	/**
	 * Appends the given identifier to the given StringBuffer.
	 * 
	 * @param str				The string buffer.
	 * @param id				The identifier to append.
	 * @param caseSensitive		<i>true</i> to format the identifier so that preserving the case sensitivity, <i>false</i> otherwise.
	 * 
	 * @return					The string buffer + identifier.
	 */
	public static final StringBuffer appendIdentifier(final StringBuffer str, final String id, final boolean caseSensitive){
		if (caseSensitive)
			return str.append('\"').append(id).append('\"');
		else
			return str.append(id);
	}

	@Override
	@SuppressWarnings("unchecked")
	public String translate(ADQLObject obj) throws TranslationException{
		if (obj instanceof ADQLQuery)
			return translate((ADQLQuery)obj);
		else if (obj instanceof ADQLList)
			return translate((ADQLList)obj);
		else if (obj instanceof SelectItem)
			return translate((SelectItem)obj);
		else if (obj instanceof ColumnReference)
			return translate((ColumnReference)obj);
		else if (obj instanceof ADQLTable)
			return translate((ADQLTable)obj);
		else if (obj instanceof ADQLJoin)
			return translate((ADQLJoin)obj);
		else if (obj instanceof ADQLOperand)
			return translate((ADQLOperand)obj);
		else if (obj instanceof ADQLConstraint)
			return translate((ADQLConstraint)obj);
		else
			return obj.toADQL();
	}

	@Override
	public String translate(ADQLQuery query) throws TranslationException{
		StringBuffer sql = new StringBuffer(translate(query.getSelect()));

		sql.append("\nFROM ").append(translate(query.getFrom()));

		if (!query.getWhere().isEmpty())
			sql.append('\n').append(translate(query.getWhere()));

		if (!query.getGroupBy().isEmpty())
			sql.append('\n').append(translate(query.getGroupBy()));

		if (!query.getHaving().isEmpty())
			sql.append('\n').append(translate(query.getHaving()));

		if (!query.getOrderBy().isEmpty())
			sql.append('\n').append(translate(query.getOrderBy()));

		if (query.getSelect().hasLimit())
			sql.append("\nLimit ").append(query.getSelect().getLimit());

		return sql.toString();
	}

	/* *************************** */
	/* ****** LIST & CLAUSE ****** */
	/* *************************** */
	@Override
	public String translate(ADQLList<? extends ADQLObject> list) throws TranslationException{
		if (list instanceof ClauseSelect)
			return translate((ClauseSelect)list);
		else if (list instanceof ClauseConstraints)
			return translate((ClauseConstraints)list);
		else
			return getDefaultADQLList(list);
	}

	/**
	 * Gets the default SQL output for a list of ADQL objects.
	 * 
	 * @param list	List to format into SQL.
	 * 
	 * @return		The corresponding SQL.
	 * 
	 * @throws TranslationException If there is an error during the translation.
	 */
	protected String getDefaultADQLList(ADQLList<? extends ADQLObject> list) throws TranslationException{
		String sql = (list.getName() == null) ? "" : (list.getName() + " ");

		boolean oldInSelect = inSelect;
		inSelect = (list.getName() != null) && list.getName().equalsIgnoreCase("select");

		try{
			for(int i = 0; i < list.size(); i++)
				sql += ((i == 0) ? "" : (" " + list.getSeparator(i) + " ")) + translate(list.get(i));
		}finally{
			inSelect = oldInSelect;
		}

		return sql;
	}

	@Override
	public String translate(ClauseSelect clause) throws TranslationException{
		String sql = null;

		for(int i = 0; i < clause.size(); i++){
			if (i == 0){
				sql = clause.getName() + (clause.distinctColumns() ? " DISTINCT" : "");
			}else
				sql += " " + clause.getSeparator(i);

			sql += " " + translate(clause.get(i));
		}

		return sql;
	}

	@Override
	public String translate(ClauseConstraints clause) throws TranslationException{
		if (clause instanceof ConstraintsGroup)
			return "(" + getDefaultADQLList(clause) + ")";
		else
			return getDefaultADQLList(clause);
	}

	@Override
	public String translate(SelectItem item) throws TranslationException{
		if (item instanceof SelectAllColumns)
			return translate((SelectAllColumns)item);

		StringBuffer translation = new StringBuffer(translate(item.getOperand()));
		if (item.hasAlias()){
			translation.append(" AS ");
			appendIdentifier(translation, item.getAlias(), item.isCaseSensitive());
		}else
			translation.append(" AS ").append(item.getName());

		return translation.toString();
	}

	@Override
	public String translate(SelectAllColumns item) throws TranslationException{
		HashMap<String,String> mapAlias = new HashMap<String,String>();

		// Fetch the full list of columns to display:
		Iterable<DBColumn> dbCols = null;
		if (item.getAdqlTable() != null && item.getAdqlTable().getDBLink() != null){
			ADQLTable table = item.getAdqlTable();
			dbCols = table.getDBLink();
			if (table.hasAlias()){
				String key = appendFullDBName(new StringBuffer(), table.getDBLink()).toString();
				mapAlias.put(key, table.isCaseSensitive(IdentifierField.ALIAS) ? ("\"" + table.getAlias() + "\"") : table.getAlias());
			}
		}else if (item.getQuery() != null){
			try{
				dbCols = item.getQuery().getFrom().getDBColumns();
			}catch(UnresolvedJoin pe){
				throw new TranslationException("Due to a join problem, the ADQL to SQL translation can not be completed!", pe);
			}
			ArrayList<ADQLTable> tables = item.getQuery().getFrom().getTables();
			for(ADQLTable table : tables){
				if (table.hasAlias()){
					String key = appendFullDBName(new StringBuffer(), table.getDBLink()).toString();
					mapAlias.put(key, table.isCaseSensitive(IdentifierField.ALIAS) ? ("\"" + table.getAlias() + "\"") : table.getAlias());
				}
			}
		}

		// Write the DB name of all these columns:
		if (dbCols != null){
			StringBuffer cols = new StringBuffer();
			for(DBColumn col : dbCols){
				if (cols.length() > 0)
					cols.append(',');
				if (col.getTable() != null){
					String fullDbName = appendFullDBName(new StringBuffer(), col.getTable()).toString();
					if (mapAlias.containsKey(fullDbName))
						appendIdentifier(cols, mapAlias.get(fullDbName), false).append('.');
					else
						cols.append(fullDbName).append('.');
				}
				appendIdentifier(cols, col.getDBName(), IdentifierField.COLUMN);
				cols.append(" AS \"").append(col.getADQLName()).append('\"');
			}
			return (cols.length() > 0) ? cols.toString() : item.toADQL();
		}else{
			return item.toADQL();
		}
	}

	@Override
	public String translate(ColumnReference ref) throws TranslationException{
		if (ref instanceof ADQLOrder)
			return translate((ADQLOrder)ref);
		else
			return getDefaultColumnReference(ref);
	}

	/**
	 * Gets the default SQL output for a column reference.
	 * 
	 * @param ref	The column reference to format into SQL.
	 * 
	 * @return		The corresponding SQL.
	 * 
	 * @throws TranslationException If there is an error during the translation.
	 */
	protected String getDefaultColumnReference(ColumnReference ref) throws TranslationException{
		if (ref.isIndex()){
			return "" + ref.getColumnIndex();
		}else{
			if (ref.getDBLink() == null){
				return (ref.isCaseSensitive() ? ("\"" + ref.getColumnName() + "\"") : ref.getColumnName());
			}else{
				DBColumn dbCol = ref.getDBLink();
				StringBuffer colName = new StringBuffer();
				// Use the table alias if any:
				if (ref.getAdqlTable() != null && ref.getAdqlTable().hasAlias())
					appendIdentifier(colName, ref.getAdqlTable().getAlias(), ref.getAdqlTable().isCaseSensitive(IdentifierField.ALIAS)).append('.');

				// Use the DBTable if any:
				else if (dbCol.getTable() != null)
					appendFullDBName(colName, dbCol.getTable()).append('.');

				appendIdentifier(colName, dbCol.getDBName(), IdentifierField.COLUMN);

				return colName.toString();
			}
		}
	}

	@Override
	public String translate(ADQLOrder order) throws TranslationException{
		return getDefaultColumnReference(order) + (order.isDescSorting() ? " DESC" : " ASC");
	}

	/* ************************** */
	/* ****** TABLE & JOIN ****** */
	/* ************************** */
	@Override
	public String translate(FromContent content) throws TranslationException{
		if (content instanceof ADQLTable)
			return translate((ADQLTable)content);
		else if (content instanceof ADQLJoin)
			return translate((ADQLJoin)content);
		else
			return content.toADQL();
	}

	@Override
	public String translate(ADQLTable table) throws TranslationException{
		StringBuffer sql = new StringBuffer();

		// CASE: SUB-QUERY:
		if (table.isSubQuery())
			sql.append('(').append(translate(table.getSubQuery())).append(')');

		// CASE: TABLE REFERENCE:
		else{
			// Use the corresponding DB table, if known:
			if (table.getDBLink() != null)
				appendFullDBName(sql, table.getDBLink());
			// Otherwise, use the whole table name given in the ADQL query:
			else
				sql.append(table.getFullTableName());
		}

		// Add the table alias, if any:
		if (table.hasAlias()){
			sql.append(" AS ");
			appendIdentifier(sql, table.getAlias(), table.isCaseSensitive(IdentifierField.ALIAS));
		}

		return sql.toString();
	}

	@Override
	public String translate(ADQLJoin join) throws TranslationException{
		StringBuffer sql = new StringBuffer(translate(join.getLeftTable()));

		if (join.isNatural())
			sql.append(" NATURAL");

		sql.append(' ').append(join.getJoinType()).append(' ').append(translate(join.getRightTable())).append(' ');

		if (!join.isNatural()){
			if (join.getJoinCondition() != null)
				sql.append(translate(join.getJoinCondition()));
			else if (join.hasJoinedColumns()){
				StringBuffer cols = new StringBuffer();
				Iterator<ADQLColumn> it = join.getJoinedColumns();
				while(it.hasNext()){
					ADQLColumn item = it.next();
					if (cols.length() > 0)
						cols.append(", ");
					if (item.getDBLink() == null)
						appendIdentifier(cols, item.getColumnName(), item.isCaseSensitive(IdentifierField.COLUMN));
					else
						appendIdentifier(cols, item.getDBLink().getDBName(), IdentifierField.COLUMN);
				}
				sql.append("USING (").append(cols).append(')');
			}
		}

		return sql.toString();
	}

	/* ********************* */
	/* ****** OPERAND ****** */
	/* ********************* */
	@Override
	public String translate(ADQLOperand op) throws TranslationException{
		if (op instanceof ADQLColumn)
			return translate((ADQLColumn)op);
		else if (op instanceof Concatenation)
			return translate((Concatenation)op);
		else if (op instanceof NegativeOperand)
			return translate((NegativeOperand)op);
		else if (op instanceof NumericConstant)
			return translate((NumericConstant)op);
		else if (op instanceof StringConstant)
			return translate((StringConstant)op);
		else if (op instanceof WrappedOperand)
			return translate((WrappedOperand)op);
		else if (op instanceof Operation)
			return translate((Operation)op);
		else if (op instanceof ADQLFunction)
			return translate((ADQLFunction)op);
		else
			return op.toADQL();
	}

	@Override
	public String translate(ADQLColumn column) throws TranslationException{
		// Use its DB name if known:
		if (column.getDBLink() != null){
			DBColumn dbCol = column.getDBLink();
			StringBuffer colName = new StringBuffer();
			// Use the table alias if any:
			if (column.getAdqlTable() != null && column.getAdqlTable().hasAlias())
				appendIdentifier(colName, column.getAdqlTable().getAlias(), column.getAdqlTable().isCaseSensitive(IdentifierField.ALIAS)).append('.');

			// Use the DBTable if any:
			else if (dbCol.getTable() != null && dbCol.getTable().getDBName() != null)
				appendFullDBName(colName, dbCol.getTable()).append('.');

			// Otherwise, use the prefix of the column given in the ADQL query:
			else if (column.getTableName() != null)
				colName = column.getFullColumnPrefix().append('.');

			appendIdentifier(colName, dbCol.getDBName(), IdentifierField.COLUMN);

			return colName.toString();
		}
		// Otherwise, use the whole name given in the ADQL query:
		else
			return column.getFullColumnName();
	}

	@Override
	public String translate(Concatenation concat) throws TranslationException{
		return translate((ADQLList<ADQLOperand>)concat);
	}

	@Override
	public String translate(NegativeOperand negOp) throws TranslationException{
		return "-" + translate(negOp.getOperand());
	}

	@Override
	public String translate(NumericConstant numConst) throws TranslationException{
		return numConst.getValue();
	}

	@Override
	public String translate(StringConstant strConst) throws TranslationException{
		return "'" + strConst.getValue() + "'";
	}

	@Override
	public String translate(WrappedOperand op) throws TranslationException{
		return "(" + translate(op.getOperand()) + ")";
	}

	@Override
	public String translate(Operation op) throws TranslationException{
		return translate(op.getLeftOperand()) + op.getOperation().toADQL() + translate(op.getRightOperand());
	}

	/* ************************ */
	/* ****** CONSTRAINT ****** */
	/* ************************ */
	@Override
	public String translate(ADQLConstraint cons) throws TranslationException{
		if (cons instanceof Comparison)
			return translate((Comparison)cons);
		else if (cons instanceof Between)
			return translate((Between)cons);
		else if (cons instanceof Exists)
			return translate((Exists)cons);
		else if (cons instanceof In)
			return translate((In)cons);
		else if (cons instanceof IsNull)
			return translate((IsNull)cons);
		else if (cons instanceof NotConstraint)
			return translate((NotConstraint)cons);
		else
			return cons.toADQL();
	}

	@Override
	public String translate(Comparison comp) throws TranslationException{
		return translate(comp.getLeftOperand()) + " " + comp.getOperator().toADQL() + " " + translate(comp.getRightOperand());
	}

	@Override
	public String translate(Between comp) throws TranslationException{
		return translate(comp.getLeftOperand()) + " BETWEEN " + translate(comp.getMinOperand()) + " AND " + translate(comp.getMaxOperand());
	}

	@Override
	public String translate(Exists exists) throws TranslationException{
		return "EXISTS(" + translate(exists.getSubQuery()) + ")";
	}

	@Override
	public String translate(In in) throws TranslationException{
		return translate(in.getOperand()) + " " + in.getName() + " (" + (in.hasSubQuery() ? translate(in.getSubQuery()) : translate(in.getValuesList())) + ")";
	}

	@Override
	public String translate(IsNull isNull) throws TranslationException{
		return translate(isNull.getColumn()) + " IS " + (isNull.isNotNull() ? "NOT " : "") + "NULL";
	}

	@Override
	public String translate(NotConstraint notCons) throws TranslationException{
		return "NOT " + translate(notCons.getConstraint());
	}

	/* *********************** */
	/* ****** FUNCTIONS ****** */
	/* *********************** */
	@Override
	public String translate(ADQLFunction fct) throws TranslationException{
		if (fct instanceof GeometryFunction)
			return translate((GeometryFunction)fct);
		else if (fct instanceof MathFunction)
			return translate((MathFunction)fct);
		else if (fct instanceof SQLFunction)
			return translate((SQLFunction)fct);
		else if (fct instanceof UserDefinedFunction)
			return translate((UserDefinedFunction)fct);
		else
			return getDefaultADQLFunction(fct);
	}

	/**
	 * Gets the default SQL output for the given ADQL function.
	 * 
	 * @param fct	The ADQL function to format into SQL.
	 * 
	 * @return		The corresponding SQL.
	 * 
	 * @throws TranslationException	If there is an error during the translation.
	 */
	protected String getDefaultADQLFunction(ADQLFunction fct) throws TranslationException{
		String sql = fct.getName() + "(";

		for(int i = 0; i < fct.getNbParameters(); i++)
			sql += ((i == 0) ? "" : ", ") + translate(fct.getParameter(i));

		return sql + ")";
	}

	@Override
	public String translate(SQLFunction fct) throws TranslationException{
		if (fct.getType() == SQLFunctionType.COUNT_ALL)
			return "COUNT(" + (fct.isDistinct() ? "DISTINCT " : "") + "*)";
		else
			return fct.getName() + "(" + (fct.isDistinct() ? "DISTINCT " : "") + translate(fct.getParameter(0)) + ")";
	}

	@Override
	public String translate(MathFunction fct) throws TranslationException{
		switch(fct.getType()){
			case LOG:
				return "ln(" + ((fct.getNbParameters() >= 1) ? translate(fct.getParameter(0)) : "") + ")";
			case LOG10:
				return "log(10, " + ((fct.getNbParameters() >= 1) ? translate(fct.getParameter(0)) : "") + ")";
			case RAND:
				return "random()";
			case TRUNCATE:
				return "trunc(" + ((fct.getNbParameters() >= 2) ? (translate(fct.getParameter(0)) + ", " + translate(fct.getParameter(1))) : "") + ")";
			default:
				return getDefaultADQLFunction(fct);
		}
	}

	@Override
	public String translate(UserDefinedFunction fct) throws TranslationException{
		return getDefaultADQLFunction(fct);
	}

	/* *********************************** */
	/* ****** GEOMETRICAL FUNCTIONS ****** */
	/* *********************************** */
	@Override
	public String translate(GeometryFunction fct) throws TranslationException{
		if (fct instanceof AreaFunction)
			return translate((AreaFunction)fct);
		else if (fct instanceof BoxFunction)
			return translate((BoxFunction)fct);
		else if (fct instanceof CentroidFunction)
			return translate((CentroidFunction)fct);
		else if (fct instanceof CircleFunction)
			return translate((CircleFunction)fct);
		else if (fct instanceof ContainsFunction)
			return translate((ContainsFunction)fct);
		else if (fct instanceof DistanceFunction)
			return translate((DistanceFunction)fct);
		else if (fct instanceof ExtractCoord)
			return translate((ExtractCoord)fct);
		else if (fct instanceof ExtractCoordSys)
			return translate((ExtractCoordSys)fct);
		else if (fct instanceof IntersectsFunction)
			return translate((IntersectsFunction)fct);
		else if (fct instanceof PointFunction)
			return translate((PointFunction)fct);
		else if (fct instanceof PolygonFunction)
			return translate((PolygonFunction)fct);
		else if (fct instanceof RegionFunction)
			return translate((RegionFunction)fct);
		else
			return getDefaultGeometryFunction(fct);
	}

	/**
	 * <p>Gets the default SQL output for the given geometrical function.</p>
	 * 
	 * <p><i><u>Note:</u> By default, only the ADQL serialization is returned.</i></p>
	 * 
	 * @param fct	The geometrical function to translate.
	 * 
	 * @return		The corresponding SQL.
	 * 
	 * @throws TranslationException If there is an error during the translation.
	 */
	protected String getDefaultGeometryFunction(GeometryFunction fct) throws TranslationException{
		if (inSelect)
			return "'" + fct.toADQL().replaceAll("'", "''") + "'";
		else
			return getDefaultADQLFunction(fct);
	}

	@Override
	public String translate(GeometryValue<? extends GeometryFunction> geomValue) throws TranslationException{
		return translate(geomValue.getValue());
	}

	@Override
	public String translate(ExtractCoord extractCoord) throws TranslationException{
		return getDefaultGeometryFunction(extractCoord);
	}

	@Override
	public String translate(ExtractCoordSys extractCoordSys) throws TranslationException{
		return getDefaultGeometryFunction(extractCoordSys);
	}

	@Override
	public String translate(AreaFunction areaFunction) throws TranslationException{
		return getDefaultGeometryFunction(areaFunction);
	}

	@Override
	public String translate(CentroidFunction centroidFunction) throws TranslationException{
		return getDefaultGeometryFunction(centroidFunction);
	}

	@Override
	public String translate(DistanceFunction fct) throws TranslationException{
		return getDefaultGeometryFunction(fct);
	}

	@Override
	public String translate(ContainsFunction fct) throws TranslationException{
		return getDefaultGeometryFunction(fct);
	}

	@Override
	public String translate(IntersectsFunction fct) throws TranslationException{
		return getDefaultGeometryFunction(fct);
	}

	@Override
	public String translate(BoxFunction box) throws TranslationException{
		return getDefaultGeometryFunction(box);
	}

	@Override
	public String translate(CircleFunction circle) throws TranslationException{
		return getDefaultGeometryFunction(circle);
	}

	@Override
	public String translate(PointFunction point) throws TranslationException{
		return getDefaultGeometryFunction(point);
	}

	@Override
	public String translate(PolygonFunction polygon) throws TranslationException{
		return getDefaultGeometryFunction(polygon);
	}

	@Override
	public String translate(RegionFunction region) throws TranslationException{
		return getDefaultGeometryFunction(region);
	}

}
