package esavo.tap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import esavo.adql.db.DBChecker;
import esavo.adql.db.DBTable;
import esavo.adql.db.exception.UnresolvedTableException;
import esavo.adql.parser.ParseException;
import esavo.adql.query.from.ADQLTable;
import esavo.tap.metadata.TAPMetadata;
import esavo.tap.metadata.TAPMetadataLoader;
import esavo.tap.metadata.TAPMetadataLoaderArgs;
import esavo.tap.metadata.TAPSchema;
import esavo.tap.metadata.TAPTable;
import esavo.uws.owner.UwsJobOwner;

public class DBCheckerOnDemand extends DBChecker {
	TAPService service;
	UwsJobOwner owner;
	boolean includeAccessibleSharedItems;
	TAPSchema uploadSchema;
	
	DBCheckerOnDemand(TAPService service, UwsJobOwner owner, boolean includeAccessibleSharedItems, TAPSchema uploadSchema){
		super();
		this.service=service;
		this.owner=owner;
		this.includeAccessibleSharedItems=includeAccessibleSharedItems;
		this.uploadSchema=uploadSchema;
	}
	
	/**
	 *  
	 * @param table	The table to resolve.
	 * 
	 * @return		The corresponding {@link DBTable} if found, <i>null</i> otherwise.
	 * 
	 * @throws ParseException	An {@link UnresolvedTableException} if the given table can't be resolved.
	 */
	@Override
	protected DBTable resolveTable(final ADQLTable table) throws ParseException{
		ArrayList<DBTable> tables = new ArrayList<DBTable>();

		List<String> fullQualifiedTableNameRequestedTableList = new ArrayList<String>();
		fullQualifiedTableNameRequestedTableList.add(table.getFullTableName());
		
		TAPMetadataLoaderArgs args = new TAPMetadataLoaderArgs();
		args.setFullQualifiedTableNames(fullQualifiedTableNameRequestedTableList);
		args.setIncludeShareInfo(TAPMetadataLoader.DO_NOT_INCLUDE_SHARE_INFO);
		args.setIncludeAccessibleSharedItems(includeAccessibleSharedItems);
		args.setOnlySchemas(false);
		args.setOnlyTables(false);
		//args.setSchemaNames(schemaNamesList);

		TAPMetadata meta=null;
		
		try {
			meta = TAPMetadataLoader.getMatchingTables(service, owner, args);
			if(meta!=null && meta.getNbTables()>0){
				Iterator<TAPTable> it = meta.getTables();
				while(it.hasNext()){
					tables.add(it.next());
				}
			}
		} catch (TAPException e) {
			// NO TABLES FOUND. CONTINUE.
		}
		
		
		if (uploadSchema != null){
			for(TAPTable t : uploadSchema.getTables()){
				if(table.getFullTableName().equals(t.getFullName())){
					tables.add(t);
				}
			}
		}
		
		
		// good if only one table has been found:
		if (tables.size() == 1)
			return tables.get(0);
		// but if more than one: ambiguous table name !
		else if (tables.size() > 1)
			throw new UnresolvedTableException(table, tables.get(0).getADQLSchemaName() + "." + tables.get(0).getADQLName(), tables.get(1).getADQLSchemaName() + "." + tables.get(1).getADQLName());
		// otherwise (no match): unknown table !
		else
			throw new UnresolvedTableException(table);
	}

}
