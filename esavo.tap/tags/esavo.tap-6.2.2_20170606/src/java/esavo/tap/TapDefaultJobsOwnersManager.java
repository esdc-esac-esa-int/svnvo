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
package esavo.tap;

import esavo.tap.db.DBConnection;
import esavo.tap.db.DBException;
import esavo.tap.metadata.TAPMetadata;
import esavo.tap.metadata.TAPSchema;
import esavo.uws.UwsException;
import esavo.uws.config.UwsConfiguration;
import esavo.uws.owner.UwsDefaultJobsOwnersManager;
import esavo.uws.owner.UwsJobOwner;
import esavo.uws.share.UwsShareManager;
import esavo.uws.storage.UwsStorage;
import esavo.uws.utils.UwsUtils;

public class TapDefaultJobsOwnersManager extends UwsDefaultJobsOwnersManager {

	private TAPFactory factory;
	private TAPService service;
	
	public TapDefaultJobsOwnersManager(String appid, UwsStorage storage,
			UwsShareManager shareManager, TAPFactory factory, TAPService service) {
		super(appid, storage, shareManager);
		this.factory = factory;
		this.service = service;
	}
	
	
	@Override
	public synchronized UwsJobOwner createDefaultOwner(String ownerid, String appid) throws UwsException{
		UwsJobOwner owner = super.createDefaultOwner(ownerid, appid);
		TAPSchemaInfo tapSchemaInfo;
		try {
			tapSchemaInfo = service.getTapSchemaInfo(owner);
		} catch (TAPException e) {
			throw new UwsException("Cannot obtain tap schema info for user '"+ownerid+"': " + e.getMessage(), e);
		} 
		
		// DO NOT CREATE SCHEMA IF USER IS ANONYMOUS.
		if(!UwsUtils.isAnonymous(owner)){
			DBConnection dbConn = null;
			String userSchema = TAPMetadata.getUserSchema(owner.getId());
			try {
				dbConn = factory.createDBConnection("TAP(Owner schema creation)", UwsConfiguration.UWS_JDBC_STORAGE_MANAGEMENT_POOL_ID);
				dbConn.createSchema(userSchema);
				TAPSchema schema = new TAPSchema(userSchema, "TAP Schema for user "+ownerid, null, null, false);
				dbConn.registerInTapSchema(tapSchemaInfo, schema);
			} catch (TAPException e) {
				throw new UwsException(e);
			} finally {
				try {
					dbConn.close();
				} catch (DBException e) {
					e.printStackTrace();
				}
			}
		}
		
		return owner;
	}

}
