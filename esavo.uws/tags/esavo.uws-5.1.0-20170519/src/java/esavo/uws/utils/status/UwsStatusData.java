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
package esavo.uws.utils.status;

public class UwsStatusData {
	
	public static final String TYPE_INGESTION = "INGESTION";
	public static final String TYPE_UPLOAD = "UPLOAD";
	public static final String TYPE_PARSE = "PARSE";
	
	private String statusData;
	private String statusType;
	
	public UwsStatusData(String statusType, String initialInfo){
		this.statusType = statusType;
		this.statusData = initialInfo;
	}

	public void setData(String data) {
		this.statusData = data;
	}

	public String getData() {
		return this.statusData;
	}
	
	public String getType(){
		return this.statusType;
	}
	
	@Override
	public String toString(){
		return "Status type: " + getType() + ", data: " + getData();
	}

}
