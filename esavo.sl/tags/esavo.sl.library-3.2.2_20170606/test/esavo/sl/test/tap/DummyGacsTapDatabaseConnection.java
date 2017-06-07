package esavo.sl.test.tap;

import java.util.List;

import esavo.tap.TAPSchemaInfo;
import esavo.tap.db.DBException;
import esavo.tap.db.TapJDBCPooledFunctions;
import esavo.tap.metadata.TAPTable;
import esavo.tap.metadata.TapTableInfo;
import esavo.tap.publicgroup.PublicGroupItem;
import esavo.uws.utils.test.database.DummyUwsDatabaseConnection;

public class DummyGacsTapDatabaseConnection extends esavo.tap.utils.test.tap.DummyTapDatabaseConnection implements TapJDBCPooledFunctions {
	
	public static final String ACTION_CMD_DELETE = "DELETE";
	public static final String ACTION_CMD_REQ_PK = "REQ_PK"; 
	public static final String ACTION_CMD_REQ_INDEX_RA_DEC = "REQ_INDEX_RA_DEC";
	public static final String ACTION_CMD_REMOVE_INDEX_RA_DEC = "REMOVE_INDEX_RA_DEC";
	public static final String ACTION_CMD_REQ_UPDATE_TABLE_SIZE = "REQ_UPDATE_TABLE_SIZE"; 
	public static final String ACTION_CMD_REQ_VACUUM = "REQ_VACUUM";

	public static final String ACTION_CMD_DROP_TABLE_FORCE = "DROP_TABLE_FORCE";
	public static final String ACTION_CMD_UPDATE_RADEC_USER_LOADED_TABLE = "UPDATE_RADEC_USER_LOADED_TABLE";
	public static final String ACTION_CMD_ADD_ACCESS_TO_PUBLIC_GROUP_TABLE = "ADD_ACCESS_TO_PUBLIC_GROUP_TABLE";
	public static final String ACTION_CMD_REMOVE_ACCESS_TO_PUBLIC_GROUP_TABLE = "REMOVE_ACCESS_TO_PUBLIC_GROUP_TABLE";
	public static final String ACTION_CMD_LIST_PUBLIC_GROUP_TABLES = "LIST_PUBLIC_GROUP_TABLES";
	public static final String ACTION_CMD_PUBLIC_GROUP_SHARED_ITEMS = "GET_PUBLIC_GROUP_SHARED_ITEMS";
	public static final String ACTION_CMD_CHANGE_JOB_NAME = "CHANGE_JOB_NAME";
	public static final String ACTION_CMD_CREATE_TAP_SCHEMA = "CREATE_TAP_SCHEMA";
	public static final String ACTION_CMD_DELETE_TAP_SCHEMA = "DELETE_TAP_SCHEMA";
	

	private TapTableInfo tapTableInfo;
	
	public DummyGacsTapDatabaseConnection(DummyUwsDatabaseConnection dbconnection){
		super(dbconnection);
	}
	
	public void setTapTableInfo(TapTableInfo tapTableInfo){
		this.tapTableInfo = tapTableInfo;
	}
	

	@Override
	public void updateTableSizeInTapSchema(TAPSchemaInfo tapSchemaInfo, TAPTable table) throws DBException {
		//requestedUpdateTableSize = true;
		logAction(ACTION_CMD_REQ_UPDATE_TABLE_SIZE, table.getFullName());
	}

	@Override
	public String createPkInTable(TAPSchemaInfo tapSchemaInfo, TAPTable table) throws DBException {
		logAction(ACTION_CMD_REQ_PK, table.getFullName());
		return "OK";
	}
	
	@Override
	public void createRaAndDecIndexes(TAPSchemaInfo tapSchemaInfo, String schemaName, String tableName, String raCol, String decCol, int raDecFlag, String tableSpace) throws DBException {
		logAction(ACTION_CMD_REQ_INDEX_RA_DEC, schemaName + "." + tableName + " (ra: " + raCol + ",dec: "+decCol+", tableSpace: "+tableSpace+")");
	}

	@Override
	public void removeRaAndDecIndexes(TAPSchemaInfo tapSchemaInfo, String schemaName, String tableName, String raCol, String decCol, int raDecFlag) throws DBException {
		logAction(ACTION_CMD_REMOVE_INDEX_RA_DEC, schemaName + "." + tableName + " (ra: " + raCol + ",dec: "+decCol+")");
	}
	@Override
	public void vacuumAnalyze(String schemaName, String tableName) throws DBException {
		logAction(ACTION_CMD_REQ_VACUUM, schemaName+"."+tableName);
	}
	
	
	@Override
	public void createTableColumnIndex(String schemaName, String tableName, String tableColumnName) throws DBException {
		logAction(ACTION_CMD_INDEX_TABLE_COLUMN,schemaName+"."+tableName+"."+tableColumnName);
	}

	@Override
	public void updateUserTableData(TAPSchemaInfo tapSchemaInfo, String schemaName, String tableName,
			String tableColumnName, String ucd, String uType, int flags, int indexed) throws DBException {
		logAction(ACTION_CMD_UPDATE_USER_LOADED_TABLE,schemaName+"."+tableName+"."+tableColumnName);
	}
	
	@Override
	public TapTableInfo fetchTapTableInfo(TAPSchemaInfo tapSchemaInfo, String schemaName, String tableName) throws DBException {
		logAction(ACTION_CMD_FETCH_TAP_TABLE_INFO,schemaName+"."+tableName);
		return tapTableInfo;
	}
	
	@Override
	public void removeTableColumnIndex(String schemaName, String tableName, String tableColumnName) throws DBException {
		logAction(ACTION_CMD_REMOVE_INDEX_TABLE_COLUMN, schemaName+"."+tableName+"."+tableColumnName);
	}
	
	@Override
	public void dropTable(TAPTable table, boolean forceRemoval) throws DBException {
		//System.out.println("TapDBConnection: Entering dropTable("+table.getDBName()+")");
		logAction(ACTION_CMD_DROP_TABLE_FORCE, table.getFullName());
	}


	@Override
	public void updateUserTableRaDecData(TAPSchemaInfo tapSchemaInfo, String schemaName, String tableName,
			String raColumn, String decColumn, int flagsRa, int flagsDec)
			throws DBException {
		logAction(ACTION_CMD_UPDATE_RADEC_USER_LOADED_TABLE,
				schemaName+"."+tableName+" - "+raColumn + "/" + decColumn + " ("+flagsRa+"/"+flagsDec+")");
	}

	@Override
	public void addAccessToPublicGroupTables(List<PublicGroupItem> items) throws DBException {
		logAction(ACTION_CMD_ADD_ACCESS_TO_PUBLIC_GROUP_TABLE, ""+items.size());
	}

	@Override
	public void removeAccessToPublicGroupTables(List<PublicGroupItem> items) throws DBException {
		logAction(ACTION_CMD_REMOVE_ACCESS_TO_PUBLIC_GROUP_TABLE, ""+items.size());
	}

	
	@Override
	public List<PublicGroupItem> getPublicGroupTables(String user) throws DBException {
		logAction(ACTION_CMD_LIST_PUBLIC_GROUP_TABLES, user);
		return null;
	}

	@Override
	public List<String> getPublicGroupSharedItems(List<PublicGroupItem> items, String groupid) throws DBException {
		logAction(ACTION_CMD_PUBLIC_GROUP_SHARED_ITEMS, groupid+" - "+items.size());
		return null;
	}

	@Override
	public void changeJobName(String jobid, String jobName) throws DBException {
		logAction(ACTION_CMD_CHANGE_JOB_NAME, jobid+" - "+jobName);
	}

	@Override
	public void createTapSchema(String tapSchemaName) throws DBException {
		logAction(ACTION_CMD_CREATE_TAP_SCHEMA, tapSchemaName);
	}

	@Override
	public void deleteTapSchema(String tapSchemaName) throws DBException {
		logAction(ACTION_CMD_DELETE_TAP_SCHEMA, tapSchemaName);
	}
	
}
