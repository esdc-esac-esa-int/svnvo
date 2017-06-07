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
package esavo.sl.dd.requests;

import java.util.List;
import java.util.Map;


import esavo.sl.dd.util.DDFilePath;
import esavo.uws.config.UwsConfiguration;
import esavo.uws.owner.UwsJobOwner;

public interface DDFunctions {
	
	public DDRequestResult process(DDRetrievalRequest retrievalRequest) throws Exception;
	public UwsConfiguration getConfiguration();
	
	public int getProprietary(Map<String,String> retrievalElement, UwsJobOwner user) throws Exception;
	public List<DDFilePath> getPaths(DDRetrievalRequest retrievalRequest) throws Exception;
	
	public void insertLog(DDRetrievalRequest retrievalRequest, int statusOid) throws Exception;
	public void updateLog(DDRetrievalRequest retrievalRequest, double size, int statusOid, String propStatus) throws Exception;

}
