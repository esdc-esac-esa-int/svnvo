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
package esavo.uws.owner;

import esavo.uws.UwsException;

public interface UwsJobsOwnersManager {
	
	public static final String OWNER_PARAMETER_DB_QUOTA = "db_quota";
	public static final String OWNER_PARAMETER_FILES_QUOTA = "files_quota";
	public static final String OWNER_PARAMETER_ASYNC_MAX_EXEC_TIME = "async_max_exec_time";
	public static final String OWNER_PARAMETER_SYNC_MAX_EXEC_TIME = "sync_max_exec_time";
	public static final String OWNER_PARAMETER_CURRENT_DB_SIZE = "db_current_size";
	public static final String OWNER_PARAMETER_CURRENT_FILES_SIZE = "files_current_size";
	
	public UwsJobOwner loadOrCreateOwner(String ownerid) throws UwsException;
	public boolean updateOwnerRoles(UwsJobOwner owner) throws UwsException;
	public boolean updateOwnerParameter(UwsJobOwner owner, String parameterName) throws UwsException;

}
